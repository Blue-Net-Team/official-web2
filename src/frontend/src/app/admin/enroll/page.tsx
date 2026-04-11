'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Input, Modal, Pagination, Row, Col, Spin } from 'antd'
import {
  StatisticsCards,
  FilterBar,
  EnrollmentCard,
  EnrollmentDrawer,
} from '@/components/Admin/EnrollManagement'
import type { FilterValues } from '@/components/Admin/EnrollManagement'
import { adminEnrollService } from '@/apis/services/admin-enroll.service'
import type {
  EnrollmentBriefDTO,
  EnrollmentDetailDTO,
  EnrollmentStatisticsDTO,
} from '@/apis/schema/type'

const PAGE_SIZE = 12

export default function EnrollManagementPage() {
  const { message: messageApi } = App.useApp()

  // Data state
  const [list, setList] = useState<EnrollmentBriefDTO[]>([])
  const [statistics, setStatistics] = useState<EnrollmentStatisticsDTO | null>(null)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)

  // Pagination & filters
  const [page, setPage] = useState(1)
  const [filters, setFilters] = useState<FilterValues>({
    keyword: '',
    status: undefined,
    direction: undefined,
  })

  // Drawer
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerDetail, setDrawerDetail] = useState<EnrollmentDetailDTO | null>(null)
  const [drawerLoading, setDrawerLoading] = useState(false)

  // Reject modal
  const [rejectModalOpen, setRejectModalOpen] = useState(false)
  const [rejectingId, setRejectingId] = useState<number | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  // Fetch list
  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const params: Record<string, unknown> = {
        page: page - 1,
        size: PAGE_SIZE,
      }
      if (filters.keyword) params.keyword = filters.keyword
      if (filters.status) params.status = filters.status
      if (filters.direction) params.direction = filters.direction

      const res = await adminEnrollService.getList(
        params as Parameters<typeof adminEnrollService.getList>[0]
      )
      const data = res.data
      if (data) {
        setList(data.content)
        setTotalElements(data.totalElements)
      }
    } finally {
      setLoading(false)
    }
  }, [page, filters])

  // Fetch statistics
  const fetchStatistics = useCallback(async () => {
    const res = await adminEnrollService.getStatistics()
    if (res.data) setStatistics(res.data)
  }, [])

  // Initial load & refetch on filter/page change
  useEffect(() => {
    Promise.all([fetchList(), fetchStatistics()])
  }, [fetchList, fetchStatistics])

  // Filter change handler
  const handleFilterChange = (next: FilterValues) => {
    setFilters(next)
    setPage(1)
  }

  // Open drawer
  const handleCardClick = async (enrollment: EnrollmentBriefDTO) => {
    setDrawerOpen(true)
    setDrawerDetail(null)
    setDrawerLoading(true)
    try {
      const res = await adminEnrollService.getDetail(enrollment.id)
      if (res.data) setDrawerDetail(res.data)
    } finally {
      setDrawerLoading(false)
    }
  }

  // Approve
  const handleApprove = async (id: number) => {
    const res = await adminEnrollService.approve(id)
    if (res.data) {
      messageApi.success('已通过')
      setDrawerOpen(false)
      await Promise.all([fetchList(), fetchStatistics()])
    }
  }

  // Reject flow
  const handleRejectClick = (id: number) => {
    setRejectingId(id)
    setRejectReason('')
    setRejectModalOpen(true)
  }

  const handleRejectConfirm = async () => {
    if (rejectingId == null) return
    const res = await adminEnrollService.reject(rejectingId, {
      reason: rejectReason || undefined,
    })
    if (res.data) {
      messageApi.success('已拒绝')
      setRejectModalOpen(false)
      setDrawerOpen(false)
      setRejectingId(null)
      await Promise.all([fetchList(), fetchStatistics()])
    }
  }

  return (
    <div className="flex flex-col gap-6">
      {/* Statistics */}
      <StatisticsCards statistics={statistics} />

      {/* Filter bar */}
      <FilterBar value={filters} onChange={handleFilterChange} />

      {/* Card grid */}
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          {list.map((enrollment) => (
            <Col xs={24} md={12} lg={8} key={enrollment.id}>
              <EnrollmentCard
                enrollment={enrollment}
                onClick={() => handleCardClick(enrollment)}
                onApprove={handleApprove}
                onReject={handleRejectClick}
              />
            </Col>
          ))}
          {!loading && list.length === 0 && (
            <Col span={24}>
              <div className="text-center py-20 text-white/50">暂无报名数据</div>
            </Col>
          )}
        </Row>
      </Spin>

      {/* Pagination */}
      {totalElements > PAGE_SIZE && (
        <div className="flex justify-center">
          <Pagination
            current={page}
            total={totalElements}
            pageSize={PAGE_SIZE}
            showSizeChanger={false}
            onChange={(p) => setPage(p)}
          />
        </div>
      )}

      {/* Detail drawer */}
      <EnrollmentDrawer
        open={drawerOpen}
        detail={drawerDetail}
        loading={drawerLoading}
        onClose={() => setDrawerOpen(false)}
        onApprove={handleApprove}
        onReject={handleRejectClick}
      />

      {/* Reject modal */}
      <Modal
        title="拒绝报名"
        open={rejectModalOpen}
        onOk={handleRejectConfirm}
        onCancel={() => setRejectModalOpen(false)}
        okText="确认拒绝"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <div className="mb-2 text-white/55 text-[13px]">请填写拒绝原因（可选）</div>
        <Input.TextArea
          rows={4}
          maxLength={200}
          showCount
          placeholder="拒绝原因..."
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
        />
      </Modal>
    </div>
  )
}
