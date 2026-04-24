'use client'

import { useState, useCallback, useMemo, useEffect } from 'react'
import {
  Card,
  Checkbox,
  Button,
  Space,
  App,
  Typography,
  Descriptions,
  Spin,
  Empty,
  Tag,
} from 'antd'
import { SaveOutlined } from '@ant-design/icons'
import PermissionTree from '@/components/Admin/PermissionTree'
import { adminPermissionService } from '@/apis/services/admin-permission.service'
import { useApi } from '@/hooks'
import type { PermissionDTO } from '@/apis/schema/type'

const { Title, Text } = Typography

const ACCESS_LEVEL_CONFIG: Record<string, { color: string; label: string }> = {
  PUBLIC: { color: 'green', label: '公开访问' },
  AUTHENTICATED: { color: 'blue', label: '登录即可' },
  PROTECTED: { color: 'default', label: '需要权限' },
}

const AVAILABLE_ROLES = [
  { value: 'DIRECTION_ADMIN', label: '方向管理员' },
  { value: 'MEMBER', label: '正式成员' },
  { value: 'CANDIDATE', label: '候选人' },
]

export default function PermissionRolePage() {
  const { message } = App.useApp()

  const {
    data: permissionDetail,
    loading: detailLoading,
    execute: fetchDetail,
  } = useApi(adminPermissionService.getPermissionDetail.bind(adminPermissionService))
  const {
    data: rolesData,
    loading: rolesLoading,
    execute: fetchRoles,
  } = useApi(adminPermissionService.getPermissionRoles.bind(adminPermissionService))

  const [selectedPermissionId, setSelectedPermissionId] = useState<number | null>(null)
  const [assignedRoles, setAssignedRoles] = useState<string[]>([])
  const [checkedRoles, setCheckedRoles] = useState<string[]>([])
  const [saving, setSaving] = useState(false)

  const loading = detailLoading || rolesLoading || saving

  const handleSelect = useCallback(
    async (permissionId: number) => {
      setSelectedPermissionId(permissionId)
      try {
        await Promise.all([fetchDetail(permissionId), fetchRoles(permissionId)])
      } catch {
        message.error('加载权限详情失败')
      }
    },
    [fetchDetail, fetchRoles, message]
  )

  useEffect(() => {
    if (rolesData) {
      setAssignedRoles(rolesData)
      setCheckedRoles(rolesData)
    }
  }, [rolesData])

  const isAlwaysEnabled = permissionDetail
    ? permissionDetail.accessLevel === 'PUBLIC' || permissionDetail.accessLevel === 'AUTHENTICATED'
    : false

  const handleRoleCheck = useCallback(
    (roleName: string, checked: boolean) => {
      if (isAlwaysEnabled) return
      setCheckedRoles((prev) =>
        checked ? [...prev, roleName] : prev.filter((r) => r !== roleName)
      )
    },
    [isAlwaysEnabled]
  )

  const roleChanges = useMemo(() => {
    const toAdd = checkedRoles.filter((r) => !assignedRoles.includes(r))
    const toRemove = assignedRoles.filter((r) => !checkedRoles.includes(r))
    return { toAdd, toRemove }
  }, [checkedRoles, assignedRoles])

  const handleSave = async () => {
    if (!selectedPermissionId) {
      message.warning('请先选择一个权限')
      return
    }

    const { toAdd, toRemove } = roleChanges

    if (toAdd.length === 0 && toRemove.length === 0) {
      message.info('没有需要变更的角色')
      return
    }

    setSaving(true)
    try {
      if (toAdd.length > 0) {
        const res = await adminPermissionService.assignRolesToPermission(selectedPermissionId, {
          roleNames: toAdd,
        })
        if (res.code !== 200) {
          message.error(res.msg || '添加角色失败')
          return
        }
      }
      if (toRemove.length > 0) {
        const res = await adminPermissionService.removeRolesFromPermission(selectedPermissionId, {
          roleNames: toRemove,
        })
        if (res.code !== 200) {
          message.error(res.msg || '移除角色失败')
          return
        }
      }
      message.success('角色分配已更新')
      await fetchRoles(selectedPermissionId)
    } catch {
      message.error('操作失败')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="p-6">
      <Title level={4} className="!mb-6">
        权限角色管理
      </Title>

      <div className="flex gap-6">
        <Card title="选择权限" className="flex-1" styles={{ body: { padding: 16 } }}>
          <PermissionTree assignedPermissions={[]} onSelect={handleSelect} mode="selectable" />
        </Card>

        <Card
          title="角色分配"
          className="w-[360px] shrink-0"
          styles={{ body: { padding: 16 } }}
          extra={
            <Button
              type="primary"
              icon={<SaveOutlined />}
              onClick={handleSave}
              loading={loading}
              disabled={!selectedPermissionId || isAlwaysEnabled}
            >
              保存
            </Button>
          }
        >
          {loading && <Spin className="block mx-auto my-5" />}

          {!loading && !selectedPermissionId && <Empty description="请在左侧点击选择一个权限" />}

          {!loading && selectedPermissionId && permissionDetail && (
            <>
              <Descriptions column={1} size="small" bordered className="mb-4">
                <Descriptions.Item label="权限标识">{permissionDetail.value}</Descriptions.Item>
                <Descriptions.Item label="权限名称">{permissionDetail.name}</Descriptions.Item>
                <Descriptions.Item label="访问级别">
                  <Tag
                    color={ACCESS_LEVEL_CONFIG[permissionDetail.accessLevel]?.color || 'default'}
                  >
                    {ACCESS_LEVEL_CONFIG[permissionDetail.accessLevel]?.label ||
                      permissionDetail.accessLevel}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="接口路径">
                  {permissionDetail.url || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="HTTP方法">
                  {permissionDetail.method || '-'}
                </Descriptions.Item>
              </Descriptions>

              <Text strong className="block mb-3">
                拥有此权限的角色：
              </Text>

              {isAlwaysEnabled && (
                <Text type="secondary" className="block mb-2 text-xs">
                  该权限为{permissionDetail.accessLevel === 'PUBLIC' ? '公开' : '登录'}
                  访问，所有角色自动拥有，无需分配。
                </Text>
              )}

              <Space direction="vertical" className="w-full">
                {AVAILABLE_ROLES.map((role) => (
                  <Checkbox
                    key={role.value}
                    checked={isAlwaysEnabled || checkedRoles.includes(role.value)}
                    onChange={(e) => handleRoleCheck(role.value, e.target.checked)}
                    disabled={isAlwaysEnabled}
                  >
                    <div>
                      <Text strong>{role.label}</Text>
                      <br />
                      <Text type="secondary" className="text-xs">
                        {role.value}
                      </Text>
                    </div>
                  </Checkbox>
                ))}
              </Space>
            </>
          )}
        </Card>
      </div>
    </div>
  )
}
