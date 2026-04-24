'use client'

import { useEffect, useState } from 'react'
import { App, Button, Drawer, Form, Input, Spin, Tag, Divider, Tabs } from 'antd'
import { EyeOutlined } from '@ant-design/icons'
import type { MessageTemplateInfoDTO } from '@/apis/schema/type'
import { adminMessageTemplateService } from '@/apis/services/admin-message-template.service'

interface MessageTemplateDrawerProps {
  open: boolean
  record: MessageTemplateInfoDTO | null
  onSuccess: () => void
  onCancel: () => void
}

interface FormValues {
  subject: string
  content: string
}

export default function MessageTemplateDrawer({
  open,
  record,
  onSuccess,
  onCancel,
}: MessageTemplateDrawerProps) {
  const [form] = Form.useForm<FormValues>()
  const { message: messageApi } = App.useApp()
  const [saving, setSaving] = useState(false)
  const [previewing, setPreviewing] = useState(false)
  const [previewHtml, setPreviewHtml] = useState<string>('')
  const [activeTab, setActiveTab] = useState<string>('edit')

  useEffect(() => {
    if (open && record) {
      form.setFieldsValue({
        subject: record.subject,
        content: record.content,
      })
      setPreviewHtml('')
      setActiveTab('edit')
    }
  }, [open, record, form])

  const handleSave = async () => {
    if (!record) return
    try {
      const values = await form.validateFields()
      setSaving(true)
      const response = await adminMessageTemplateService.update(record.code, {
        subject: values.subject,
        content: values.content,
      })
      if (response.code === 200) {
        messageApi.success('保存成功')
        onSuccess()
      } else {
        messageApi.error(`保存失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('保存模板失败:', error)
      messageApi.error('保存失败')
    } finally {
      setSaving(false)
    }
  }

  const handlePreview = async () => {
    if (!record) return
    try {
      setPreviewing(true)
      // 构造测试变量值
      const testVariables: Record<string, string> = {}
      record.variables.forEach((v) => {
        switch (v) {
          case 'code':
            testVariables[v] = '123456'
            break
          case 'username':
          case 'nickname':
            testVariables[v] = '张三'
            break
          case 'studentId':
            testVariables[v] = '2024001001'
            break
          case 'initialPassword':
            testVariables[v] = 'Abcd1234!'
            break
          case 'title':
            testVariables[v] = '登录'
            break
          case 'description':
            testVariables[v] = '您的验证码为：'
            break
          case 'footer':
            testVariables[v] = '验证码5分钟内有效。'
            break
          case 'rejectReason':
            testVariables[v] = '人数已满，请下次再报名。'
            break
          case 'directionLabel':
            testVariables[v] = '计算机视觉'
            break
          case 'epoch':
            testVariables[v] = '1'
            break
          case 'color':
            testVariables[v] = '#52c41a'
            break
          case 'resultText':
            testVariables[v] = '通过'
            break
          default:
            testVariables[v] = `{{${v}}}`
        }
      })

      const response = await adminMessageTemplateService.preview(record.code, testVariables)
      if (response.code === 200 && response.data) {
        setPreviewHtml(response.data)
        setActiveTab('preview')
      } else {
        messageApi.error(`预览失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('预览模板失败:', error)
      messageApi.error('预览失败')
    } finally {
      setPreviewing(false)
    }
  }

  const handleResetToDefault = () => {
    if (!record) return
    form.setFieldsValue({
      content: record.defaultContent,
    })
    messageApi.info('已恢复为默认内容')
  }

  return (
    <Drawer
      title={`编辑模板 - ${record?.name || ''}`}
      open={open}
      width={800}
      onClose={onCancel}
      footer={
        <div className="flex justify-end gap-2">
          <Button onClick={onCancel}>取消</Button>
          <Button onClick={handleResetToDefault}>恢复默认</Button>
          <Button icon={<EyeOutlined />} onClick={handlePreview} loading={previewing}>
            预览
          </Button>
          <Button type="primary" onClick={handleSave} loading={saving}>
            保存
          </Button>
        </div>
      }
    >
      {record && (
        <>
          <div className="mb-4">
            <div className="text-[#666] text-sm mb-2">
              <strong>描述：</strong>
              {record.description || '-'}
            </div>
            <div className="text-[#666] text-sm mb-2">
              <strong>可用变量：</strong>
              {record.variables.map((v) => (
                <Tag key={v} color="blue">
                  {'{{'}
                  {v}
                  {'}}'}
                </Tag>
              ))}
              {record.variables.length === 0 && <span className="text-[#999]">无</span>}
            </div>
          </div>

          <Divider />

          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            items={[
              {
                key: 'edit',
                label: '编辑内容',
                children: (
                  <Form form={form} layout="vertical">
                    <Form.Item
                      label="邮件主题"
                      name="subject"
                      rules={[{ required: true, message: '请输入邮件主题' }]}
                    >
                      <Input placeholder="请输入邮件主题" maxLength={200} />
                    </Form.Item>
                    <Form.Item
                      label="模板内容（HTML）"
                      name="content"
                      rules={[{ required: true, message: '请输入模板内容' }]}
                    >
                      <Input.TextArea
                        placeholder="请输入 HTML 模板内容"
                        rows={20}
                        className="font-mono"
                      />
                    </Form.Item>
                  </Form>
                ),
              },
              {
                key: 'preview',
                label: '预览效果',
                children: (
                  <Spin spinning={previewing}>
                    {previewHtml ? (
                      <div className="border border-[#d9d9d9] rounded-lg p-4 bg-white">
                        <iframe
                          srcDoc={previewHtml}
                          className="w-full h-[500px] border-none"
                          title="模板预览"
                        />
                      </div>
                    ) : (
                      <div className="text-center p-12 text-[#999] border border-dashed border-[#d9d9d9] rounded-lg">
                        点击「预览」按钮查看渲染效果
                      </div>
                    )}
                  </Spin>
                ),
              },
            ]}
          />
        </>
      )}
    </Drawer>
  )
}
