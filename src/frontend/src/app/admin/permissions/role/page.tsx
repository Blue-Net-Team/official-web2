'use client'

import { useState, useCallback, useRef, useMemo } from 'react'
import {
  Card,
  Radio,
  Button,
  Space,
  App,
  Alert,
  Typography,
  Divider,
  Descriptions,
  Tag,
} from 'antd'
import { SaveOutlined, InfoCircleOutlined } from '@ant-design/icons'
import PermissionTree from '@/components/Admin/PermissionTree'
import { adminPermissionService } from '@/apis/services/admin-permission.service'
import { getRoleLevel } from '@/utils/RoleUtils'
import authStore from '@/stores/authStore'
import type { PermissionDTO, PermissionTreeDTO } from '@/apis/schema/type'

const { Title, Text } = Typography

const ACCESS_LEVEL_CONFIG: Record<string, { color: string; label: string }> = {
  PUBLIC: { color: 'green', label: '公开访问' },
  AUTHENTICATED: { color: 'blue', label: '登录即可' },
  PROTECTED: { color: 'default', label: '需要权限' },
}

const ROLES = [
  { value: 'SUPER_ADMIN', label: '超级管理员' },
  { value: 'DIRECTION_ADMIN', label: '方向管理员' },
  { value: 'MEMBER', label: '正式成员' },
  { value: 'CANDIDATE', label: '候选人' },
]

function mapValuesToIds(nodes: PermissionTreeDTO[], valueSet: Set<string>): number[] {
  const ids: number[] = []
  const walk = (list: PermissionTreeDTO[]) => {
    for (const node of list) {
      if (node.leaf && node.value && valueSet.has(node.value) && node.permissionId != null) {
        ids.push(node.permissionId)
      }
      if (node.children) walk(node.children)
    }
  }
  walk(nodes)
  return ids
}

function getAlwaysEnabledIds(nodes: PermissionTreeDTO[]): Set<number> {
  const ids = new Set<number>()
  const walk = (list: PermissionTreeDTO[]) => {
    for (const node of list) {
      if (
        node.leaf &&
        (node.accessLevel === 'PUBLIC' || node.accessLevel === 'AUTHENTICATED') &&
        node.permissionId != null
      ) {
        ids.add(node.permissionId)
      }
      if (node.children) walk(node.children)
    }
  }
  walk(nodes)
  return ids
}

