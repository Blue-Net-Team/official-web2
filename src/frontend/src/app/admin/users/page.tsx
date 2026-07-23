'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  App,
  Button,
  Descriptions,
  Drawer,
  Form,
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
import {
  DeleteOutlined,
  EyeOutlined,
  LockOutlined,
  EditOutlined,
  StopOutlined,
  CheckCircleOutlined,
  TeamOutlined,
  PlusOutlined,
} from '@ant-design/icons'
import { usePagination, useApi, useAuth } from '@/hooks'
import { hashPassword } from '@/utils/passwordHash'
import { adminUserService } from '@/apis/services/admin-user.service'
import { collegeService } from '@/apis/services/college.service'
import {
  DIRECTION_LABELS,
  ROLE_LABELS,
  GENDER_LABELS,
  getRoleTagColor,
} from '@/apis/schema/enumerate'
import type { AdminUserListItemDTO, CollegeDTO } from '@/apis/schema/type'

const PAGE_SIZE = 15

/** 固定角色选项（与数据库自增ID对应） */
const ROLE_OPTIONS = [
  { value: 1, label: ROLE_LABELS.SUPER_ADMIN },
  { value: 2, label: ROLE_LABELS.DIRECTION_ADMIN },
  { value: 3, label: ROLE_LABELS.MEMBER },
  { value: 4, label: ROLE_LABELS.CANDIDATE },
]

const DIRECTION_OPTIONS = Object.entries(DIRECTION_LABELS).map(([value, label]) => ({
  value,
  label,
}))

interface FilterValues {
  roleId?: number
  direction?: string
  collegeId?: number
  keyword: string
}

