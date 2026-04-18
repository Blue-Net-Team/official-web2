'use client'

import { useState, useEffect, useMemo, useCallback } from 'react'
import { Tree, Input, Spin, Empty, Badge, Tag } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import type { TreeProps } from 'antd'
import type { PermissionTreeDTO } from '@/apis/schema/type'
import { adminPermissionService } from '@/apis/services/admin-permission.service'

type PermissionTreeMode = 'checkable' | 'selectable'

interface PermissionTreeProps {
  assignedPermissions: string[]
  onSelectionChange?: (permissionIds: number[]) => void
  onSelect?: (permissionId: number) => void
  mode?: PermissionTreeMode
}

interface AntdTreeNode {
  key: string
  title: React.ReactNode
  isLeaf?: boolean
  permissionId?: number | null
  accessLevel?: string | null
  disabled?: boolean
  children?: AntdTreeNode[]
}

const ACCESS_LEVEL_CONFIG: Record<string, { color: string; label: string }> = {
  PUBLIC: { color: 'green', label: '公开' },
  AUTHENTICATED: { color: 'blue', label: '登录' },
  PROTECTED: { color: 'default', label: '受保护' },
}

function isAlwaysEnabled(accessLevel: string | null | undefined): boolean {
  return accessLevel === 'PUBLIC' || accessLevel === 'AUTHENTICATED'
}

function treeToAntdNodes(
  nodes: PermissionTreeDTO[],
  searchKeyword: string,
  assignedSet: Set<string>,
  mode: PermissionTreeMode
): AntdTreeNode[] {
  return nodes
    .map((node): AntdTreeNode | null => {
      const matchesSearch =
        !searchKeyword ||
        node.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        (node.value != null && node.value.toLowerCase().includes(searchKeyword.toLowerCase()))

      const childNodes = node.children
        ? treeToAntdNodes(node.children, searchKeyword, assignedSet, mode)
        : []

      if (!matchesSearch && childNodes.length === 0) {
        return null
      }

      const isAssigned = node.value != null && assignedSet.has(node.value)
      const alwaysEnabled = isAlwaysEnabled(node.accessLevel)

      const accessTag =
        node.leaf && node.accessLevel ? (
          <Tag
            color={ACCESS_LEVEL_CONFIG[node.accessLevel]?.color || 'default'}
            style={{ marginLeft: 4, fontSize: 11, lineHeight: '18px', padding: '0 4px' }}
          >
            {ACCESS_LEVEL_CONFIG[node.accessLevel]?.label || node.accessLevel}
          </Tag>
        ) : null

      return {
        key: node.key,
        title: (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
            {node.title}
            {accessTag}
            {isAssigned && <Badge status="success" style={{ marginLeft: 4 }} />}
            {!node.leaf && node.permissionCount > 0 && (
              <span style={{ color: '#999', fontSize: 12 }}>({node.permissionCount})</span>
            )}
          </span>
        ),
        isLeaf: node.leaf,
        permissionId: node.permissionId,
        accessLevel: node.accessLevel,
        disabled: mode === 'checkable' && alwaysEnabled,
        children: childNodes.length > 0 ? childNodes : undefined,
      }
    })
    .filter((node): node is AntdTreeNode => node !== null)
}

function collectLeafIds(nodes: PermissionTreeDTO[], assignedSet: Set<string>): number[] {
  const ids: number[] = []
  for (const node of nodes) {
    if (
      node.leaf &&
      node.value != null &&
      assignedSet.has(node.value) &&
      node.permissionId != null
    ) {
      ids.push(node.permissionId)
    }
    if (node.children) {
      ids.push(...collectLeafIds(node.children, assignedSet))
    }
  }
  return ids
}

function collectAllLeafIds(nodes: PermissionTreeDTO[]): number[] {
  const ids: number[] = []
  for (const node of nodes) {
    if (node.leaf && node.permissionId != null) {
      ids.push(node.permissionId)
    }
    if (node.children) {
      ids.push(...collectAllLeafIds(node.children))
    }
  }
  return ids
}

/** Collect keys of always-enabled (PUBLIC/AUTHENTICATED) leaf permissions */
function collectAlwaysEnabledKeys(nodes: PermissionTreeDTO[]): string[] {
  const keys: string[] = []
  for (const node of nodes) {
    if (node.leaf && isAlwaysEnabled(node.accessLevel)) {
      keys.push(node.key)
    }
    if (node.children) {
      keys.push(...collectAlwaysEnabledKeys(node.children))
    }
  }
  return keys
}

