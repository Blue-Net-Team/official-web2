'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import { Alert, App, Form, InputNumber, Modal, Select } from 'antd'
import dayjs from 'dayjs'
import type { AssessmentQuestionDTO, AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentQuestionService } from '@/apis/services/admin-assessment-question.service'

const QUESTION_PAGE_SIZE = 20

interface CloneQuestionModalProps {
  open: boolean
  question: AssessmentQuestionDTO | null
  sourceAssessmentTime: AssessmentTimeDTO | null
  assessmentTimes: AssessmentTimeDTO[]
  onClose: () => void
  onSuccess: () => void
}

interface CloneFormValues {
  targetAssessmentTimeId: number
  targetQuestionNo: number
}

export default function CloneQuestionModal({
  open,
  question,
  sourceAssessmentTime,
  assessmentTimes,
  onClose,
  onSuccess,
}: CloneQuestionModalProps) {
  const { message: messageApi } = App.useApp()
  const [form] = Form.useForm<CloneFormValues>()
  const [defaultNoLoading, setDefaultNoLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const requestSequence = useRef(0)

  useEffect(() => {
    if (!open) {
      requestSequence.current += 1
      setDefaultNoLoading(false)
      setSubmitting(false)
      return
    }
    form.resetFields()
  }, [open, question?.id, form])

  const targetOptions = useMemo(
    () =>
      assessmentTimes
        .filter(
          (time) =>
            time.id !== sourceAssessmentTime?.id &&
            time.direction === sourceAssessmentTime?.direction
        )
        .map((time) => ({
          value: time.id,
          label: `${time.direction ? DIRECTION_LABELS[time.direction] : '全局'} · ${
            time.epoch === 0 ? '最终考核' : `第 ${time.epoch} 轮`
          } · ${time.grade != null ? `${time.grade}级` : '不限年级'} · ${dayjs(
            time.startTime
          ).format('YYYY-MM-DD HH:mm')}`,
        })),
    [assessmentTimes, sourceAssessmentTime]
  )

  const loadDefaultQuestionNo = async (targetAssessmentTimeId: number) => {
    const sequence = ++requestSequence.current
    form.setFieldsValue({ targetAssessmentTimeId, targetQuestionNo: undefined })
    setDefaultNoLoading(true)
    try {
      const firstResponse = await adminAssessmentQuestionService.getList(
        targetAssessmentTimeId,
        0,
        QUESTION_PAGE_SIZE
      )
      const firstPage = firstResponse.data
      if (!firstPage) throw new Error('无法读取目标考题列表')

      let nextQuestionNo = 1
      if (firstPage.totalElements > 0) {
        const lastPageNumber = Math.floor((firstPage.totalElements - 1) / QUESTION_PAGE_SIZE)
        const lastPage =
          lastPageNumber === 0
            ? firstPage
            : (
                await adminAssessmentQuestionService.getList(
                  targetAssessmentTimeId,
                  lastPageNumber,
                  QUESTION_PAGE_SIZE
                )
              ).data
        const lastQuestion = lastPage?.content[lastPage.content.length - 1]
        if (!lastQuestion) throw new Error('无法计算目标题号')
        nextQuestionNo = lastQuestion.questionNo + 1
      }

      if (sequence !== requestSequence.current) return
      form.setFieldsValue({ targetQuestionNo: nextQuestionNo })
    } catch {
      if (sequence !== requestSequence.current) return
      // API 错误已由响应拦截器统一提示
    } finally {
      if (sequence === requestSequence.current) setDefaultNoLoading(false)
    }
  }

  const handleSubmit = async () => {
    if (!question) return
    try {
      const values = await form.validateFields()
      setSubmitting(true)
      await adminAssessmentQuestionService.clone(question.id, values)
      messageApi.success('克隆成功，源题保持不变')
      onSuccess()
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) return
      // API 错误已由响应拦截器统一提示
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      title={`克隆考题「${question?.title ?? ''}」`}
      open={open}
      onCancel={onClose}
      onOk={handleSubmit}
      okText="确认克隆"
      cancelText="取消"
      confirmLoading={submitting}
      okButtonProps={{ disabled: defaultNoLoading }}
      maskClosable={!submitting}
      keyboard={!submitting}
      destroyOnHidden
      width={560}
    >
      <div className="mb-4 rounded-lg border border-white/10 bg-white/[0.035] px-3 py-2 text-sm text-white/55">
        仅复制题目定义，不复制考生作答、评分和统计。源题与副本后续互不影响。
      </div>
      <Form form={form} layout="vertical" requiredMark={false}>
        <Form.Item
          name="targetAssessmentTimeId"
          label="目标考核时间"
          rules={[{ required: true, message: '请选择目标考核时间' }]}
        >
          <Select
            placeholder="选择同方向的其他考核时间"
            options={targetOptions}
            onChange={loadDefaultQuestionNo}
            showSearch
            optionFilterProp="label"
          />
        </Form.Item>
        <Form.Item
          name="targetQuestionNo"
          label="目标题号"
          extra="默认使用目标当前最大题号 + 1，可修改；提交时会再次检查冲突。"
          rules={[{ required: true, message: '请输入目标题号' }]}
        >
          <InputNumber
            min={1}
            precision={0}
            className="w-full"
            placeholder={defaultNoLoading ? '正在计算默认题号…' : '请输入目标题号'}
            disabled={defaultNoLoading}
          />
        </Form.Item>
      </Form>
      {question?.questionType === 'ALGORITHM' && (
        <Alert
          type="info"
          showIcon
          message="算法题将复制判题源码配置并重置为草稿；测试数据、测速结果和资源限制需在目标题重新生成与确认。"
        />
      )}
    </Modal>
  )
}
