'use client'

import { useEffect, useState } from 'react'
import { App, Button, Drawer, Form, Input, InputNumber } from 'antd'
import { AxiosError } from 'axios'
import type { LearningStepDTO } from '@/apis/schema/direction.dto'
import { adminDirectionService } from '@/apis/services/direction.service'
import type { ResponseMessage } from '@/apis/schema/type'

interface LearningStepDrawerProps {
  open: boolean
  /** 当前方向 slug（cv/embed/struct），新增时使用 */
  direction: string
  /** 编辑的记录；为 null 表示新增 */
  record: LearningStepDTO | null
  onSuccess: () => void
  onCancel: () => void
}

interface FormValues {
  stepNumber: number
  title: string
  relatedLink?: string
}

export default function LearningStepDrawer({
  open,
  direction,
  record,
  onSuccess,
  onCancel,
}: LearningStepDrawerProps) {
  const [form] = Form.useForm<FormValues>()
  const { message: messageApi } = App.useApp()
  const [saving, setSaving] = useState(false)

  const isEdit = record !== null

  useEffect(() => {
    if (open) {
      if (record) {
        form.setFieldsValue({
          stepNumber: record.stepNumber,
          title: record.title,
          relatedLink: record.relatedLink ?? undefined,
        })
      } else {
        form.resetFields()
      }
    }
  }, [open, record, form])

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      const payload = {
        stepNumber: values.stepNumber,
        title: values.title,
        relatedLink: values.relatedLink || null,
      }
      const response = isEdit
        ? await adminDirectionService.updateStep(record.id, payload)
        : await adminDirectionService.createStep(direction, payload)
      if (response.code === 200) {
        messageApi.success(isEdit ? '更新成功' : '创建成功')
        onSuccess()
      } else {
        messageApi.error(`保存失败: ${response.msg}`)
      }
    } catch (error) {
      if (error instanceof AxiosError) {
        // 透出后端业务错误消息（如步骤序号冲突）
        const data = error.response?.data as ResponseMessage<unknown> | undefined
        messageApi.error(data?.msg || '保存失败')
      } else if (error instanceof Error && error.message) {
        // 表单校验失败等本地错误不重复提示
        if (!('errorFields' in error)) {
          messageApi.error('保存失败')
        }
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <Drawer
      title={isEdit ? `编辑步骤 - ${record.title}` : '新增步骤'}
      open={open}
      width={480}
      onClose={onCancel}
      destroyOnHidden
      footer={
        <div className="flex justify-end gap-2">
          <Button onClick={onCancel}>取消</Button>
          <Button type="primary" onClick={handleSave} loading={saving}>
            保存
          </Button>
        </div>
      }
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="步骤序号"
          name="stepNumber"
          rules={[{ required: true, message: '请输入步骤序号' }]}
        >
          <InputNumber min={1} max={100} precision={0} className="w-full" placeholder="1-100" />
        </Form.Item>
        <Form.Item
          label="标题"
          name="title"
          rules={[{ required: true, whitespace: true, message: '请输入步骤标题' }]}
        >
          <Input placeholder="请输入步骤标题" maxLength={200} />
        </Form.Item>
        <Form.Item
          label="相关链接"
          name="relatedLink"
          rules={[{ type: 'url', message: '请输入合法的 URL' }]}
        >
          <Input placeholder="https://...（可选）" maxLength={500} />
        </Form.Item>
      </Form>
    </Drawer>
  )
}
