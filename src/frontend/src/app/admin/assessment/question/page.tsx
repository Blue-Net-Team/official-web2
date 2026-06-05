'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import { App, Button, Empty, Grid, Modal, Pagination, Select, Spin, Table, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, PaperClipOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type {
  AssessmentQuestionDTO,
  AssessmentTimeDTO,
  QuestionType,
} from '@/apis/schema/assessment.dto'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentTimeService } from '@/apis/services/admin-assessment-time.service'
import { adminAssessmentQuestionService } from '@/apis/services/admin-assessment-question.service'
import { useAuth } from '@/hooks'
import { getRoleLevel } from '@/utils/RoleUtils'
import QuestionDrawer, { QUESTION_TYPE_LABELS, type DrawerMode } from './QuestionDrawer'
import { usePagination } from '@/hooks'
import { ResponseMessage, PageDTO } from '@/apis/schema/type'

const PAGE_SIZE = 20
const { useBreakpoint } = Grid

/** Select 中「全局」选项的哨兵值，对应 direction = null。 */
const GLOBAL_DIRECTION_VALUE = '__GLOBAL__' as const
/** 方向筛选器支持的值类型。 */
type DirectionFilterValue = Direction | typeof GLOBAL_DIRECTION_VALUE

const QUESTION_TYPE_COLORS: Record<QuestionType, string> = {
  FILE_UPLOAD: 'blue',
  SINGLE_CHOICE: 'green',
  MULTIPLE_CHOICE: 'purple',
  ALGORITHM: 'orange',
}

