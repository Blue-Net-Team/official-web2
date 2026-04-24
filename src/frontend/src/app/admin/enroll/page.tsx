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
import { usePagination } from '@/hooks'
import type {
  EnrollmentBriefDTO,
  EnrollmentDetailDTO,
  EnrollmentStatisticsDTO,
} from '@/apis/schema/type'

const PAGE_SIZE = 12

export default function EnrollManagementPage() {
  const { message: messageApi } = App.useApp()

  // Statistics state
  const [statistics, setStatistics] = useState<EnrollmentStatisticsDTO | null>(null)

  // Pagination & filters
  const [filters, setFilters] = useState<FilterValues>({
    keyword: '',
    status: undefined,
    direction: undefined,
  })

  const fetchPage = useCallback(
    (page: number, pageSize: number) => {
      const params: Record<string, unknown> = {
        page,
        size: pageSize,
      }
      if (filters.keyword) params.keyword = filters.keyword
      if (filters.status) params.status = filters.status
      if (filters.direction) params.direction = filters.direction

      return adminEnrollService.getList(params as Parameters<typeof adminEnrollService.getList>[0])
    },
    [filters]
  )

  const { data, total, totalPages, loading, currentPage, setCurrentPage, refresh } = usePagination(
    fetchPage,
    { pageSize: PAGE_SIZE }
  )

  // Drawer
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerDetail, setDrawerDetail] = useState<EnrollmentDetailDTO | null>(null)
  const [drawerLoading, setDrawerLoading] = useState(false)

  // Reject modal
  const [rejectModalOpen, setRejectModalOpen] = useState(false)
  const [rejectingId, setRejectingId] = useState<number | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  // Fetch statistics
  const fetchStatistics = useCallback(async () => {
    try {
      const res = await adminEnrollService.getStatistics()
      if (res.data) setStatistics(res.data)
    } catch {
      messageApi.error('获取报名统计失败，请稍后重试')
    }
  }, [messageApi])

  // Initial load statistics
  useEffect(() => {
    fetchStatistics()
  }, [fetchStatistics])

  // Filter change handler
  const handleFilterChange = (next: FilterValues) => {
    setFilters(next)
    setCurrentPage(0)
  }

  // Open drawer
  const handleCardClick = async (enrollment: EnrollmentBriefDTO) => {
    setDrawerOpen(true)
    setDrawerDetail(null)
    setDrawerLoading(true)
    try {
      const res = await adminEnrollService.getDetail(enrollment.id)
      if (res.data) setDrawerDetail(res.data)
    } catch {
      messageApi.error('获取报名详情失败，请稍后重试')
      setDrawerOpen(false)
    } finally {
      setDrawerLoading(false)
    }
  }

  // Approve
  const handleApprove = async (id: number, assessmentGradeYear?: number) => {
    try {
      const res = await adminEnrollService.approve(id, { assessmentGradeYear })
      if (res.data) {
        messageApi.success('已通过')
        setDrawerOpen(false)
        refresh()
        await fetchStatistics()
      }
    } catch {
      messageApi.error('审核通过失败，请稍后重试')
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
    try {
      const res = await adminEnrollService.reject(rejectingId, {
        reason: rejectReason || undefined,
      })
      if (res.data) {
        messageApi.success('已拒绝')
        setRejectModalOpen(false)
        setDrawerOpen(false)
        setRejectingId(null)
        refresh()
        await fetchStatistics()
      }
    } catch {
      messageApi.error('拒绝报名失败，请稍后重试')
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
          {data.map((enrollment) => (
            <Col xs={24} md={12} lg={8} key={enrollment.id}>
              <EnrollmentCard
                enrollment={enrollment}
                onClick={() => handleCardClick(enrollment)}
                onApprove={handleApprove}
                onReject={handleRejectClick}
              />
            </Col>
          ))}
          {!loading && data.length === 0 && (
            <Col span={24}>
              <div className="text-center py-20 text-white/50">暂无报名数据</div>
            </Col>
          )}
        </Row>
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
