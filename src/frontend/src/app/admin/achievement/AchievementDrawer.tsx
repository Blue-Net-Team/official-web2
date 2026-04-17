'use client'

import { useEffect, useState } from 'react'
import { App, Button, Drawer, Form, Input, Select, DatePicker, Upload, Spin, Image } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import type {
  AchievementDTO,
  AchievementType,
  AwardLevel,
  CreateAchievementRequestDTO,
  UpdateAchievementRequestDTO,
} from '@/apis/schema/type'
import { ACHIEVEMENT_TYPE_LABELS, AWARD_LEVEL_LABELS } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'
import { fileService } from '@/apis/services/file.service'
import { adminAchievementService } from '@/apis/services/admin-achievement.service'

export type DrawerMode = 'create' | 'edit'

interface AchievementFormValues {
  title: string
  type?: AchievementType
  relateTo?: string | null
  achieveAt?: Dayjs | null
  awardLevel?: AwardLevel | null
  awardName?: string | null
  fileId?: number | null
}

interface AchievementDrawerProps {
  open: boolean
  mode: DrawerMode
  record: AchievementDTO | null
  onSuccess: () => void
  onCancel: () => void
}

const { Option } = Select

const FILE_TYPE = 'NORMAL_IMG' as const

export default function AchievementDrawer({
  open,
  mode,
  record,
  onSuccess,
  onCancel,
}: AchievementDrawerProps) {
  const [form] = Form.useForm<AchievementFormValues>()
  const { message: messageApi } = App.useApp()
  const [saving, setSaving] = useState(false)
  const [fileUploading, setFileUploading] = useState(false)
  const [fileId, setFileId] = useState<number | null>(null)
  const [selectedType, setSelectedType] = useState<AchievementType | undefined>()

  // 当打开或数据/模式变化时重置表单
  useEffect(() => {
    if (open) {
      if (mode === 'create') {
        form.resetFields()
        setFileId(null)
        setSelectedType(undefined)
      } else if (record) {
        form.setFieldsValue({
          title: record.title,
          type: record.type,
          relateTo: record.relateTo,
          achieveAt: record.achieveAt ? dayjs(record.achieveAt) : null,
          awardLevel: record.awardLevel,
          awardName: record.awardName,
          fileId: record.fileId,
        })
        setFileId(record.fileId)
        setSelectedType(record.type)
      }
    }
  }, [open, mode, record, form])

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      const achieveAt = values.achieveAt ? dayjs(values.achieveAt).format('YYYY-MM-DD') : null

      setSaving(true)
      if (mode === 'create') {
        const payload: CreateAchievementRequestDTO = {
          title: values.title,
          type: values.type!,
          relateTo: values.relateTo || null,
          achieveAt: achieveAt!,
          awardLevel: values.type === 'COMPETITION' ? values.awardLevel || null : null,
          awardName: values.type === 'COMPETITION' ? values.awardName || null : null,
          fileId: fileId!,
        }
        await adminAchievementService.create(payload)
        messageApi.success('创建成功')
      } else if (record) {
        const payload: UpdateAchievementRequestDTO = {
          title: values.title,
          type: values.type,
          relateTo: values.relateTo || null,
          achieveAt: achieveAt ?? undefined,
          awardLevel: values.type === 'COMPETITION' ? values.awardLevel || null : null,
          awardName: values.type === 'COMPETITION' ? values.awardName || null : null,
          fileId: fileId ?? undefined,
        }
        await adminAchievementService.update(record.id, payload)
        messageApi.success('更新成功')
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

  const handleTypeChange = (value: AchievementType) => {
    setSelectedType(value)
    // 如果类型不是 COMPETITION，清空奖项级别和名称
    if (value !== 'COMPETITION') {
      form.setFieldsValue({
        awardLevel: undefined,
        awardName: undefined,
      })
    }
  }

  const title = mode === 'create' ? '新建成就' : '编辑成就'

  return (
    <Drawer
      title={title}
      open={open}
      width={600}
      onClose={onCancel}
      footer={
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <Button onClick={onCancel}>取消</Button>
          <Button type="primary" onClick={handleSave} loading={saving}>
            保存
          </Button>
        </div>
      }
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="成就标题"
          name="title"
          rules={[{ required: true, message: '请输入成就标题' }]}
        >
          <Input placeholder="例如：蓝桥杯全国一等奖" maxLength={200} />
        </Form.Item>

        <Form.Item
          label="成就类型"
          name="type"
          rules={[{ required: true, message: '请选择成就类型' }]}
        >
          <Select placeholder="请选择成就类型" onChange={handleTypeChange}>
            {Object.entries(ACHIEVEMENT_TYPE_LABELS).map(([value, label]) => (
              <Option key={value} value={value}>
                {label}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          label="关联项"
          name="relateTo"
          tooltip="竞赛为赛项名，论文为期刊名，专利可为空"
          rules={[{ required: selectedType != 'PATENT', message: '请输入关联项' }]}
        >
          <Input placeholder="例如：蓝桥杯、计算机学报" maxLength={100} />
        </Form.Item>

        <Form.Item
          label="获奖日期"
          name="achieveAt"
          rules={[{ required: true, message: '请选择获奖日期' }]}
        >
          <DatePicker style={{ width: '100%' }} placeholder="请选择日期" format="YYYY-MM-DD" />
        </Form.Item>

        {selectedType === 'COMPETITION' && (
          <>
            <Form.Item
              label="奖项级别"
              name="awardLevel"
              rules={[{ required: selectedType === 'COMPETITION', message: '请选择奖项级别' }]}
            >
              <Select placeholder="请选择奖项级别">
                {Object.entries(AWARD_LEVEL_LABELS).map(([value, label]) => (
                  <Option key={value} value={value}>
                    {label}
                  </Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              label="奖项名称"
              name="awardName"
              rules={[{ required: selectedType === 'COMPETITION', message: '请输入奖项名称' }]}
            >
              <Input placeholder="例如：一等奖、金奖" maxLength={50} />
            </Form.Item>
          </>
        )}

        <Form.Item label="成就图片" extra="支持 JPG/PNG 格式，建议尺寸 800x600">
          <Spin spinning={fileUploading}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {fileId && (
                <Image
                  src={`${API_BASE_URL}/file/download/${fileId}`}
                  alt="成就图片"
                  style={{ maxWidth: '100%', height: 'auto', borderRadius: 8 }}
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
