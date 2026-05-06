'use client'

import { useCallback, useMemo, useState } from 'react'
import { App, Button, Drawer, Image, Pagination, Select, Spin, Table, Tag, Tooltip } from 'antd'
import { EyeOutlined, GithubOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { usePagination, useApi } from '@/hooks'
import { adminBugReportService } from '@/apis/services/bug-report.service'
import { API_BASE_URL } from '@/apis/config'
import { BUG_REPORT_STATUS_LABELS, BUG_REPORT_STATUS_COLORS } from '@/apis/schema/bug-report.dto'
import type { BugReportListItemDTO, BugReportStatus } from '@/apis/schema/bug-report.dto'

const PAGE_SIZE = 15

const STATUS_OPTIONS = [
  { value: 'PENDING', label: BUG_REPORT_STATUS_LABELS.PENDING },
  { value: 'IN_PROGRESS', label: BUG_REPORT_STATUS_LABELS.IN_PROGRESS },
  { value: 'RESOLVED', label: BUG_REPORT_STATUS_LABELS.RESOLVED },
]

export default function BugReportManagementPage() {
  const { message: messageApi } = App.useApp()

  // Filter state
  const [filterStatus, setFilterStatus] = useState<BugReportStatus | undefined>(undefined)

  // Detail drawer state
  const [detailOpen, setDetailOpen] = useState(false)

  // Update status loading
  const [updatingId, setUpdatingId] = useState<number | null>(null)

  // Fetch page with filters
  const fetchPage = useCallback(
    (page: number, pageSize: number) => {
      const params: { page?: number; size?: number; status?: BugReportStatus } = {
        page,
        size: pageSize,
      }
      if (filterStatus) {
        params.status = filterStatus
      }
      return adminBugReportService.getList(params)
    },
    [filterStatus]
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
  } = useApi(adminBugReportService.getDetail.bind(adminBugReportService))

  // View detail
  const handleViewDetail = async (record: BugReportListItemDTO) => {
    setDetailOpen(true)
    resetDetail()
    try {
      await fetchDetail(record.id)
    } catch {
      messageApi.error('获取详情失败')
    }
  }

  // Update status
  const handleStatusChange = async (id: number, status: BugReportStatus) => {
    setUpdatingId(id)
    try {
      const res = await adminBugReportService.updateStatus(id, { status })
      if (res.code === 200) {
        messageApi.success('状态更新成功')
        refresh()
      } else {
        messageApi.error(res.msg || '状态更新失败')
      }
    } catch {
      messageApi.error('状态更新失败')
    } finally {
      setUpdatingId(null)
    }
  }

  // Parse environment info
  const parsedEnvironment = useMemo(() => {
    if (!detailData?.environmentJson) return null
    try {
      return JSON.parse(detailData.environmentJson) as Record<string, unknown>
    } catch {
      return null
    }
  }, [detailData?.environmentJson])

  // Table columns
  const columns: ColumnsType<BugReportListItemDTO> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 60,
    },
    {
      title: '描述',
      dataIndex: 'description',
      ellipsis: true,
      render: (v: string) => <span title={v}>{v.length > 50 ? `${v.slice(0, 50)}...` : v}</span>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: BugReportStatus, record: BugReportListItemDTO) => (
        <Select
          size="small"
          value={status}
          options={STATUS_OPTIONS}
          loading={updatingId === record.id}
          onClick={(e) => e.stopPropagation()}
          onChange={(value) => handleStatusChange(record.id, value as BugReportStatus)}
          style={{ width: 90 }}
          variant="borderless"
        />
      ),
    },
    {
      title: '页面 URL',
      dataIndex: 'pageUrl',
      width: 180,
      ellipsis: true,
      render: (v: string) => (
        <a
          href={v}
          target="_blank"
          rel="noopener noreferrer"
          className="text-blue-400 hover:text-blue-300"
          onClick={(e) => e.stopPropagation()}
          title={v}
        >
          {v.length > 30 ? `${v.slice(0, 30)}...` : v}
        </a>
      ),
    },
    {
      title: 'GitHub',
      key: 'github',
      width: 80,
      align: 'center',
      render: (_, record: BugReportListItemDTO) =>
        record.githubIssueUrl ? (
          <a
            href={record.githubIssueUrl}
            target="_blank"
            rel="noopener noreferrer"
            title="查看 GitHub Issue"
            onClick={(e) => e.stopPropagation()}
          >
            <GithubOutlined className="text-white/70 hover:text-blue-400 text-base" />
          </a>
        ) : (
          <span className="text-white/20">-</span>
        ),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      fixed: 'right',
      render: (_, record) => (
        <Button
          type="text"
          size="small"
          icon={<EyeOutlined />}
          onClick={(e) => {
            e.stopPropagation()
            handleViewDetail(record)
          }}
        >
          查看
        </Button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">Bug 报告管理</h2>
      </div>

      {/* Filters */}
      <div className="flex gap-3 items-center flex-wrap">
        <Select
          placeholder="筛选状态"
          allowClear
          className="w-[140px]"
          value={filterStatus}
          onChange={(value) => {
            setFilterStatus(value)
            setCurrentPage(0)
          }}
          options={STATUS_OPTIONS}
        />
      </div>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          size="small"
          pagination={false}
          scroll={{ x: 'max-content' }}
          locale={{ emptyText: '暂无 Bug 报告' }}
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
      <Drawer
        title={`Bug 报告详情 #${detailData?.id}`}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        width={560}
      >
        <Spin spinning={detailLoading}>
          {detailData && (
            <div className="flex flex-col gap-6">
              {/* Status & GitHub Link */}
              <div className="flex items-center gap-4">
                <div>
                  <div className="text-sm text-white/50 mb-1">当前状态</div>
                  <Tag color={BUG_REPORT_STATUS_COLORS[detailData.status]} className="text-sm">
                    {BUG_REPORT_STATUS_LABELS[detailData.status]}
                  </Tag>
                </div>
                {detailData.githubIssueUrl && (
                  <div>
                    <div className="text-sm text-white/50 mb-1">GitHub Issue</div>
                    <a
                      href={detailData.githubIssueUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1 text-sm text-blue-400 hover:text-blue-300"
                    >
                      <GithubOutlined />#{detailData.githubIssueNumber}
                    </a>
                  </div>
                )}
              </div>

              {/* Description */}
              <div>
                <div className="text-sm text-white/50 mb-1">问题描述</div>
                <div className="text-sm whitespace-pre-wrap bg-white/5 p-3 rounded">
                  {detailData.description}
                </div>
              </div>

              {/* Page URL */}
              <div>
                <div className="text-sm text-white/50 mb-1">页面 URL</div>
                <a
                  href={detailData.pageUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-sm text-blue-400 hover:text-blue-300 break-all"
                >
                  {detailData.pageUrl}
                </a>
              </div>

              {/* Environment Info */}
              {parsedEnvironment && (
                <div>
                  <div className="text-sm text-white/50 mb-1">环境信息</div>
                  <pre className="text-xs bg-white/5 p-3 rounded overflow-auto max-h-60">
                    {JSON.stringify(parsedEnvironment, null, 2)}
                  </pre>
                </div>
              )}

              {/* Screenshots */}
              {detailData.fileIds.length > 0 && (
                <div>
                  <div className="text-sm text-white/50 mb-2">截图</div>
                  <div className="flex flex-wrap gap-2">
                    {detailData.fileIds.map((fileId) => {
                      const imgUrl = `${API_BASE_URL}/file/download/${fileId}`
                      return (
                        <Image
                          key={fileId}
                          src={imgUrl}
                          alt={`截图 ${fileId}`}
                          className="rounded border border-white/10"
                          style={{ maxWidth: 200, maxHeight: 200, objectFit: 'cover' }}
                          preview={{ src: imgUrl }}
                        />
                      )
                    })}
                  </div>
                </div>
              )}

              {/* Reporter Email */}
              {detailData.reporterEmail && (
                <div>
                  <div className="text-sm text-white/50 mb-1">联系邮箱</div>
                  <div className="text-sm">{detailData.reporterEmail}</div>
                </div>
              )}
            </div>
          )}
        </Spin>
      </Drawer>
    </div>
  )
}