export default function RolePermissionPage() {
  const { message } = App.useApp()
  const userInfo = authStore((state) => state.userInfo)
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  const [selectedRole, setSelectedRole] = useState<string>('MEMBER')
  const [assignedPermissions, setAssignedPermissions] = useState<string[]>([])
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([])
  const [initialPermissionIds, setInitialPermissionIds] = useState<number[]>([])
  const [loading, setLoading] = useState(false)
  const [previewPermission, setPreviewPermission] = useState<PermissionDTO | null>(null)

  const treeDataRef = useRef<PermissionTreeDTO[]>([])

  const hasChanges = useMemo(() => {
    const current = new Set(selectedPermissionIds)
    const initial = new Set(initialPermissionIds)
    if (current.size !== initial.size) return true
    for (const id of current) {
      if (!initial.has(id)) return true
    }
    return false
  }, [selectedPermissionIds, initialPermissionIds])

  const handleRoleChange = useCallback(
    async (roleName: string) => {
      setSelectedRole(roleName)
      setPreviewPermission(null)
      setSelectedPermissionIds([])
      setInitialPermissionIds([])
      setLoading(true)
      try {
        const [permRes, treeRes] = await Promise.all([
          adminPermissionService.getRolePermissions(roleName),
          adminPermissionService.getPermissionTree(),
        ])

        if (treeRes.code === 200 && treeRes.data) {
          treeDataRef.current = treeRes.data
        }

        if (permRes.code === 200 && permRes.data) {
          setAssignedPermissions(permRes.data)
          const ids = mapValuesToIds(treeDataRef.current, new Set(permRes.data))
          setInitialPermissionIds(ids)
          setSelectedPermissionIds(ids)
        }
      } catch {
        message.error('加载角色权限失败')
      } finally {
        setLoading(false)
      }
    },
    [message]
  )

  const handleSelectionChange = useCallback((ids: number[]) => {
    setSelectedPermissionIds(ids)
  }, [])

  const handlePermissionSelect = useCallback(async (permissionId: number) => {
    try {
      const res = await adminPermissionService.getPermissionDetail(permissionId)
      if (res.code === 200 && res.data) {
        setPreviewPermission(res.data)
      }
    } catch {
      // silently ignore
    }
  }, [])

  const handleSave = async () => {
    const alwaysEnabled = getAlwaysEnabledIds(treeDataRef.current)

    const toAdd = selectedPermissionIds.filter(
      (id) => !initialPermissionIds.includes(id) && !alwaysEnabled.has(id)
    )
    const toRemove = initialPermissionIds.filter(
      (id) => !selectedPermissionIds.includes(id) && !alwaysEnabled.has(id)
    )

    if (toAdd.length === 0 && toRemove.length === 0) {
      message.info('没有需要变更的权限')
      return
    }

    setLoading(true)
    try {
      if (toAdd.length > 0) {
        const res = await adminPermissionService.assignPermissionsToRole(selectedRole, {
          permissionIds: toAdd,
        })
        if (res.code !== 200) {
          message.error(res.msg || '分配权限失败')
          return
        }
      }
      if (toRemove.length > 0) {
        const res = await adminPermissionService.removePermissionsFromRole(selectedRole, {
          permissionIds: toRemove,
        })
        if (res.code !== 200) {
          message.error(res.msg || '移除权限失败')
          return
        }
      }

      message.success('权限已更新')

      // Reload fresh state
      const permRes = await adminPermissionService.getRolePermissions(selectedRole)
      if (permRes.code === 200 && permRes.data) {
        setAssignedPermissions(permRes.data)
        const ids = mapValuesToIds(treeDataRef.current, new Set(permRes.data))
        setInitialPermissionIds(ids)
        setSelectedPermissionIds(ids)
      }
    } catch {
      message.error('操作失败')
    } finally {
      setLoading(false)
    }
  }

  const isSuperAdmin = selectedRole === 'SUPER_ADMIN'

  return (
    <div className="p-6">
      <Title level={4} className="!mb-6">
        角色权限管理
      </Title>

      <div className="flex gap-6">
        <Card
          title={`权限列表 - ${ROLES.find((r) => r.value === selectedRole)?.label || selectedRole}`}
          className="flex-1"
          styles={{ body: { padding: 16 } }}
        >
          <PermissionTree
            assignedPermissions={assignedPermissions}
            onSelectionChange={handleSelectionChange}
            onSelect={handlePermissionSelect}
            mode="checkable"
          />
        </Card>

        <Card
          title="角色权限分配"
          className="w-[360px] shrink-0"
          styles={{ body: { padding: 16 } }}
          extra={
            <Button
              type="primary"
              icon={<SaveOutlined />}
              onClick={handleSave}
              loading={loading}
              disabled={isSuperAdmin || !hasChanges}
            >
              保存
            </Button>
          }
        >
          {isSuperAdmin && (
            <Alert
              message="SUPER_ADMIN 角色绕过权限检查"
              description="该角色自动拥有所有权限，无需手动分配或移除。"
              type="info"
              icon={<InfoCircleOutlined />}
              showIcon
              className="mb-4"
            />
          )}

          <Text strong className="block mb-3">
            选择角色
          </Text>
          <Radio.Group
            value={selectedRole}
            onChange={(e) => handleRoleChange(e.target.value)}
            className="w-full"
          >
            <Space direction="vertical" className="w-full">
              {ROLES.filter((r) => {
                if (r.value === 'SUPER_ADMIN') return roleLevel >= 4
                return true
              }).map((role) => (
                <Radio key={role.value} value={role.value} className="w-full">
                  <div>
                    <Text strong>{role.label}</Text>
                    <br />
                    <Text type="secondary" className="text-xs">
                      {role.value}
                    </Text>
                  </div>
                </Radio>
              ))}
            </Space>
          </Radio.Group>

          <Divider />

          <div className="text-xs text-[#999]">
            <Text type="secondary">已分配权限数：{assignedPermissions.length}</Text>
          </div>

          {previewPermission && (
            <>
              <Divider />
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="权限标识">{previewPermission.value}</Descriptions.Item>
                <Descriptions.Item label="权限名称">{previewPermission.name}</Descriptions.Item>
                <Descriptions.Item label="访问级别">
                  <Tag
                    color={ACCESS_LEVEL_CONFIG[previewPermission.accessLevel]?.color || 'default'}
                  >
                    {ACCESS_LEVEL_CONFIG[previewPermission.accessLevel]?.label ||
                      previewPermission.accessLevel}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="接口路径">
                  {previewPermission.url || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="HTTP方法">
                  {previewPermission.method || '-'}
                </Descriptions.Item>
              </Descriptions>
            </>
          )}
        </Card>
      </div>
    </div>
  )
}
