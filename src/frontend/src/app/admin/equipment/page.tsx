'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { App, Button, Grid, Modal, Spin, Table } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { EquipmentDTO } from '@/apis/schema/type'
import { EquipmentService } from '@/apis/services/equipment.service'
import { adminEquipmentService } from '@/apis/services/admin-equipment.service'
import EquipmentDrawer, { type DrawerMode } from './EquipmentDrawer'

const { useBreakpoint } = Grid

export default function EquipmentManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // Data state
  const [equipments, setEquipments] = useState<EquipmentDTO[]>([])
  const [loading, setLoading] = useState(false)

  // Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedEquipment, setSelectedEquipment] = useState<EquipmentDTO | null>(null)

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingEquipment, setDeletingEquipment] = useState<EquipmentDTO | null>(null)

  const fetchEquipments = useCallback(async () => {
    setLoading(true)
    try {
      const res = await EquipmentService.getAllEquipments()
      if (res.code === 200 && res.data) {
        setEquipments(res.data)
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchEquipments()
  }, [fetchEquipments])

  // Open drawer for viewing
  const handleRowClick = (record: EquipmentDTO) => {
    setSelectedEquipment(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }

  // Open drawer for creating
  const handleCreate = () => {
    setSelectedEquipment(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  // Delete flow
  const handleDeleteClick = (equipment: EquipmentDTO) => {
    setDeletingEquipment(equipment)
    setDeleteModalOpen(true)
  }

  const handleDeleteConfirm = async () => {
    if (!deletingEquipment) return
    try {
      await adminEquipmentService.delete(deletingEquipment.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      fetchEquipments()
    } catch {
      messageApi.error('删除失败')
    }
  }

  // Drawer success callback
  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    fetchEquipments()
  }

  // Table columns
  const columns: ColumnsType<EquipmentDTO> = useMemo(() => {
    const cols: ColumnsType<EquipmentDTO> = [
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
          title: '品牌',
          dataIndex: 'brand',
          key: 'brand',
          width: 120,
          ellipsis: true,
          render: (brand: string | null) => brand || '-',
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
        <h2 className="text-lg font-medium text-white/90 m-0">设备管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建设备
        </Button>
      </div>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          dataSource={equipments}
          columns={columns}
          rowKey={(record) => String(record.id)}
          size="small"
          pagination={false}
          onRow={(record) => ({
            onClick: () => handleRowClick(record),
            className: 'cursor-pointer',
          })}
          locale={{ emptyText: '暂无设备数据' }}
        />
      </Spin>

      {/* Equipment Drawer */}
      <EquipmentDrawer
        open={drawerOpen}
        equipment={selectedEquipment}
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
        <p>确认删除设备「{deletingEquipment?.name}」？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}
