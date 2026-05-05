'use client'

import { useEffect, useState } from 'react'
import { App, Button, Drawer, Form, Input, InputNumber, Select, Switch, Upload, Spin, Image } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import type {
  ConsultationQrcodeDTO,
  AssessmentQrcodeDTO,
  CreateAssessmentQrcodeRequestDTO,
  UpdateConsultationQrcodeRequestDTO,
  UpdateAssessmentQrcodeRequestDTO,
} from '@/apis/services/qrcode.service'
import { API_BASE_URL } from '@/apis/config'
import { fileService } from '@/apis/services/file.service'
import { qrcodeService } from '@/apis/services/qrcode.service'

export type DrawerMode = 'create' | 'edit'
export type QrcodeType = 'consultation' | 'assessment'

interface ConsultationFormValues {
  fileId?: number | null
}

interface AssessmentFormValues {
  fileId?: number | null
  direction?: string
  epoch?: number
  isShared?: boolean
}

interface QrcodeDrawerProps {
  open: boolean
  mode: DrawerMode
  type: QrcodeType
  record: ConsultationQrcodeDTO | AssessmentQrcodeDTO | null
  onSuccess: () => void
  onCancel: () => void
}

const { Option } = Select

const FILE_TYPE = 'QRCODE' as const

const DIRECTION_OPTIONS = [
  { value: 'COMPUTER_VISION', label: '计算机视觉' },
  { value: 'STRUCTURAL_DESIGN', label: '结构设计' },
  { value: 'EMBEDDED', label: '嵌入式开发' },
]

export default function QrcodeDrawer({
  open,
  mode,
  type,
  record,
  onSuccess,
  onCancel,
}: QrcodeDrawerProps) {
  const [form] = Form.useForm()
  const { message: messageApi } = App.useApp()
  const [saving, setSaving] = useState(false)
  const [fileUploading, setFileUploading] = useState(false)
  const [fileId, setFileId] = useState<number | null>(null)
  const [isShared, setIsShared] = useState(false)

  // 当打开或数据/模式变化时重置表单
  useEffect(() => {
    if (open) {
      if (mode === 'create') {
        form.resetFields()
        setFileId(null)
        setIsShared(false)
      } else if (record) {
        if (type === 'consultation') {
          const consultationRecord = record as ConsultationQrcodeDTO
          form.setFieldsValue({
            fileId: consultationRecord.fileId,
          })
          setFileId(consultationRecord.fileId)
        } else {
          const assessmentRecord = record as AssessmentQrcodeDTO
          form.setFieldsValue({
            fileId: assessmentRecord.fileId,
            direction: assessmentRecord.direction,
            epoch: assessmentRecord.epoch,
            isShared: assessmentRecord.isShared,
          })
          setFileId(assessmentRecord.fileId)
          setIsShared(assessmentRecord.isShared || false)
        }
      }
    }
  }, [open, mode, type, record, form])

  const handleSave = async () => {
    try {
      if (!fileId) {
        messageApi.error('请上传二维码图片')
        return
      }

      const values = await form.validateFields()
      setSaving(true)

      if (type === 'consultation') {
        if (mode === 'create') {
          await qrcodeService.createConsultationQrcode(fileId)
          messageApi.success('创建成功')
        } else if (record) {
          const payload: UpdateConsultationQrcodeRequestDTO = {
            fileId: fileId,
          }
          await qrcodeService.updateConsultationQrcode(record.id, payload)
          messageApi.success('更新成功')
        }
      } else {
        if (mode === 'create') {
          const payload: CreateAssessmentQrcodeRequestDTO = {
            fileId: fileId,
            direction: isShared ? undefined : values.direction,
            epoch: values.epoch!,
            isShared: isShared || false,
          }
          await qrcodeService.createAssessmentQrcode(payload)
          messageApi.success('创建成功')
        } else if (record) {
          const payload: UpdateAssessmentQrcodeRequestDTO = {
            fileId: fileId,
            direction: isShared ? undefined : values.direction,
            epoch: values.epoch,
            isShared: isShared || false,
          }
          await qrcodeService.updateAssessmentQrcode(record.id, payload)
          messageApi.success('更新成功')
        }
      }

      onSuccess()
    } catch (error) {
      console.error('保存失败:', error)
      messageApi.error('保存失败')
    } finally {
      setSaving(false)
    }
  }

  const handleFileUpload = async (file: File) => {
    try {
      setFileUploading(true)
      const response = await fileService.upload(file, FILE_TYPE)
      if (response.code === 200 && response.data) {
        const uploadedFileId = response.data.id
        setFileId(uploadedFileId)
        form.setFieldValue('fileId', uploadedFileId)
        messageApi.success('文件上传成功')
      } else {
        messageApi.error(`文件上传失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      messageApi.error('文件上传失败')
    } finally {
      setFileUploading(false)
    }
    return false // 阻止默认上传行为
  }

  const handleIsSharedChange = (checked: boolean) => {
    setIsShared(checked)
    if (checked) {
      form.setFieldValue('direction', undefined)
    }
  }

  const title =
    mode === 'create'
      ? type === 'consultation'
        ? '新建咨询群二维码'
        : '新建考核群二维码'
      : type === 'consultation'
        ? '编辑咨询群二维码'
        : '编辑考核群二维码'

  return (
    <Drawer
      title={title}
      open={open}
      width={600}
      onClose={onCancel}
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
        {type === 'assessment' && (
          <>
            <Form.Item
              label="考核轮次"
              name="epoch"
              rules={[{ required: true, message: '请输入考核轮次' }]}
            >
              <InputNumber
                placeholder="例如：1"
                min={1}
                step={1}
                style={{ width: '100%' }}
              />
            </Form.Item>

            <Form.Item label="三方向共用" name="isShared" valuePropName="checked">
              <Switch
                checked={isShared}
                onChange={handleIsSharedChange}
                checkedChildren="是"
                unCheckedChildren="否"
              />
            </Form.Item>

            {!isShared && (
              <Form.Item
                label="方向"
                name="direction"
                rules={[{ required: !isShared, message: '请选择方向' }]}
              >
                <Select placeholder="请选择方向">
                  {DIRECTION_OPTIONS.map((option) => (
                    <Option key={option.value} value={option.value}>
                      {option.label}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            )}
          </>
        )}

        <Form.Item label="二维码图片" extra="支持 JPG/PNG 格式，建议尺寸 300x300">
          <Spin spinning={fileUploading}>
            <div className="flex flex-col gap-2">
              {fileId && (
                <Image
                  src={`${API_BASE_URL}/file/download/${fileId}`}
                  alt="二维码图片"
                  className="max-w-[300px] h-auto rounded-lg"
                />
              )}
              <Upload
                beforeUpload={handleFileUpload}
                showUploadList={false}
                accept=".jpg,.jpeg,.png"
                maxCount={1}
              >
                <Button icon={<UploadOutlined />} loading={fileUploading}>
                  {fileId ? '重新上传图片' : '上传图片'}
                </Button>
              </Upload>
            </div>
          </Spin>
          <Form.Item name="fileId" hidden>
            <Input />
          </Form.Item>
        </Form.Item>
      </Form>
    </Drawer>
  )
}