const PermissionTree = ({
  assignedPermissions,
  onSelectionChange,
  onSelect,
  mode = 'checkable',
}: PermissionTreeProps) => {
  const [treeData, setTreeData] = useState<PermissionTreeDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [checkedKeys, setCheckedKeys] = useState<string[]>([])
  const [selectedKey, setSelectedKey] = useState<string | null>(null)

  useEffect(() => {
    const fetchTree = async () => {
      setLoading(true)
      try {
        const res = await adminPermissionService.getPermissionTree()
        if (res.code === 200 && res.data) {
          setTreeData(res.data)
        }
      } finally {
        setLoading(false)
      }
    }
    fetchTree()
  }, [])

  const assignedSet = useMemo(() => new Set(assignedPermissions), [assignedPermissions])

  // Keys that are always enabled (PUBLIC/AUTHENTICATED)
  const alwaysEnabledKeys = useMemo(() => new Set(collectAlwaysEnabledKeys(treeData)), [treeData])

  useEffect(() => {
    const assignedKeys: string[] = []
    // Include always-enabled keys
    for (const key of alwaysEnabledKeys) {
      assignedKeys.push(key)
    }
    const walk = (nodes: PermissionTreeDTO[]) => {
      for (const node of nodes) {
        if (node.leaf && node.value != null && assignedSet.has(node.value)) {
          assignedKeys.push(node.key)
        }
        if (node.children) walk(node.children)
      }
    }
    walk(treeData)
    setCheckedKeys(assignedKeys)
  }, [assignedSet, treeData, alwaysEnabledKeys])

  const antdTreeData = useMemo(
    () => treeToAntdNodes(treeData, searchKeyword, assignedSet, mode),
    [treeData, searchKeyword, assignedSet, mode]
  )

  const handleCheck: TreeProps['onCheck'] = useCallback(
    (checked: React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }) => {
      const keys = (Array.isArray(checked) ? checked : []) as string[]
      // Always keep always-enabled keys checked
      const finalKeys = Array.from(new Set([...keys, ...alwaysEnabledKeys]))
      setCheckedKeys(finalKeys)

      if (onSelectionChange) {
        const selectedIds: number[] = []
        const walk = (nodes: PermissionTreeDTO[]) => {
          for (const node of nodes) {
            if (node.leaf && finalKeys.includes(node.key) && node.permissionId != null) {
              selectedIds.push(node.permissionId)
            }
            if (node.children) walk(node.children)
          }
        }
        walk(treeData)
        onSelectionChange(selectedIds)
      }
    },
    [treeData, onSelectionChange, alwaysEnabledKeys]
  )

  const handleSelect: TreeProps['onSelect'] = useCallback(
    (selectedKeys: React.Key[]) => {
      const key = selectedKeys[0] as string | undefined
      setSelectedKey(key || null)

      if (key && onSelect) {
        const node = findNodeByKey(treeData, key)
        if (node?.leaf && node.permissionId != null) {
          onSelect(node.permissionId)
        }
      }
    },
    [treeData, onSelect]
  )

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin tip="加载权限树..." />
      </div>
    )
  }

  return (
    <div>
      <Input
        placeholder="搜索权限..."
        prefix={<SearchOutlined />}
        value={searchKeyword}
        onChange={(e) => setSearchKeyword(e.target.value)}
        allowClear
        style={{ marginBottom: 12 }}
      />
      {antdTreeData && antdTreeData.length > 0 ? (
        <Tree
          checkable={mode === 'checkable'}
          checkedKeys={mode === 'checkable' ? checkedKeys : undefined}
          onCheck={mode === 'checkable' ? handleCheck : undefined}
          selectedKeys={selectedKey ? [selectedKey] : []}
          onSelect={onSelect ? handleSelect : undefined}
          treeData={antdTreeData}
          defaultExpandAll={!!searchKeyword}
          showLine={{ showLeafIcon: false }}
        />
      ) : (
        <Empty description="暂无权限数据" />
      )}
    </div>
  )
}

function findNodeByKey(nodes: PermissionTreeDTO[], key: string): PermissionTreeDTO | null {
  for (const node of nodes) {
    if (node.key === key) return node
    if (node.children) {
      const found = findNodeByKey(node.children, key)
      if (found) return found
    }
  }
  return null
}

export default PermissionTree
export { collectLeafIds, collectAllLeafIds }
