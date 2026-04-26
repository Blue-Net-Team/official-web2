'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { App, Button, Grid, Modal, Spin, Table } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { VenueDTO } from '@/apis/schema/type'
import { VenueService } from '@/apis/services/venue.service'
import { adminVenueService } from '@/apis/services/admin-venue.service'
import VenueDrawer, { type DrawerMode } from './VenueDrawer'

const { useBreakpoint } = Grid

export default function VenueManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // Data state
  const [venues, setVenues] = useState<VenueDTO[]>([])
  const [loading, setLoading] = useState(false)

  // Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedVenue, setSelectedVenue] = useState<VenueDTO | null>(null)

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingVenue, setDeletingVenue] = useState<VenueDTO | null>(null)

  const fetchVenues = useCallback(async () => {
    setLoading(true)
    try {
      const res = await VenueService.getAllVenues()
      if (res.code === 200 && res.data) {
        setVenues(res.data)
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchVenues()
  }, [fetchVenues])

  // Open drawer for viewing
  const handleRowClick = (record: VenueDTO) => {
    setSelectedVenue(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }

  // Open drawer for creating
  const handleCreate = () => {
    setSelectedVenue(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  // Delete flow
  const handleDeleteClick = (venue: VenueDTO) => {
    setDeletingVenue(venue)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingVenue) return
    try {
      await adminVenueService.delete(deletingVenue.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      fetchVenues()
    } catch {
      messageApi.error('删除失败')
    }
  }

  // Drawer success callback
  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    fetchVenues()
  }

  // Table columns
  const columns: ColumnsType<VenueDTO> = useMemo(() => {
    const cols: ColumnsType<VenueDTO> = [
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
        ellipsis: true,
      },
    ]

    if (!isMobile) {
      cols.push(
        {
          title: '副标题',
          dataIndex: 'subtitle',
          key: 'subtitle',
          ellipsis: true,
          render: (subtitle: string | null) => subtitle || '-',
        },
        {
          title: '描述',
          dataIndex: 'description',
          key: 'description',
          ellipsis: true,
          render: (desc: string | null) => desc || '-',
        }
      )
    }

    return cols
  }, [isMobile])

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">场地管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建场地
        </Button>
      </div>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          dataSource={venues}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          onRow={(record) => ({
            onClick: () => handleRowClick(record),
            className: 'cursor-pointer',
          })}
          locale={{ emptyText: '暂无场地数据' }}
        />
      </Spin>

      {/* Venue Drawer */}
      <VenueDrawer
        open={drawerOpen}
        venue={selectedVenue}
        mode={drawerMode}
        onClose={() => setDrawerOpen(false)}
        onSuccess={handleDrawerSuccess}
        onDelete={handleDeleteClick}
        onEdit={() => setDrawerMode('edit')}
      />

      {/* Delete confirmation modal */}
      <Modal
        title="确认删除"
        open={deleteModalOpen}
        onOk={handleDeleteConfirm}
        onCancel={() => setDeleteModalOpen(false)}
        okText="确认删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确认删除场地「{deletingVenue?.name}」？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}
