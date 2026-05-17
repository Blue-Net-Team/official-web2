'use client'

import { useEffect, useMemo } from 'react'
import { App, Button, DatePicker, Drawer, Form, InputNumber, Select, Space, Switch } from 'antd'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import type {
  AssessmentTimeDTO,
  CreateAssessmentTimeRequestDTO,
  UpdateAssessmentTimeRequestDTO,
} from '@/apis/schema/assessment.dto'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentTimeService } from '@/apis/services/admin-assessment-time.service'

export type DrawerMode = 'view' | 'edit' | 'create'

interface AssessmentTimeDrawerProps {
  open: boolean
  assessmentTime: AssessmentTimeDTO | null
  mode: DrawerMode
  onClose: () => void
  onSuccess: () => void
  onDelete: (item: AssessmentTimeDTO) => void
  onEdit: () => void
  isSuperAdmin: boolean
  userDirection: Direction | null
}

/** 前端使用的特殊值，Select 提交时转回 undefined（即后端 null） */
const GLOBAL_DIRECTION_VALUE = '__GLOBAL__' as const

interface FormValues {
  direction: Direction | typeof GLOBAL_DIRECTION_VALUE
  epoch: number
  grade: number | undefined
  timeRange: [Dayjs, Dayjs]
  timeLimit: boolean
  timeLimitMinutes: number | null
  allowTeam: boolean
}

