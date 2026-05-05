'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Modal, Spin, Table } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { CollegeDTO } from '@/apis/schema/type'
import { collegeService } from '@/apis/services/college.service'
import { adminCollegeService } from '@/apis/services/admin-college.service'
import CollegeDrawer, { type DrawerMode } from './CollegeDrawer'

export default function CollegeManagementPage() {
  const { message: messageApi } = App.useApp()

  const [colleges, setColleges] = useState<CollegeDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedCollege, setSelectedCollege] = useState<CollegeDTO | null>(null)
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingCollege, setDeletingCollege] = useState<CollegeDTO | null>(null)

  const fetchColleges = useCallback(async () => {
    setLoading(true)
    try {
      const res = await collegeService.getColleges()
      if (res.code === 200 && res.data) {
        setColleges(res.data)
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchColleges()
  }, [fetchColleges])

  const handleRowClick = (record: CollegeDTO) => {
    setSelectedCollege(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }

  const handleCreate = () => {
    setSelectedCollege(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  const handleDeleteClick = (college: CollegeDTO) => {
    setDeletingCollege(college)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingCollege) return
    try {
      await adminCollegeService.delete(deletingCollege.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      fetchColleges()
    } catch (err: any) {
      messageApi.error(err?.response?.data?.msg || '删除失败')
    }
  }

  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    messageApi.success(drawerMode === 'create' ? '创建成功' : '更新成功')
    fetchColleges()
  }

  const columns: ColumnsType<CollegeDTO> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '学院名称', dataIndex: 'name', key: 'name', ellipsis: true },
  ]

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">学院管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新增学院
        </Button>
      </div>

      <Spin spinning={loading}>
        <Table
          dataSource={colleges}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          onRow={(record) => ({ onClick: () => handleRowClick(record), className: 'cursor-pointer' })}
          locale={{ emptyText: '暂无学院数据' }}
        />
      </Spin>

      <CollegeDrawer
        open={drawerOpen}
        college={selectedCollege}
        mode={drawerMode}
        onClose={() => setDrawerOpen(false)}
        onSuccess={handleDrawerSuccess}
        onDelete={handleDeleteClick}
        onEdit={() => setDrawerMode('edit')}
      />

      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        okText="确认删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确认删除学院「{deletingCollege?.name}」吗？</p>
        <p className="text-gray-400 text-sm">若该学院下有关联用户或报名记录，则无法删除。</p>
      </Modal>
    </div>
  )
}