export default function AdminUserManagementPage() {
  const { message: messageApi, modal } = App.useApp()
  const { userInfo } = useAuth()

  // Filters
  const [filters, setFilters] = useState<FilterValues>({
    keyword: '',
  })
  const [colleges, setColleges] = useState<CollegeDTO[]>([])

  // Table selection
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])

  // Detail drawer
  const [detailOpen, setDetailOpen] = useState(false)

  // Edit modal
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [editingUser, setEditingUser] = useState<AdminUserListItemDTO | null>(null)
  const [editForm] = Form.useForm()
  const [editSubmitting, setEditSubmitting] = useState(false)

  // Reset password modal
  const [resetModalOpen, setResetModalOpen] = useState(false)
  const [resettingUser, setResettingUser] = useState<AdminUserListItemDTO | null>(null)
  const [resetForm] = Form.useForm()
  const [resetSubmitting, setResetSubmitting] = useState(false)

  // Delete modal
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingUser, setDeletingUser] = useState<AdminUserListItemDTO | null>(null)
  const [deleteSubmitting, setDeleteSubmitting] = useState(false)

  // Batch role modal
  const [batchRoleModalOpen, setBatchRoleModalOpen] = useState(false)
  const [batchRoleId, setBatchRoleId] = useState<number | undefined>()
  const [batchSubmitting, setBatchSubmitting] = useState(false)

  // Create user modal
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [createForm] = Form.useForm()
  const [createSubmitting, setCreateSubmitting] = useState(false)

  // Fetch colleges
  useEffect(() => {
    collegeService.getColleges().then((res) => {
      if (res.data) setColleges(res.data)
    })
  }, [])

  const collegeOptions = useMemo(
    () => colleges.map((c) => ({ value: c.id, label: c.name })),
    [colleges]
  )

  // Pagination
  const fetchPage = useCallback(
    (page: number, pageSize: number) => {
      const params: Record<string, unknown> = { page, size: pageSize }
      if (filters.roleId !== undefined) params.roleId = filters.roleId
      if (filters.direction) params.direction = filters.direction
      if (filters.collegeId !== undefined) params.collegeId = filters.collegeId
      if (filters.keyword) params.keyword = filters.keyword
      return adminUserService.getList(params as Parameters<typeof adminUserService.getList>[0])
    },
    [filters]
  )

  const { data, total, loading, currentPage, setCurrentPage, refresh } = usePagination(fetchPage, {
    pageSize: PAGE_SIZE,
  })

  // Detail API
  const {
    data: detailData,
    loading: detailLoading,
    execute: fetchDetail,
    reset: resetDetail,
  } = useApi(adminUserService.getDetail.bind(adminUserService))

  // Open detail
  const handleViewDetail = async (user: AdminUserListItemDTO) => {
    setDetailOpen(true)
    resetDetail()
    try {
      await fetchDetail(user.id)
    } catch {
      messageApi.error('获取用户详情失败')
    }
  }

  // Open edit
  const handleEdit = (user: AdminUserListItemDTO) => {
    setEditingUser(user)
    editForm.setFieldsValue({
      roleId: user.roleId ?? undefined,
      direction: user.direction ?? undefined,
      disable: user.disable,
      job: user.job ?? undefined,
      studentId: user.studentId ?? undefined,
      username: user.username ?? undefined,
      nickname: user.nickname ?? undefined,
      email: user.email ?? undefined,
      collegeId: user.collegeId ?? undefined,
      major: user.major ?? undefined,
      gender: user.gender ?? undefined,
      assessmentGradeYear: user.assessmentGradeYear ?? undefined,
    })
    setEditModalOpen(true)
  }

  // Submit edit
  const handleEditSubmit = async () => {
    if (!editingUser) return
    const values = await editForm.validateFields()
    setEditSubmitting(true)
    try {
      const res = await adminUserService.update(editingUser.id, values)
      if (res.code === 200) {
        messageApi.success('更新成功')
        setEditModalOpen(false)
        refresh()
      } else {
        messageApi.error(res.msg || '更新失败')
      }
    } catch {
      messageApi.error('更新失败')
    } finally {
      setEditSubmitting(false)
    }
  }

  // Open reset password
  const handleResetPassword = (user: AdminUserListItemDTO) => {
    setResettingUser(user)
    resetForm.resetFields()
    setResetModalOpen(true)
  }

  // Submit reset password
  const handleResetSubmit = async () => {
    if (!resettingUser) return
    const values = await resetForm.validateFields()
    setResetSubmitting(true)
    try {
      const res = await adminUserService.resetPassword(resettingUser.id, {
        newPassword: hashPassword(values.newPassword),
        confirmPassword: hashPassword(values.confirmPassword),
      })
      if (res.code === 200) {
        messageApi.success('密码已重置')
        setResetModalOpen(false)
      } else {
        messageApi.error(res.msg || '重置失败')
      }
    } catch {
      messageApi.error('重置失败')
    } finally {
      setResetSubmitting(false)
    }
  }

  const isProtectedUser = (user: AdminUserListItemDTO) => {
    if (user.studentId === '000000000000') return true
    if (userInfo && user.id === userInfo.id) return true
    return false
  }

  // Open delete
  const handleDelete = (user: AdminUserListItemDTO) => {
    if (isProtectedUser(user)) {
      messageApi.warning(
        user.studentId === '000000000000' ? '系统账号不可删除' : '不能删除自己的账号'
      )
      return
    }
    setDeletingUser(user)
    setDeleteModalOpen(true)
  }

  // Confirm delete
  const handleDeleteConfirm = async () => {
    if (!deletingUser) return
    setDeleteSubmitting(true)
    try {
      const res = await adminUserService.delete(deletingUser.id)
      if (res.code === 200) {
        messageApi.success('删除成功')
        setDeleteModalOpen(false)
        refresh()
      } else {
        messageApi.error(res.msg || '删除失败')
      }
    } catch {
      messageApi.error('删除失败')
    } finally {
      setDeleteSubmitting(false)
    }
  }

  // Batch delete
  const handleBatchDelete = async () => {
    const ids = selectedRowKeys.map(Number)
    if (ids.length === 0) return
    const protectedUsers = data?.filter((u) => selectedRowKeys.includes(u.id) && isProtectedUser(u))
    if (protectedUsers && protectedUsers.length > 0) {
      const names = protectedUsers.map((u) => u.username).join('、')
      messageApi.warning(`选中用户中包含不可删除账号：${names}`)
      return
    }
    modal.confirm({
      title: `确认批量删除 ${ids.length} 个用户？`,
      content: '此操作将物理删除用户及其所有关联数据，不可撤销。',
      okText: '确认删除',
      cancelText: '取消',
      okButtonProps: { danger: true, loading: batchSubmitting },
      onOk: async () => {
        setBatchSubmitting(true)
        try {
          const res = await adminUserService.batchDelete({ userIds: ids })
          if (res.code === 200) {
            messageApi.success('批量删除成功')
            setSelectedRowKeys([])
            refresh()
          } else {
            messageApi.error(res.msg || '批量删除失败')
          }
        } catch {
          messageApi.error('批量删除失败')
        } finally {
          setBatchSubmitting(false)
        }
      },
    })
  }

  // Batch disable
  const handleBatchDisable = async (disable: boolean) => {
    const ids = selectedRowKeys.map(Number)
    if (ids.length === 0) return
    setBatchSubmitting(true)
    try {
      const service = disable ? adminUserService.batchDisable : adminUserService.batchEnable
      const res = await service({ userIds: ids })
      if (res.code === 200) {
        messageApi.success(disable ? '批量禁用成功' : '批量启用成功')
        setSelectedRowKeys([])
        refresh()
      } else {
        messageApi.error(res.msg || '操作失败')
      }
    } catch {
      messageApi.error('操作失败')
    } finally {
      setBatchSubmitting(false)
    }
  }

  // Batch update role
  const handleBatchRole = () => {
    const ids = selectedRowKeys.map(Number)
    if (ids.length === 0) return
    setBatchRoleId(undefined)
    setBatchRoleModalOpen(true)
  }

  const handleBatchRoleSubmit = async () => {
    if (batchRoleId === undefined) {
      messageApi.warning('请选择角色')
      return
    }
    const ids = selectedRowKeys.map(Number)
    setBatchSubmitting(true)
    try {
      const res = await adminUserService.batchUpdateRole({ userIds: ids, roleId: batchRoleId })
      if (res.code === 200) {
        messageApi.success('批量修改角色成功')
        setBatchRoleModalOpen(false)
        setSelectedRowKeys([])
        refresh()
      } else {
        messageApi.error(res.msg || '操作失败')
      }
    } catch {
      messageApi.error('操作失败')
    } finally {
      setBatchSubmitting(false)
    }
  }

  // Create user
  const handleCreateOpen = () => {
    createForm.resetFields()
    setCreateModalOpen(true)
  }

  const handleCreateSubmit = async () => {
    const values = await createForm.validateFields()
    setCreateSubmitting(true)
    try {
      const hashedPassword = hashPassword(values.password)
      const res = await adminUserService.create({
        studentId: values.studentId,
        email: values.email,
        username: values.username,
        password: hashedPassword,
        nickname: values.nickname,
        roleId: values.roleId,
        collegeId: values.collegeId,
        major: values.major,
        direction: values.direction,
        gender: values.gender,
        job: values.job,
        assessmentGradeYear: values.assessmentGradeYear,
      })
      if (res.code === 200) {
        messageApi.success('用户创建成功')
        setCreateModalOpen(false)
        refresh()
      } else {
        messageApi.error(res.msg || '创建失败')
      }
    } catch {
      messageApi.error('创建失败')
    } finally {
      setCreateSubmitting(false)
    }
  }

  // Filter change
  const handleFilterChange = (next: Partial<FilterValues>) => {
    setFilters((prev) => ({ ...prev, ...next }))
    setCurrentPage(0)
  }

  // Table columns
  const columns: TableColumnsType<AdminUserListItemDTO> = [
    {
      title: '学号',
      dataIndex: 'studentId',
      width: 130,
      ellipsis: true,
    },
    {
      title: '姓名',
      dataIndex: 'username',
      width: 100,
      ellipsis: true,
    },
    {
      title: '昵称',
      dataIndex: 'nickname',
      width: 120,
      ellipsis: true,
      render: (v: string | null) => v || '-',
    },
    {
      title: '角色',
      key: 'roleId',
      dataIndex: 'roleName',
      width: 110,
      filters: ROLE_OPTIONS.map((opt) => ({ text: opt.label, value: opt.value })),
      filterMultiple: false,
      filteredValue: filters.roleId !== undefined ? [filters.roleId] : undefined,
      render: (v: string | null) =>
        v ? (
          <Tag color={getRoleTagColor(v)} bordered={false}>
            {ROLE_LABELS[v] || v}
          </Tag>
        ) : (
          <Tag bordered={false}>-</Tag>
        ),
    },
    {
      title: '方向',
      key: 'direction',
      dataIndex: 'direction',
      width: 120,
      filters: DIRECTION_OPTIONS.map((opt) => ({ text: opt.label, value: opt.value })),
      filterMultiple: false,
      filteredValue: filters.direction ? [filters.direction] : undefined,
      render: (v: string | null) =>
        v ? DIRECTION_LABELS[v as keyof typeof DIRECTION_LABELS] || v : '-',
    },
    {
      title: '学院',
      key: 'collegeId',
      dataIndex: 'college',
      width: 160,
      ellipsis: true,
      filters: collegeOptions.map((opt) => ({ text: opt.label, value: opt.value })),
      filterMultiple: false,
      filteredValue: filters.collegeId !== undefined ? [filters.collegeId] : undefined,
      render: (v: string | null) => v || '-',
    },
    {
      title: '性别',
      dataIndex: 'gender',
      width: 70,
      render: (v: string | null) => (v ? GENDER_LABELS[v as keyof typeof GENDER_LABELS] || v : '-'),
    },
    {
      title: '考核年级',
      dataIndex: 'assessmentGradeYear',
      width: 100,
      render: (v: number | null) => v ?? '-',
    },
    {
      title: '状态',
      dataIndex: 'disable',
      width: 80,
      render: (v: boolean) => (v ? <Tag color="red">已禁用</Tag> : <Tag color="green">正常</Tag>),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="text"
            size="small"
            icon={<LockOutlined />}
            onClick={() => handleResetPassword(record)}
          >
            密码
          </Button>
          <Button
            type="text"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ]

  const hasSelected = selectedRowKeys.length > 0

  return (
    <div className="flex flex-col gap-4">
      {/* Header & Filters */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-lg font-medium text-white/90 m-0">用户管理</h2>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <Input.Search
          allowClear
          placeholder="搜索学号 / 姓名"
          style={{ width: 220 }}
          value={filters.keyword}
          onChange={(e) => handleFilterChange({ keyword: e.target.value })}
          onSearch={() => setCurrentPage(0)}
        />
      </div>

      {/* Batch operations */}
      <div className="flex flex-wrap items-center gap-3">
        <Button
          danger
          disabled={!hasSelected || batchSubmitting}
          loading={batchSubmitting}
          icon={<DeleteOutlined />}
          onClick={handleBatchDelete}
        >
          批量删除{hasSelected ? ` (${selectedRowKeys.length})` : ''}
        </Button>
        <Button
          disabled={!hasSelected || batchSubmitting}
          loading={batchSubmitting}
          icon={<StopOutlined />}
          onClick={() => handleBatchDisable(true)}
        >
          批量禁用{hasSelected ? ` (${selectedRowKeys.length})` : ''}
        </Button>
        <Button
          disabled={!hasSelected || batchSubmitting}
          loading={batchSubmitting}
          icon={<CheckCircleOutlined />}
          onClick={() => handleBatchDisable(false)}
        >
          批量启用{hasSelected ? ` (${selectedRowKeys.length})` : ''}
        </Button>
        <Button
          disabled={!hasSelected || batchSubmitting}
          loading={batchSubmitting}
          icon={<TeamOutlined />}
          onClick={handleBatchRole}
        >
          批量改角色{hasSelected ? ` (${selectedRowKeys.length})` : ''}
        </Button>
        <div className="flex-1" />
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateOpen}>
          添加用户
        </Button>
      </div>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          rowSelection={{
            selectedRowKeys,
            onChange: setSelectedRowKeys,
          }}
          columns={columns}
          dataSource={data}
          rowKey="id"
          size="small"
          pagination={false}
          scroll={{ x: 'max-content' }}
          locale={{ emptyText: '暂无用户数据' }}
          onChange={(_, tableFilters) => {
            setFilters((prev) => ({
              ...prev,
              roleId: tableFilters.roleId?.[0] as number | undefined,
              direction: tableFilters.direction?.[0] as string | undefined,
              collegeId: tableFilters.collegeId?.[0] as number | undefined,
            }))
            setCurrentPage(0)
          }}
        />
      </Spin>

      {/* Pagination */}
      {total > PAGE_SIZE && (
        <div className="flex justify-center">
          <Pagination
            current={currentPage + 1}
            total={total}
            pageSize={PAGE_SIZE}
            showSizeChanger={false}
            onChange={(p) => setCurrentPage(p - 1)}
          />
        </div>
      )}

      {/* Detail Drawer */}
      <Drawer title="用户详情" open={detailOpen} onClose={() => setDetailOpen(false)} width={480}>
        <Spin spinning={detailLoading}>
          {detailData && (
            <Descriptions column={1} size="small" bordered>
              <Descriptions.Item label="ID">{detailData.id}</Descriptions.Item>
              <Descriptions.Item label="学号">{detailData.studentId}</Descriptions.Item>
              <Descriptions.Item label="姓名">{detailData.username}</Descriptions.Item>
              <Descriptions.Item label="昵称">{detailData.nickname || '-'}</Descriptions.Item>
              <Descriptions.Item label="邮箱">{detailData.email || '-'}</Descriptions.Item>
              <Descriptions.Item label="角色">
                {detailData.roleName ? (
                  <Tag color={getRoleTagColor(detailData.roleName)}>
                    {ROLE_LABELS[detailData.roleName] || detailData.roleName}
                  </Tag>
                ) : (
                  '-'
                )}
              </Descriptions.Item>
              <Descriptions.Item label="方向">
                {detailData.direction
                  ? DIRECTION_LABELS[detailData.direction as keyof typeof DIRECTION_LABELS] ||
                    detailData.direction
                  : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="学院">{detailData.college || '-'}</Descriptions.Item>
              <Descriptions.Item label="专业">{detailData.major || '-'}</Descriptions.Item>
              <Descriptions.Item label="性别">
                {detailData.gender
                  ? GENDER_LABELS[detailData.gender as keyof typeof GENDER_LABELS] ||
                    detailData.gender
                  : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="职位">{detailData.job || '-'}</Descriptions.Item>
              <Descriptions.Item label="考核年级">
                {detailData.assessmentGradeYear ?? '-'}
              </Descriptions.Item>
              <Descriptions.Item label="GitHub">
                {detailData.githubUsername || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {detailData.disable ? <Tag color="red">已禁用</Tag> : <Tag color="green">正常</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="经历数">{detailData.experienceCount}</Descriptions.Item>
              <Descriptions.Item label="成就数">{detailData.achievementCount}</Descriptions.Item>
              <Descriptions.Item label="答题数">{detailData.answerCount}</Descriptions.Item>
              <Descriptions.Item label="评论数">{detailData.commentCount}</Descriptions.Item>
              <Descriptions.Item label="个人简介">
                <div className="whitespace-pre-wrap">{detailData.bio || '-'}</div>
              </Descriptions.Item>
            </Descriptions>
          )}
        </Spin>
      </Drawer>

      {/* Edit Modal */}
      <Modal
        title={`编辑用户 - ${editingUser?.username}`}
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={() => setEditModalOpen(false)}
        okText="保存"
        cancelText="取消"
        confirmLoading={editSubmitting}
        width={520}
      >
        <Form form={editForm} layout="vertical" className="mt-4">
          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="studentId"
              label="学号"
              rules={[{ pattern: /^\d{12,13}$/, message: '学号必须为12-13位数字' }]}
            >
              <Input placeholder="学号" />
            </Form.Item>
            <Form.Item name="username" label="姓名">
              <Input placeholder="姓名" />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="nickname" label="昵称">
              <Input placeholder="昵称" />
            </Form.Item>
            <Form.Item
              name="email"
              label="邮箱"
              rules={[{ type: 'email', message: '邮箱格式不正确' }]}
            >
              <Input placeholder="邮箱地址" />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="roleId" label="角色">
              <Select allowClear placeholder="选择角色" options={ROLE_OPTIONS} />
            </Form.Item>
            <Form.Item name="direction" label="方向">
              <Select allowClear placeholder="选择方向" options={DIRECTION_OPTIONS} />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="collegeId" label="学院">
              <Select allowClear placeholder="选择学院" options={collegeOptions} />
            </Form.Item>
            <Form.Item name="major" label="专业">
              <Input placeholder="专业" />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="gender" label="性别">
              <Select
                allowClear
                placeholder="选择性别"
                options={[
                  { value: 'MALE', label: GENDER_LABELS.MALE },
                  { value: 'FEMALE', label: GENDER_LABELS.FEMALE },
                  { value: 'UNKNOWN', label: GENDER_LABELS.UNKNOWN },
                ]}
              />
            </Form.Item>
            <Form.Item name="job" label="岗位">
              <Input placeholder="岗位" />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="assessmentGradeYear" label="考核年级">
              <Input type="number" placeholder="考核年级年份" />
            </Form.Item>
            <Form.Item name="disable" label="状态">
              <Select
                placeholder="选择状态"
                options={[
                  { value: false, label: '正常' },
                  { value: true, label: '禁用' },
                ]}
              />
            </Form.Item>
          </div>
        </Form>
      </Modal>

      {/* Reset Password Modal */}
      <Modal
        title={`重置密码 - ${resettingUser?.username}`}
        open={resetModalOpen}
        onOk={handleResetSubmit}
        onCancel={() => setResetModalOpen(false)}
        okText="确认重置"
        cancelText="取消"
        confirmLoading={resetSubmitting}
      >
        <Form form={resetForm} layout="vertical" className="mt-4">
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码至少6位' },
            ]}
          >
            <Input.Password placeholder="输入新密码" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认密码"
            rules={[
              { required: true, message: '请再次输入密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'))
                },
              }),
            ]}
          >
            <Input.Password placeholder="再次输入新密码" />
          </Form.Item>
        </Form>
      </Modal>

      {/* Delete Modal */}
      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        okText="确认删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
        confirmLoading={deleteSubmitting}
      >
        <p>
          确认删除用户「{deletingUser?.username}（{deletingUser?.studentId}）」？
        </p>
        <p className="text-red-500">此操作将物理删除该用户及其所有关联数据，不可撤销。</p>
      </Modal>

      {/* Batch Role Modal */}
      <Modal
        title={`批量修改角色 (${selectedRowKeys.length} 人)`}
        open={batchRoleModalOpen}
        onOk={handleBatchRoleSubmit}
        onCancel={() => setBatchRoleModalOpen(false)}
        okText="确认修改"
        cancelText="取消"
        confirmLoading={batchSubmitting}
      >
        <div className="mt-4">
          <Select
            className="w-full"
            placeholder="选择目标角色"
            value={batchRoleId}
            onChange={setBatchRoleId}
            options={ROLE_OPTIONS}
          />
        </div>
      </Modal>

      {/* Create User Modal */}
      <Modal
        title="添加用户"
        open={createModalOpen}
        onOk={handleCreateSubmit}
        onCancel={() => setCreateModalOpen(false)}
        okText="创建"
        cancelText="取消"
        confirmLoading={createSubmitting}
        width={520}
      >
        <Form form={createForm} layout="vertical" className="mt-4">
          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="studentId"
              label="学号"
              rules={[
                { required: true, message: '请输入学号' },
                { pattern: /^\d{12,13}$/, message: '学号必须为12-13位数字' },
              ]}
            >
              <Input placeholder="学号" />
            </Form.Item>
            <Form.Item
              name="username"
              label="姓名"
              rules={[{ required: true, message: '请输入姓名' }]}
            >
              <Input placeholder="姓名" />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="password"
              label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6位' },
              ]}
            >
              <Input.Password placeholder="初始密码" />
            </Form.Item>
            <Form.Item
              name="confirmPassword"
              label="确认密码"
              rules={[
                { required: true, message: '请再次输入密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve()
                    }
                    return Promise.reject(new Error('两次输入的密码不一致'))
                  },
                }),
              ]}
            >
              <Input.Password placeholder="再次输入密码" />
            </Form.Item>
          </div>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '邮箱格式不正确' },
            ]}
          >
            <Input placeholder="邮箱地址" />
          </Form.Item>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="nickname" label="昵称">
              <Input placeholder="昵称（可选）" />
            </Form.Item>
            <Form.Item
              name="roleId"
              label="角色"
              rules={[{ required: true, message: '请选择角色' }]}
              initialValue={4}
            >
              <Select placeholder="选择角色" options={ROLE_OPTIONS} />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="collegeId" label="学院">
              <Select allowClear placeholder="选择学院" options={collegeOptions} />
            </Form.Item>
            <Form.Item name="major" label="专业">
              <Input placeholder="专业（可选）" />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="direction" label="方向">
              <Select allowClear placeholder="选择方向" options={DIRECTION_OPTIONS} />
            </Form.Item>
            <Form.Item name="gender" label="性别">
              <Select
                allowClear
                placeholder="选择性别"
                options={[
                  { value: 'MALE', label: GENDER_LABELS.MALE },
                  { value: 'FEMALE', label: GENDER_LABELS.FEMALE },
                  { value: 'UNKNOWN', label: GENDER_LABELS.UNKNOWN },
                ]}
              />
            </Form.Item>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="job" label="岗位">
              <Input placeholder="岗位（可选）" />
            </Form.Item>
            <Form.Item name="assessmentGradeYear" label="考核年级">
              <Input type="number" placeholder="考核年级年份（可选）" />
            </Form.Item>
          </div>
        </Form>
      </Modal>
    </div>
  )
}