export default function AssessmentTimeDrawer({
  open,
  assessmentTime,
  mode,
  onClose,
  onSuccess,
  onDelete,
  onEdit,
  isSuperAdmin,
  userDirection,
}: AssessmentTimeDrawerProps) {
  const { message: messageApi } = App.useApp()
  const [form] = Form.useForm<FormValues>()

  const isViewMode = mode === 'view'
  const isCreateMode = mode === 'create'
  const timeLimitValue = Form.useWatch('timeLimit', form)

  // Populate form when opening
  useEffect(() => {
    if (!open) return

    if (isCreateMode) {
      form.resetFields()
      // DIRECTION_ADMIN: default to own direction
      if (!isSuperAdmin && userDirection) {
        form.setFieldsValue({ direction: userDirection })
      }
    } else if (assessmentTime) {
      form.setFieldsValue({
        direction:
          assessmentTime.direction ?? (GLOBAL_DIRECTION_VALUE as typeof GLOBAL_DIRECTION_VALUE),
        epoch: assessmentTime.epoch,
        grade: assessmentTime.grade ?? undefined,
        timeRange: [dayjs(assessmentTime.startTime), dayjs(assessmentTime.endTime)],
        timeLimit: assessmentTime.timeLimit,
        timeLimitMinutes: assessmentTime.timeLimitMinutes,
        allowTeam: assessmentTime.allowTeam,
      })
    }
  }, [open, mode, assessmentTime, form, isCreateMode, isSuperAdmin, userDirection])

  // Direction options
  const directionOptions = useMemo(() => {
    const entries = Object.entries(DIRECTION_LABELS) as [Direction, string][]
    // DIRECTION_ADMIN can only select own direction
    if (!isSuperAdmin && userDirection) {
      return entries.filter(([value]) => value === userDirection)
    }
    // SUPER_ADMIN: add the global option
    return [...entries, [GLOBAL_DIRECTION_VALUE, '全局'] as [typeof GLOBAL_DIRECTION_VALUE, string]]
  }, [isSuperAdmin, userDirection])

  // Whether current user can operate on the given direction
  const canOperate = useMemo(() => {
    if (assessmentTime == null) return true
    return (
      isSuperAdmin ||
      (assessmentTime.direction != null && assessmentTime.direction === userDirection)
    )
  }, [isSuperAdmin, userDirection, assessmentTime])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const payload: CreateAssessmentTimeRequestDTO | UpdateAssessmentTimeRequestDTO = {
        direction: values.direction === GLOBAL_DIRECTION_VALUE ? undefined : values.direction,
        epoch: values.epoch,
        grade: values.grade,
        startTime: values.timeRange[0].format('YYYY-MM-DDTHH:mm:ss'),
        endTime: values.timeRange[1].format('YYYY-MM-DDTHH:mm:ss'),
        timeLimit: values.timeLimit,
        timeLimitMinutes: values.timeLimit ? values.timeLimitMinutes : null,
        allowTeam: values.allowTeam,
      }

      if (isCreateMode) {
        await adminAssessmentTimeService.create(payload as CreateAssessmentTimeRequestDTO)
        messageApi.success('创建成功')
      } else {
        await adminAssessmentTimeService.update(
          assessmentTime!.id,
          payload as UpdateAssessmentTimeRequestDTO
        )
        messageApi.success('更新成功')
      }
      onSuccess()
    } catch (err: unknown) {
      // Form validation errors are handled by antd
      if (err && typeof err === 'object' && 'errorFields' in err) return
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '操作失败')
    }
  }

  const drawerTitle = useMemo(() => {
    if (isCreateMode) return '新增考核时间'
    if (mode === 'edit') return '编辑考核时间'
    return '考核时间详情'
  }, [mode, isCreateMode])

  return (
    <Drawer
      title={drawerTitle}
      open={open}
      onClose={onClose}
      width={isViewMode ? 420 : 480}
      footer={
        isViewMode ? (
          <div className="flex justify-end gap-2">
            {canOperate && assessmentTime && (
              <Button danger onClick={() => onDelete(assessmentTime)}>
                删除
              </Button>
            )}
            {canOperate && (
              <Button type="primary" onClick={onEdit}>
                编辑
              </Button>
            )}
          </div>
        ) : (
          <div className="flex justify-end gap-2">
            <Button onClick={onClose}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              {isCreateMode ? '创建' : '保存'}
            </Button>
          </div>
        )
      }
    >
      <Form
        form={form}
        layout="vertical"
        disabled={isViewMode}
        initialValues={{ timeLimit: false, allowTeam: false }}
      >
        <Form.Item name="direction" label="方向">
          <Select
            options={directionOptions.map(([value, label]) => ({ value, label }))}
            placeholder="选择方向"
            disabled={isViewMode || (!isSuperAdmin && !!userDirection)}
            onChange={(value) => {
              if (value === GLOBAL_DIRECTION_VALUE) {
                form.setFieldsValue({ grade: undefined })
              }
            }}
          />
        </Form.Item>

        <Form.Item name="epoch" label="轮次" rules={[{ required: true, message: '请输入轮次' }]}>
          <InputNumber min={1} placeholder="第几轮" className="w-full" />
        </Form.Item>

        <Form.Item name="grade" label="年级">
          <InputNumber
            min={2000}
            max={2100}
            placeholder="入学年份（如 2025，不限可选全局方向）"
            className="w-full"
          />
        </Form.Item>

        <Form.Item
          name="timeRange"
          label="考核时间"
          rules={[{ required: true, message: '请选择考核时间' }]}
        >
          <DatePicker.RangePicker
            showTime={{ format: 'HH:mm' }}
            format="YYYY-MM-DD HH:mm"
            className="w-full"
            placeholder={['开始时间', '结束时间']}
          />
        </Form.Item>

        <Form.Item name="timeLimit" label="限时" valuePropName="checked">
          <Switch checkedChildren="限时" unCheckedChildren="不限时" />
        </Form.Item>

        {timeLimitValue && (
          <Form.Item
            name="timeLimitMinutes"
            label="限时分钟数"
            rules={[{ required: true, message: '请输入限时分钟数' }]}
          >
            <InputNumber min={1} placeholder="限时分钟数" className="w-full" suffix="分钟" />
          </Form.Item>
        )}

        <Form.Item name="allowTeam" label="允许组队" valuePropName="checked">
          <Switch checkedChildren="允许" unCheckedChildren="不允许" />
        </Form.Item>
      </Form>
    </Drawer>
  )
}
