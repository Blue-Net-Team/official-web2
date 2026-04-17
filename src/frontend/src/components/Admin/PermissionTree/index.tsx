'use client'

import { useState, useEffect, useMemo, useCallback } from 'react'
import { Tree, Input, Spin, Empty, Badge } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import type { TreeProps } from 'antd'
import type { PermissionTreeDTO } from '@/apis/schema/type'
import { adminPermissionService } from '@/apis/services/admin-permission.service'

interface PermissionTreeProps {
  assignedPermissions: string[]
  onSelectionChange: (permissionIds: number[]) => void
}

interface AntdTreeNode {
  key: string
  title: React.ReactNode
  isLeaf?: boolean
  permissionId?: number | null
  children?: AntdTreeNode[]
}

function treeToAntdNodes(
  nodes: PermissionTreeDTO[],
  searchKeyword: string,
  assignedSet: Set<string>
): AntdTreeNode[] {
  return nodes
    .map((node): AntdTreeNode | null => {
      const matchesSearch =
        !searchKeyword ||
        node.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        (node.value != null && node.value.toLowerCase().includes(searchKeyword.toLowerCase()))

      const childNodes = node.children
        ? treeToAntdNodes(node.children, searchKeyword, assignedSet)
        : []

      if (!matchesSearch && childNodes.length === 0) {
        return null
      }

      const isAssigned = node.value != null && assignedSet.has(node.value)

      return {
        key: node.key,
        title: (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
            {node.title}
            {isAssigned && <Badge status="success" style={{ marginLeft: 4 }} />}
            {!node.leaf && node.permissionCount > 0 && (
              <span style={{ color: '#999', fontSize: 12 }}>({node.permissionCount})</span>
            )}
          </span>
        ),
        isLeaf: node.leaf,
        permissionId: node.permissionId,
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

const PermissionTree = ({ assignedPermissions, onSelectionChange }: PermissionTreeProps) => {
  const [treeData, setTreeData] = useState<PermissionTreeDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [checkedKeys, setCheckedKeys] = useState<string[]>([])

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

  useEffect(() => {
    const assignedKeys: string[] = []
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
  }, [assignedSet, treeData])

  const antdTreeData = useMemo(
    () => treeToAntdNodes(treeData, searchKeyword, assignedSet),
    [treeData, searchKeyword, assignedSet]
  )

  const handleCheck: TreeProps['onCheck'] = useCallback(
    (checked) => {
      const keys = (Array.isArray(checked) ? checked : []) as string[]
      setCheckedKeys(keys)

      const selectedIds: number[] = []
      const walk = (nodes: PermissionTreeDTO[]) => {
        for (const node of nodes) {
          if (node.leaf && keys.includes(node.key) && node.permissionId != null) {
            selectedIds.push(node.permissionId)
          }
          if (node.children) walk(node.children)
        }
      }
      walk(treeData)
      onSelectionChange(selectedIds)
    },
    [treeData, onSelectionChange]
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
          checkable
          checkedKeys={checkedKeys}
          onCheck={handleCheck}
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

export default PermissionTree
export { collectLeafIds, collectAllLeafIds }
