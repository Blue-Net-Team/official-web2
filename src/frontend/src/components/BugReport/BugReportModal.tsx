'use client'

import { useState } from 'react'
import { App, Button, Form, Input, Modal, Upload } from 'antd'
import { UploadOutlined, SendOutlined } from '@ant-design/icons'
import type { UploadFile, UploadProps } from 'antd/es/upload'
import { usePresignedUpload } from '@/hooks/usePresignedUpload'
import { bugReportService } from '@/apis/services/bug-report.service'

interface BugReportModalProps {
  open: boolean
  onClose: () => void
}

interface BugReportFormValues {
  description: string
  reporterEmail?: string
}

const MAX_SCREENSHOTS = 3

export default function BugReportModal({ open, onClose }: BugReportModalProps) {
  const { message: messageApi } = App.useApp()
  const [form] = Form.useForm<BugReportFormValues>()
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const [submitting, setSubmitting] = useState(false)

  const { upload } = usePresignedUpload()

  const handleUpload: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError } = options
    try {
      const fileId = await upload(file as File, 'NORMAL_IMG')
      if (fileId != null) {
        onSuccess?.({ id: fileId })
      } else {
        onError?.(new Error('上传失败'))
      }
    } catch (err) {
      onError?.(err as Error)
    }
  }

  const handleChange: UploadProps['onChange'] = ({ fileList: newFileList }) => {
    setFileList(newFileList)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    setSubmitting(true)

    try {
      // 收集已上传文件的 ID
      const fileIds = fileList
        .filter((f) => f.status === 'done' && f.response)
        .map((f) => (f.response as { id: number }).id)

      // 收集环境信息
      const environmentJson = JSON.stringify({
        href: window.location.href,
        userAgent: navigator.userAgent,
        screenWidth: window.screen.width,
        screenHeight: window.screen.height,
        innerWidth: window.innerWidth,
        innerHeight: window.innerHeight,
      })

      const res = await bugReportService.create({
        description: values.description,
        reporterEmail: values.reporterEmail,
        fileIds,
        pageUrl: window.location.href,
        environmentJson,
      })

      if (res.code === 200) {
        messageApi.success('提交成功，感谢反馈！')
        form.resetFields()
        setFileList([])
        onClose()
      } else {
        messageApi.error(res.msg || '提交失败')
      }
    } catch {
      messageApi.error('提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  const handleCancel = () => {
    form.resetFields()
    setFileList([])
    onClose()
  }

  return (
    <Modal
      title="反馈问题"
      open={open}
      onCancel={handleCancel}
      footer={
        <div className="flex justify-end gap-3">
          <Button onClick={handleCancel}>取消</Button>
          <Button
            type="primary"
            icon={<SendOutlined />}
            loading={submitting}
            onClick={handleSubmit}
          >
            提交反馈
          </Button>
        </div>
      }
      destroyOnClose
    >
      <Form form={form} layout="vertical" className="mt-4">
        <Form.Item
          name="description"
          label="问题描述"
          rules={[{ required: true, message: '请描述遇到的问题' }]}
        >
          <Input.TextArea
            rows={4}
            maxLength={2000}
            showCount
            placeholder="请详细描述您遇到的问题..."
          />
        </Form.Item>

        <Form.Item label="截图（最多 3 张）">
          <Upload
            listType="picture-card"
            fileList={fileList}
            customRequest={handleUpload}
            onChange={handleChange}
            maxCount={MAX_SCREENSHOTS}
            accept="image/*"
          >
            {fileList.length < MAX_SCREENSHOTS && (
              <div>
                <UploadOutlined />
                <div className="mt-1">上传</div>
              </div>
            )}
          </Upload>
        </Form.Item>

        <Form.Item
          name="reporterEmail"
          label="联系邮箱"
          rules={[{ type: 'email', message: '邮箱格式不正确' }]}
        >
          <Input placeholder="选填，方便我们联系您" />
        </Form.Item>
      </Form>
    </Modal>
  )
}
