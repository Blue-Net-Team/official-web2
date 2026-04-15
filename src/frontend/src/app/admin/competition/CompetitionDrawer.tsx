'use client'

import { useEffect, useState } from 'react'
import {
  Button,
  Descriptions,
  Divider,
  Drawer,
  Form,
  Image,
  Input,
  Select,
  Spin,
  Upload,
} from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import type {
  CompetitionLevel,
  CompetitionRequestDTO,
  CompetitionResponseDTO,
} from '@/apis/schema/type'
import { COMPETITION_LEVEL_LABELS, COMPETITION_LEVEL_COLORS } from '@/types/competition'
import { fileService } from '@/apis/services/file.service'
import { API_BASE_URL } from '@/apis/config'
import { adminCompetitionService } from '@/apis/services/admin-competition.service'

export type DrawerMode = 'view' | 'edit' | 'create'

interface CompetitionDrawerProps {
  open: boolean
  competition: CompetitionResponseDTO | null
  mode: DrawerMode
  onClose: () => void
  onSuccess: () => void
  onDelete: (competition: CompetitionResponseDTO) => void
  onEdit: () => void
}

const LEVEL_OPTIONS: { value: CompetitionLevel; label: string }[] = [
  { value: 'national', label: '国家级' },
  { value: 'provincial', label: '省级' },
  { value: 'school', label: '校级' },
]

export default function CompetitionDrawer({
  open,
  competition,
  mode,
  onClose,
  onSuccess,
  onDelete,
  onEdit,
}: CompetitionDrawerProps) {
  const [form] = Form.useForm<CompetitionRequestDTO>()
  const [saving, setSaving] = useState(false)
  const [logoUploading, setLogoUploading] = useState(false)
  const [coverUploading, setCoverUploading] = useState(false)
  const [logoFileId, setLogoFileId] = useState<number | null>(null)
  const [coverFileId, setCoverFileId] = useState<number | null>(null)

  // 当打开或数据/模式变化时重置表单
  useEffect(() => {
    if (open) {
      if (mode === 'create') {
        form.resetFields()
        setLogoFileId(null)
        setCoverFileId(null)
      } else if (competition) {
        form.setFieldsValue({
          name: competition.name,
          shortName: competition.shortName,
          level: competition.level,
          month: competition.month,
          organizer: competition.organizer,
          summary: competition.summary,
          logoFileId: competition.logoFileId,
          coverFileId: competition.coverFileId,
        })
        setLogoFileId(competition.logoFileId)
        setCoverFileId(competition.coverFileId)
      }
    }
  }, [open, mode, competition, form])

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      if (mode === 'create') {
        await adminCompetitionService.create(values)
      } else if (competition) {
        await adminCompetitionService.update(competition.id, values)
      }
      onSuccess()
    } finally {
      setSaving(false)
    }
  }

  const handleLogoUpload = async (file: File) => {
    setLogoUploading(true)
    try {
      const res = await fileService.upload(file, 'NORMAL_IMG')
      if (res.code === 200 && res.data) {
        setLogoFileId(res.data.id)
        form.setFieldValue('logoFileId', res.data.id)
      }
    } finally {
      setLogoUploading(false)
    }
    return false
  }

  const handleCoverUpload = async (file: File) => {
    setCoverUploading(true)
    try {
      const res = await fileService.upload(file, 'NORMAL_IMG')
      if (res.code === 200 && res.data) {
        setCoverFileId(res.data.id)
        form.setFieldValue('coverFileId', res.data.id)
      }
    } finally {
      setCoverUploading(false)
    }
    return false
  }

  const title =
    mode === 'create'
      ? '新建竞赛'
      : mode === 'edit'
        ? '编辑竞赛'
        : (competition?.name ?? '竞赛详情')

  return (
    <Drawer
      title={title}
      placement="right"
      width={480}
      open={open}
      onClose={onClose}
      styles={{ body: { padding: 0 } }}
      footer={
        <div className="flex justify-end gap-2">
          {mode === 'view' && competition ? (
            <>
              <Button danger icon={<DeleteOutlined />} onClick={() => onDelete(competition)}>
                删除
              </Button>
              <Button type="primary" icon={<EditOutlined />} onClick={onEdit}>
                编辑
              </Button>
            </>
          ) : (
            <>
              <Button onClick={onClose}>取消</Button>
              <Button type="primary" loading={saving} onClick={handleSave}>
                {mode === 'create' ? '创建' : '保存'}
              </Button>
            </>
          )}
        </div>
      }
    >
      {mode === 'view' && competition ? (
        <DetailView competition={competition} />
      ) : (
        <FormView
          form={form}
          logoFileId={logoFileId}
          coverFileId={coverFileId}
          logoUploading={logoUploading}
          coverUploading={coverUploading}
          onLogoUpload={handleLogoUpload}
          onCoverUpload={handleCoverUpload}
        />
      )}
    </Drawer>
  )
}

