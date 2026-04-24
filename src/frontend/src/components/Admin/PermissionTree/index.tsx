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
  disableCheckbox?: boolean
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
      const accessTag =
        node.leaf && node.accessLevel ? (
          <Tag
            color={ACCESS_LEVEL_CONFIG[node.accessLevel]?.color || 'default'}
            className="ml-1 text-[11px] leading-[18px] px-1"
          >
            {ACCESS_LEVEL_CONFIG[node.accessLevel]?.label || node.accessLevel}
          </Tag>
        ) : null

      return {
        key: node.key,
        title: (
          <span className="inline-flex items-center gap-1.5">
            {node.title}
            {accessTag}
            {isAssigned && <Badge status="success" className="ml-1" />}
            {!node.leaf && node.permissionCount > 0 && (
              <span className="text-[#999] text-xs">({node.permissionCount})</span>
            )}
          </span>
        ),
        isLeaf: node.leaf,
        permissionId: node.permissionId,
        accessLevel: node.accessLevel,
        // Keep always-enabled leaves checkable so AntD can compute parent half-check state correctly.
        // They are still enforced as selected in handleCheck via alwaysEnabledKeys union.
        disableCheckbox: false,
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

/** Extract leaf keys from a raw key set by walking the tree */
function filterLeafKeys(nodes: PermissionTreeDTO[], rawKeys: Set<string>): Set<string> {
  const leaves = new Set<string>()
  const walk = (list: PermissionTreeDTO[]) => {
    for (const node of list) {
      if (node.leaf && rawKeys.has(node.key)) leaves.add(node.key)
      if (node.children) walk(node.children)
    }
  }
  walk(nodes)
  return leaves
}

/** Tri-state walk: true = all checked, 'half' = some, false = none */
type WalkResult = true | 'half' | false

function computeCheckState(
  nodes: PermissionTreeDTO[],
  checkedLeaves: Set<string>
): { checked: string[]; halfChecked: string[] } {
  const checked: string[] = []
  const halfChecked: string[] = []

  const walk = (node: PermissionTreeDTO): WalkResult => {
    if (node.leaf) {
      if (checkedLeaves.has(node.key)) {
        checked.push(node.key)
        return true
      }
      return false
    }
    if (!node.children || node.children.length === 0) return false

    const childResults: WalkResult[] = node.children.map(walk)
    const allChecked = childResults.length > 0 && childResults.every((r) => r === true)
    const anyHit = childResults.some((r) => r !== false)

    if (allChecked) {
      checked.push(node.key)
      return true
    }
    if (anyHit) {
      halfChecked.push(node.key)
      return 'half'
    }
    return false
  }

  nodes.forEach(walk)
  return { checked, halfChecked }
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
  const [checkedKeys, setCheckedKeys] = useState<{ checked: string[]; halfChecked: string[] }>({
    checked: [],
    halfChecked: [],
  })
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
    if (treeData.length === 0) return

    const checkedLeaves = new Set<string>(alwaysEnabledKeys)
    const walk = (nodes: PermissionTreeDTO[]) => {
      for (const node of nodes) {
        if (node.leaf && node.value != null && assignedSet.has(node.value)) {
          checkedLeaves.add(node.key)
        }
        if (node.children) walk(node.children)
      }
    }
    walk(treeData)
    setCheckedKeys(computeCheckState(treeData, checkedLeaves))
  }, [assignedSet, treeData, alwaysEnabledKeys])

  const antdTreeData = useMemo(
    () => treeToAntdNodes(treeData, searchKeyword, assignedSet, mode),
    [treeData, searchKeyword, assignedSet, mode]
  )

  const handleCheck: TreeProps['onCheck'] = useCallback(
    (checked: React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }) => {
      const keys: string[] = Array.isArray(checked)
        ? (checked as string[])
        : (checked as { checked: React.Key[] }).checked.map(String)

      const rawKeys = new Set(keys)
      for (const k of alwaysEnabledKeys) rawKeys.add(k)

      const leafKeys = filterLeafKeys(treeData, rawKeys)
      setCheckedKeys(computeCheckState(treeData, leafKeys))

      if (onSelectionChange) {
        const selectedIds: number[] = []
        const walkIds = (nodes: PermissionTreeDTO[]) => {
          for (const node of nodes) {
            if (node.leaf && leafKeys.has(node.key) && node.permissionId != null) {
              selectedIds.push(node.permissionId)
            }
            if (node.children) walkIds(node.children)
          }
        }
        walkIds(treeData)
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
      <div className="text-center p-10">
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
        className="mb-3"
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
