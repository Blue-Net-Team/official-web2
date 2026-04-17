'use client'

import { useState, useCallback } from 'react'
import { Card, Radio, Button, Space, App, Alert, Typography, Divider } from 'antd'
import { CheckOutlined, DeleteOutlined, InfoCircleOutlined } from '@ant-design/icons'
import PermissionTree from '@/components/Admin/PermissionTree'
import { adminPermissionService } from '@/apis/services/admin-permission.service'
import { getRoleLevel } from '@/utils/RoleUtils'
import authStore from '@/stores/authStore'

const { Title, Text } = Typography

const ROLES = [
  { value: 'SUPER_ADMIN', label: '超级管理员', description: '绕过权限检查，无需分配权限' },
  { value: 'DIRECTION_ADMIN', label: '方向管理员' },
  { value: 'MEMBER', label: '正式成员' },
  { value: 'CANDIDATE', label: '候选人' },
]

export default function RolePermissionPage() {
  const { message } = App.useApp()
  const userInfo = authStore((state) => state.userInfo)
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  const [selectedRole, setSelectedRole] = useState<string>('MEMBER')
  const [assignedPermissions, setAssignedPermissions] = useState<string[]>([])
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([])
  const [loading, setLoading] = useState(false)

  const handleRoleChange = useCallback(
    async (roleName: string) => {
      setSelectedRole(roleName)
      setSelectedPermissionIds([])
      setLoading(true)
      try {
        const res = await adminPermissionService.getRolePermissions(roleName)
        if (res.code === 200 && res.data) {
          setAssignedPermissions(res.data)
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

  const handleAssign = async () => {
    if (selectedPermissionIds.length === 0) {
      message.warning('请先选择要分配的权限')
      return
    }
    setLoading(true)
    try {
      const res = await adminPermissionService.assignPermissionsToRole(selectedRole, {
        permissionIds: selectedPermissionIds,
      })
      if (res.code === 200 && res.data) {
        message.success(`成功分配 ${res.data.successCount} 个权限`)
        setAssignedPermissions(res.data.currentPermissions)
        setSelectedPermissionIds([])
      } else {
        message.error(res.msg || '分配失败')
      }
    } catch {
      message.error('分配权限失败')
    } finally {
      setLoading(false)
    }
  }

  const handleRemove = async () => {
    if (selectedPermissionIds.length === 0) {
      message.warning('请先选择要移除的权限')
      return
    }
    setLoading(true)
    try {
      const res = await adminPermissionService.removePermissionsFromRole(selectedRole, {
        permissionIds: selectedPermissionIds,
      })
      if (res.code === 200 && res.data) {
        message.success(`成功移除 ${res.data.successCount} 个权限`)
        setAssignedPermissions(res.data.currentPermissions)
        setSelectedPermissionIds([])
      } else {
        message.error(res.msg || '移除失败')
      }
    } catch {
      message.error('移除权限失败')
    } finally {
      setLoading(false)
    }
  }

  const isSuperAdmin = selectedRole === 'SUPER_ADMIN'

  return (
    <div style={{ padding: 24 }}>
      <Title level={4} style={{ marginBottom: 24 }}>
        角色权限管理
      </Title>

      <div style={{ display: 'flex', gap: 24 }}>
        <Card
          title="选择角色"
          style={{ width: 280, flexShrink: 0 }}
          styles={{ body: { padding: 16 } }}
        >
          <Radio.Group
            value={selectedRole}
            onChange={(e) => handleRoleChange(e.target.value)}
            style={{ width: '100%' }}
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              {ROLES.filter((r) => {
                if (r.value === 'SUPER_ADMIN') return roleLevel >= 4
                return true
              }).map((role) => (
                <Radio key={role.value} value={role.value} style={{ width: '100%' }}>
                  <div>
                    <Text strong>{role.label}</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {role.value}
                    </Text>
                  </div>
                </Radio>
              ))}
            </Space>
          </Radio.Group>

          <Divider />

          <div style={{ fontSize: 12, color: '#999' }}>
            <Text type="secondary">已分配权限数：{assignedPermissions.length}</Text>
          </div>
        </Card>

        <Card
          title={`权限列表 - ${ROLES.find((r) => r.value === selectedRole)?.label || selectedRole}`}
          style={{ flex: 1 }}
          styles={{ body: { padding: 16 } }}
          extra={
            <Space>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                onClick={handleAssign}
                loading={loading}
                disabled={isSuperAdmin || selectedPermissionIds.length === 0}
              >
                分配选中权限
              </Button>
              <Button
                danger
                icon={<DeleteOutlined />}
                onClick={handleRemove}
                loading={loading}
                disabled={isSuperAdmin || selectedPermissionIds.length === 0}
              >
                移除选中权限
              </Button>
            </Space>
          }
        >
          {isSuperAdmin && (
            <Alert
              message="SUPER_ADMIN 角色绕过权限检查"
              description="该角色自动拥有所有权限，无需手动分配或移除。"
              type="info"
              icon={<InfoCircleOutlined />}
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}

          <PermissionTree
            assignedPermissions={assignedPermissions}
            onSelectionChange={handleSelectionChange}
          />
        </Card>
      </div>
    </div>
  )
}
