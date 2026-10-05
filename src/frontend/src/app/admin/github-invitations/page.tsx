'use client'

import { useCallback, useMemo, useState } from 'react'
import {
  App,
  Button,
  Card,
  Checkbox,
  Grid,
  Input,
  Modal,
  Pagination,
  Select,
  Space,
  Spin,
  Table,
  Tag,
} from 'antd'
import type { TableColumnsType } from 'antd'
import { GithubOutlined, SendOutlined } from '@ant-design/icons'
import { usePagination, useAuth } from '@/hooks'
import { adminUserService } from '@/apis/services/admin-user.service'
import { adminGitHubOrgInvitationService } from '@/apis/services/admin-github-org-invitation.service'
import { DIRECTION_LABELS, ROLE_LABELS, getRoleTagColor } from '@/apis/schema/enumerate'
import type { AdminUserListItemDTO, GitHubOrgBatchInviteResultDTO } from '@/apis/schema/type'
import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'
import { getRoleLevel } from '@/utils/RoleUtils'

const PAGE_SIZE = 15

interface FilterValues {
  direction?: string
  keyword: string
}

export default function AdminGitHubInvitationsPage() {
  const { message: messageApi } = App.useApp()
  const { userInfo } = useAuth()
  const screens = Grid.useBreakpoint()
  const isMobile = !screens.md
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  const [filters, setFilters] = useState<FilterValues>({ keyword: '' })
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
  const [invitingUserIds, setInvitingUserIds] = useState<number[]>([])
  const [batchInviting, setBatchInviting] = useState(false)
  const [batchResult, setBatchResult] = useState<GitHubOrgBatchInviteResultDTO | null>(null)

  const fetchPage = useCallback(
    (page: number, pageSize: number) => {
      const params: Record<string, unknown> = { page, size: pageSize }
      if (filters.direction) params.direction = filters.direction
      if (filters.keyword) params.keyword = filters.keyword
      return adminUserService.getList(params as Parameters<typeof adminUserService.getList>[0])
    },
    [filters]
  )

  const { data, total, loading, currentPage, setCurrentPage } = usePagination(fetchPage, {
    pageSize: PAGE_SIZE,
  })

  const directionOptions = useMemo(
    () => Object.entries(DIRECTION_LABELS).map(([value, label]) => ({ value, label })),
    []
  )

  /** 单个邀请 */
  const handleInvite = async (user: AdminUserListItemDTO) => {
    setInvitingUserIds((prev) => [...prev, user.id])
    try {
      const res = await adminGitHubOrgInvitationService.inviteUser(user.id)
      if (res.code === 200 && res.data) {
        if (res.data.success) {
          messageApi.success(`${user.username}：${res.data.reason}`)
        } else {
          messageApi.error(`${user.username}：${res.data.reason}`)
        }
      } else {
        messageApi.error(res.msg || '邀请失败')
      }
    } catch {
      messageApi.error('邀请失败')
    } finally {
      setInvitingUserIds((prev) => prev.filter((id) => id !== user.id))
    }
  }

  /** 批量邀请 */
  const handleBatchInvite = async () => {
    if (selectedRowKeys.length === 0) {
      messageApi.warning('请先选择要邀请的用户')
      return
    }
    setBatchInviting(true)
    try {
      const res = await adminGitHubOrgInvitationService.inviteBatch(
        selectedRowKeys.map((key) => Number(key))
      )
      if (res.code === 200 && res.data) {
        setBatchResult(res.data)
      } else {
        messageApi.error(res.msg || '批量邀请失败')
      }
    } catch {
      messageApi.error('批量邀请失败')
    } finally {
      setBatchInviting(false)
    }
  }

  /** 渲染 GitHub 绑定状态 */
  const renderGithubStatus = (user: AdminUserListItemDTO) =>
    user.githubUsername ? (
      <Tag icon={<GithubOutlined />} color="blue">
        @{user.githubUsername}
      </Tag>
    ) : (
      <Tag>未绑定</Tag>
    )

  /** 渲染邀请按钮 */
  const renderInviteButton = (user: AdminUserListItemDTO) => (
    <Button
      type="primary"
      size="small"
      icon={<SendOutlined />}
      loading={invitingUserIds.includes(user.id)}
      onClick={() => handleInvite(user)}
    >
      邀请
    </Button>
  )

  const columns: TableColumnsType<AdminUserListItemDTO> = [
    {
      title: '姓名',
      key: 'name',
      render: (_, record) => record.username,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      render: (email: string | null) => email || '-',
    },
    {
      title: 'GitHub',
      key: 'github',
      render: (_, record) => renderGithubStatus(record),
    },
    {
      title: '方向',
      dataIndex: 'direction',
      key: 'direction',
      render: (direction: string | null) =>
        direction ? DIRECTION_LABELS[direction as keyof typeof DIRECTION_LABELS] || direction : '-',
    },
    {
      title: '角色',
      dataIndex: 'roleName',
      key: 'roleName',
      render: (roleName: string | null) =>
        roleName ? (
          <Tag color={getRoleTagColor(roleName)} bordered={false}>
            {ROLE_LABELS[roleName] || roleName}
          </Tag>
        ) : (
          <Tag bordered={false}>-</Tag>
        ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => renderInviteButton(record),
    },
  ]

  /** 批量结果弹窗中的用户昵称映射 */
  const userNameMap = useMemo(() => {
    const map = new Map<number, string>()
    data.forEach((user) => map.set(user.id, user.username))
    return map
  }, [data])

  if (roleLevel < 2) {
    return <ErrorPage config={ERROR_CONFIGS[403]} />
  }

  return (
    <div>
      <Card
        title={
          <Space>
            <GithubOutlined />
            <span>GitHub 组织邀请</span>
          </Space>
        }
        extra={
          isMobile ? undefined : (
            <Space wrap>
              <Input.Search
                placeholder="搜索姓名 / 学号 / 邮箱"
                allowClear
                style={{ width: 220 }}
                onSearch={(value) => {
                  setFilters((prev) => ({ ...prev, keyword: value }))
                  setCurrentPage(0)
                }}
              />
              <Select
                placeholder="方向"
                allowClear
                style={{ width: 140 }}
                options={directionOptions}
                onChange={(value) => {
                  setFilters((prev) => ({ ...prev, direction: value }))
                  setCurrentPage(0)
                }}
              />
              <Button
                type="primary"
                icon={<SendOutlined />}
                disabled={selectedRowKeys.length === 0}
                loading={batchInviting}
                onClick={handleBatchInvite}
              >
                批量邀请{selectedRowKeys.length > 0 ? `（${selectedRowKeys.length}）` : ''}
              </Button>
            </Space>
          )
        }
      >
        {isMobile && (
          <div className="flex flex-wrap items-center gap-2 pb-3">
            <Input.Search
              placeholder="搜索姓名 / 学号 / 邮箱"
              allowClear
              className="min-w-0 flex-[1_1_100%]"
              onSearch={(value) => {
                setFilters((prev) => ({ ...prev, keyword: value }))
                setCurrentPage(0)
              }}
            />
            <Select
              placeholder="方向"
              allowClear
              className="min-w-0 flex-1"
              options={directionOptions}
              onChange={(value) => {
                setFilters((prev) => ({ ...prev, direction: value }))
                setCurrentPage(0)
              }}
            />
            <Button
              type="primary"
              icon={<SendOutlined />}
              disabled={selectedRowKeys.length === 0}
              loading={batchInviting}
              onClick={handleBatchInvite}
            >
              批量邀请{selectedRowKeys.length > 0 ? `（${selectedRowKeys.length}）` : ''}
            </Button>
          </div>
        )}
        <Spin spinning={loading}>
          {isMobile ? (
            <div className="flex flex-col gap-3">
              {data.length === 0 ? (
                <div className="rounded-xl border border-white/[0.08] bg-white/[0.045] px-4 py-10 text-center text-sm text-white/40">
                  暂无用户数据
                </div>
              ) : (
                data.map((user) => (
                  <div
                    key={user.id}
                    className="flex items-center gap-3 rounded-xl border border-white/[0.08] bg-white/[0.045] p-3"
                  >
                    <Checkbox
                      checked={selectedRowKeys.includes(user.id)}
                      onChange={(e) =>
                        setSelectedRowKeys((prev) =>
                          e.target.checked ? [...prev, user.id] : prev.filter((k) => k !== user.id)
                        )
                      }
                    />
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="truncate font-medium text-white/90">{user.username}</span>
                        {user.roleName ? (
                          <Tag color={getRoleTagColor(user.roleName)} bordered={false}>
                            {ROLE_LABELS[user.roleName] || user.roleName}
                          </Tag>
                        ) : (
                          <Tag bordered={false}>-</Tag>
                        )}
                      </div>
                      <div className="mt-0.5 truncate text-xs text-white/40">
                        {user.direction
                          ? DIRECTION_LABELS[user.direction as keyof typeof DIRECTION_LABELS] ||
                            user.direction
                          : '-'}{' '}
                        · {user.email || '-'}
                      </div>
                      <div className="mt-1">{renderGithubStatus(user)}</div>
                    </div>
                    <Button
                      type="primary"
                      size="small"
                      icon={<SendOutlined />}
                      loading={invitingUserIds.includes(user.id)}
                      onClick={() => handleInvite(user)}
                      className="shrink-0"
                    >
                      邀请
                    </Button>
                  </div>
                ))
              )}
            </div>
          ) : (
            <Table<AdminUserListItemDTO>
              rowKey="id"
              columns={columns}
              dataSource={data}
              pagination={false}
              rowSelection={{
                selectedRowKeys,
                onChange: setSelectedRowKeys,
              }}
            />
          )}
          <div style={{ marginTop: 16, textAlign: 'right' }}>
            <Pagination
              current={currentPage + 1}
              total={total}
              pageSize={PAGE_SIZE}
              onChange={(page) => setCurrentPage(page - 1)}
              showSizeChanger={false}
              showTotal={(count) => `共 ${count} 个用户`}
            />
          </div>
        </Spin>
      </Card>

      <Modal
        title="批量邀请结果"
        open={batchResult !== null}
        footer={
          <Button type="primary" onClick={() => setBatchResult(null)}>
            关闭
          </Button>
        }
        onCancel={() => setBatchResult(null)}
      >
        {batchResult && (
          <>
            <Space size="large" style={{ marginBottom: 16 }}>
              <span>总数：{batchResult.total}</span>
              <span style={{ color: '#52c41a' }}>成功：{batchResult.succeeded}</span>
              <span style={{ color: '#ff4d4f' }}>失败：{batchResult.failed}</span>
            </Space>
            <Table
              rowKey="userId"
              size="small"
              pagination={false}
              dataSource={batchResult.details}
              columns={[
                {
                  title: '用户',
                  key: 'user',
                  render: (_, record) => userNameMap.get(record.userId) || `ID: ${record.userId}`,
                },
                {
                  title: '结果',
                  dataIndex: 'success',
                  key: 'success',
                  render: (success: boolean) =>
                    success ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
                },
                {
                  title: '说明',
                  dataIndex: 'reason',
                  key: 'reason',
                },
              ]}
            />
          </>
        )}
      </Modal>
    </div>
  )
}