export default function AssessmentQuestionManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  const { userInfo } = useAuth()
  const isSuperAdmin = getRoleLevel(userInfo?.roleName || '') >= 3
  const roleLevel = getRoleLevel(userInfo?.roleName || '')
  const userDirection = userInfo?.direction

  // Filter state
  const [filterDirection, setFilterDirection] = useState<Direction | null | undefined>(
    isSuperAdmin ? undefined : (userDirection ?? undefined)
  )
  const [filterTimeId, setFilterTimeId] = useState<number | undefined>(undefined)

  // 用户加载完成后，非超管默认选中自己的方向
  useEffect(() => {
    if (!isSuperAdmin && userDirection && !filterDirection) {
      setFilterDirection(userDirection)
    }
  }, [isSuperAdmin, userDirection, filterDirection])

  // Assessment times for the selected direction
  const [assessmentTimes, setAssessmentTimes] = useState<AssessmentTimeDTO[]>([])

  // Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('view')
  const [selectedQuestion, setSelectedQuestion] = useState<AssessmentQuestionDTO | null>(null)

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false)
  const [deletingItem, setDeletingItem] = useState<AssessmentQuestionDTO | null>(null)

  // Permission: 全局考核仅 SUPER_ADMIN 可操作；其他情况 DIRECTION_ADMIN 只能操作本方向。
  const canOperate = useMemo(() => {
    if (filterDirection === null) return isSuperAdmin
    return isSuperAdmin || filterDirection === userDirection
  }, [isSuperAdmin, filterDirection, userDirection])

  // Direction options: SUPER_ADMIN 额外看到「全局」；DIRECTION_ADMIN 只能看到本方向。
  const directionOptions = useMemo(() => {
    const entries = Object.entries(DIRECTION_LABELS) as [Direction, string][]
    const options = entries.map(([value, label]) => ({ value, label }))
    if (!isSuperAdmin && userDirection) {
      return options.filter((opt) => opt.value === userDirection)
    }
    if (isSuperAdmin) {
      return [{ value: GLOBAL_DIRECTION_VALUE, label: '全局' }, ...options]
    }
    return options
  }, [isSuperAdmin, userDirection])

  // 方向变更后加载该方向下的考核时间；null 表示全局考核。
  const fetchAssessmentTimes = useCallback(async (direction: Direction | null) => {
    try {
      const res = await adminAssessmentTimeService.getList(0, 100)
      if (res.data) {
        if (direction === null) {
          setAssessmentTimes(res.data.content.filter((t) => t.direction === null))
        } else {
          setAssessmentTimes(res.data.content.filter((t) => t.direction === direction))
        }
      }
    } catch {
      // silent
    }
  }, [])

  useEffect(() => {
    if (filterDirection !== undefined) {
      fetchAssessmentTimes(filterDirection)
    } else {
      setAssessmentTimes([])
    }
    setFilterTimeId(undefined)
  }, [filterDirection, fetchAssessmentTimes])

  // 考核时间下拉选项；全局考核时 grade 为 null，显示为「不限年级」。
  const timeOptions = useMemo(() => {
    return assessmentTimes.map((t) => ({
      value: t.id,
      label: `${t.direction ? DIRECTION_LABELS[t.direction] : '全局'} · 第 ${t.epoch} 轮 · ${t.grade != null ? `${t.grade}级` : '不限年级'}`,
    }))
  }, [assessmentTimes])

  // 当前选中的考核时间上下文。
  const selectedTime = useMemo(() => {
    return assessmentTimes.find((t) => t.id === filterTimeId) ?? null
  }, [assessmentTimes, filterTimeId])

  const apiFn = useCallback(
    (page: number, pageSize: number): Promise<ResponseMessage<PageDTO<AssessmentQuestionDTO>>> => {
      if (!filterTimeId) {
        return Promise.resolve({
          code: 200,
          data: {
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: page,
            size: pageSize,
          },
          msg: 'success',
        } as unknown as ResponseMessage<PageDTO<AssessmentQuestionDTO>>)
      }
      return adminAssessmentQuestionService.getList(filterTimeId, page, pageSize)
    },
    [filterTimeId]
  )

  const {
    data: questions,
    total: totalElements,
    loading,
    currentPage,
    setCurrentPage,
    refresh,
    reset,
  } = usePagination(apiFn, { pageSize: PAGE_SIZE })

  // 切换方向筛选并重置分页；__GLOBAL__ 映射为 null 表示全局。
  const handleDirectionChange = (value: DirectionFilterValue | undefined) => {
    setFilterDirection(value === GLOBAL_DIRECTION_VALUE ? null : value)
  }

  // 切换考核时间筛选并重置分页。
  const handleTimeChange = (value: number | undefined) => {
    setFilterTimeId(value)
    reset()
  }

  // 打开新增考题抽屉。
  const handleCreate = () => {
    setSelectedQuestion(null)
    setDrawerMode('create')
    setDrawerOpen(true)
  }

  // 点击表格行时以只读模式查看考题。
  const handleRowClick = useCallback((record: AssessmentQuestionDTO) => {
    setSelectedQuestion(record)
    setDrawerMode('view')
    setDrawerOpen(true)
  }, [])

  // 打开编辑考题抽屉。
  const handleEdit = useCallback((record: AssessmentQuestionDTO) => {
    setSelectedQuestion(record)
    setDrawerMode('edit')
    setDrawerOpen(true)
  }, [])

  // 打开删除确认弹窗。
  const handleDeleteClick = useCallback((item: AssessmentQuestionDTO) => {
    setDeletingItem(item)
    setDeleteModalOpen(true)
  }, [])

  // 确认删除考题后刷新列表。
  const handleDeleteConfirm = async () => {
    if (!deletingItem) return
    try {
      await adminAssessmentQuestionService.delete(deletingItem.id)
      messageApi.success('删除成功')
      setDeleteModalOpen(false)
      setDrawerOpen(false)
      refresh()
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '删除失败')
    }
  }

  // 抽屉保存成功后关闭抽屉并刷新列表。
  const handleDrawerSuccess = () => {
    setDrawerOpen(false)
    refresh()
  }

  // 考题表格列定义。
  const columns: ColumnsType<AssessmentQuestionDTO> = useMemo(() => {
    const cols: ColumnsType<AssessmentQuestionDTO> = [
      {
        title: '题号',
        dataIndex: 'questionNo',
        key: 'questionNo',
        width: 60,
      },
      {
        title: '题型',
        dataIndex: 'questionType',
        key: 'questionType',
        width: 90,
        render: (type: QuestionType) => (
          <Tag color={QUESTION_TYPE_COLORS[type]} bordered={false}>
            {QUESTION_TYPE_LABELS[type]}
          </Tag>
        ),
      },
      {
        title: '标题',
        dataIndex: 'title',
        key: 'title',
        ellipsis: true,
      },
      {
        title: '分值',
        dataIndex: 'score',
        key: 'score',
        width: 60,
      },
      {
        title: '附件',
        key: 'attachment',
        width: 50,
        render: (_: unknown, record: AssessmentQuestionDTO) =>
          record.attachmentId ? <PaperClipOutlined className="text-white/50" /> : '-',
      },
      {
        title: '操作',
        key: 'action',
        width: 80,
        render: (_: unknown, record: AssessmentQuestionDTO) =>
          canOperate ? (
            <div className="flex gap-1">
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={(e) => {
                  e.stopPropagation()
                  handleEdit(record)
                }}
              />
              <Button
                type="text"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={(e) => {
                  e.stopPropagation()
                  handleDeleteClick(record)
                }}
              />
            </div>
          ) : null,
      },
    ]

    if (isMobile) {
      return cols.filter((c) => !['score', 'attachment'].includes(c.key as string))
    }

    return cols
  }, [isMobile, canOperate, handleEdit, handleDeleteClick, handleRowClick])

  // 空状态文案随方向和考核时间筛选状态变化。
  const emptyText =
    filterDirection === undefined
      ? '请先选择方向'
      : filterDirection === null && assessmentTimes.length === 0
        ? '暂无全局考核时间'
        : !filterTimeId
          ? '请选择考核时间'
          : '暂无考题，点击新增添加'

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium text-white/90 m-0">考题管理</h2>
        {canOperate && filterTimeId && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新增考题
          </Button>
        )}
      </div>

      {/* 方向管理员未配置方向时的提示 */}
      {!isSuperAdmin && !userDirection && (
        <div className="flex justify-center py-20">
          <Empty
            description={
              <span className="text-white/60">
                您尚未配置方向，请联系超级管理员配置方向后再管理考题
              </span>
            }
          />
        </div>
      )}

      {/* 有权限操作时才显示筛选器和表格 */}
      {(isSuperAdmin || userDirection) && (
        <>
          {/* Filters */}
          <div className="flex gap-3 items-center flex-wrap">
            <Select
              placeholder="筛选方向"
              allowClear
              className="w-[140px]"
              value={filterDirection === null ? GLOBAL_DIRECTION_VALUE : filterDirection}
              onChange={handleDirectionChange}
              options={directionOptions}
            />
            <Select
              placeholder="选择考核时间"
              allowClear
              className="w-[200px]"
              value={filterTimeId}
              onChange={handleTimeChange}
              options={timeOptions}
              disabled={filterDirection === undefined}
            />
          </div>

          {/* Table */}
          <Spin spinning={loading}>
            <Table
              dataSource={questions}
              columns={columns}
              rowKey={(record) => String(record.id)}
              size="small"
              pagination={false}
              onRow={(record) => ({
                onClick: () => handleRowClick(record),
                className: 'cursor-pointer',
              })}
              locale={{ emptyText }}
            />
          </Spin>

          {/* Pagination */}
          {totalElements > PAGE_SIZE && (
            <div className="flex justify-center">
              <Pagination
                current={currentPage + 1}
                total={totalElements}
                pageSize={PAGE_SIZE}
                showSizeChanger={false}
                onChange={(p) => setCurrentPage(p - 1)}
              />
            </div>
          )}

          {/* Drawer */}
          <QuestionDrawer
            key={selectedQuestion?.id ?? 'create'}
            open={drawerOpen}
            question={selectedQuestion}
            assessmentTimeId={filterTimeId ?? null}
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
            <p>确认删除考题「{deletingItem?.title}」？此操作不可撤销。</p>
          </Modal>
        </>
      )}
    </div>
  )
}