/** 查看模式 */
function DetailView({ competition }: { competition: CompetitionResponseDTO }) {
  return (
    <div className="p-4">
      <Descriptions column={1} size="small">
        <Descriptions.Item label="名称">{competition.name}</Descriptions.Item>
        {competition.shortName && (
          <Descriptions.Item label="简称">{competition.shortName}</Descriptions.Item>
        )}
        <Descriptions.Item label="级别">
          <span
            style={{
              color: COMPETITION_LEVEL_COLORS[competition.level as CompetitionLevel],
            }}
          >
            {COMPETITION_LEVEL_LABELS[competition.level as CompetitionLevel]}
          </span>
        </Descriptions.Item>
        {competition.month && (
          <Descriptions.Item label="月份">{competition.month}</Descriptions.Item>
        )}
        {competition.organizer && (
          <Descriptions.Item label="主办方">{competition.organizer}</Descriptions.Item>
        )}
        {competition.summary && (
          <Descriptions.Item label="简介">{competition.summary}</Descriptions.Item>
        )}
      </Descriptions>

      {/* Logo 预览 */}
      {competition.logoFileId && (
        <>
          <Divider>Logo</Divider>
          <Image
            src={`${API_BASE_URL}/file/download/${competition.logoFileId}`}
            alt="Logo"
            width={120}
            className="rounded-lg"
          />
        </>
      )}

      {/* 封面预览 */}
      {competition.coverFileId && (
        <>
          <Divider>封面</Divider>
          <Image
            src={`${API_BASE_URL}/file/download/${competition.coverFileId}`}
            alt="封面"
            width={200}
            className="rounded-lg"
          />
        </>
      )}
    </div>
  )
}

/** 编辑/创建模式 */
function FormView({
  form,
  logoFileId,
  coverFileId,
  logoUploading,
  coverUploading,
  onLogoUpload,
  onCoverUpload,
}: {
  form: ReturnType<typeof Form.useForm<CompetitionRequestDTO>>[0]
  logoFileId: number | null
  coverFileId: number | null
  logoUploading: boolean
  coverUploading: boolean
  onLogoUpload: (file: File) => void
  onCoverUpload: (file: File) => void
}) {
  return (
    <div className="p-4">
      <Form form={form} layout="vertical" size="middle">
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入竞赛名称' }]}>
          <Input placeholder="竞赛名称" maxLength={100} />
        </Form.Item>

        <Form.Item name="shortName" label="简称">
          <Input placeholder="竞赛简称" maxLength={50} />
        </Form.Item>

        <Form.Item name="level" label="级别">
          <Select options={LEVEL_OPTIONS} placeholder="选择级别" />
        </Form.Item>

        <Form.Item name="month" label="月份">
          <Input placeholder="如：4月" maxLength={10} />
        </Form.Item>

        <Form.Item name="organizer" label="主办方">
          <Input placeholder="主办单位" maxLength={200} />
        </Form.Item>

        <Form.Item name="summary" label="简介">
          <Input.TextArea placeholder="竞赛简介" maxLength={500} showCount rows={3} />
        </Form.Item>

        <Form.Item name="logoFileId" label="Logo" hidden>
          <Input />
        </Form.Item>

        <Form.Item label="Logo 图片">
          <Upload accept="image/*" showUploadList={false} beforeUpload={onLogoUpload}>
            {logoFileId ? (
              <Spin spinning={logoUploading}>
                <Image
                  src={`${API_BASE_URL}/file/download/${logoFileId}`}
                  alt="Logo"
                  width={100}
                  className="rounded-lg"
                  preview={false}
                />
              </Spin>
            ) : (
              <button
                className="flex items-center justify-center w-[100px] h-[100px] rounded-lg border border-dashed border-white/20 hover:border-white/40 transition-colors"
                type="button"
              >
                <PlusOutlined className="text-lg text-white/40" />
              </button>
            )}
          </Upload>
        </Form.Item>

        <Form.Item name="coverFileId" label="封面" hidden>
          <Input />
        </Form.Item>

        <Form.Item label="封面图片">
          <Upload accept="image/*" showUploadList={false} beforeUpload={onCoverUpload}>
            {coverFileId ? (
              <Spin spinning={coverUploading}>
                <Image
                  src={`${API_BASE_URL}/file/download/${coverFileId}`}
                  alt="封面"
                  width={200}
                  className="rounded-lg"
                  preview={false}
                />
              </Spin>
            ) : (
              <button
                className="flex items-center justify-center w-[200px] h-[120px] rounded-lg border border-dashed border-white/20 hover:border-white/40 transition-colors"
                type="button"
              >
                <PlusOutlined className="text-lg text-white/40" />
              </button>
            )}
          </Upload>
        </Form.Item>
      </Form>
    </div>
  )
}
