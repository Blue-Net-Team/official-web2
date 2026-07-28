'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import {
  App,
  Button,
  Drawer,
  Form,
  Input,
  Mentions,
  Select,
  DatePicker,
  Upload,
  Spin,
  Image,
} from 'antd'
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
import { adminUserService } from '@/apis/services/admin-user.service'
import { competitionService } from '@/apis/services/competition.service'

export type DrawerMode = 'create' | 'edit'

interface AchievementFormValues {
  title: string
  type?: AchievementType
  relateTo?: string | null
  achieveAt?: Dayjs | null
  awardLevel?: AwardLevel | null
  awardName?: string | null
  fileId?: number | null
  /** Mentions 文本，形如 "@昵称1 @昵称2" */
  members?: string
  /** 外部协作者姓名标签 */
  externalMembers?: string[]
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
  const [competitionOptions, setCompetitionOptions] = useState<{ value: string; label: string }[]>(
    []
  )
  const [memberOptions, setMemberOptions] = useState<{ value: string; label: string }[]>([])
  const [memberSearching, setMemberSearching] = useState(false)
  const membersText = Form.useWatch('members', form) || ''
  // 展示名 → 用户ID 映射，用于提交时从 Mentions 文本反解 userIds
  const memberMapRef = useRef<Map<string, number>>(new Map())
  // 全量成员列表是否已加载（Mentions 的 onSearch 仅在输入 @ 时触发，搜索交由客户端过滤）
  const membersLoadedRef = useRef(false)

  // 登记成员展示名映射，重名时追加 #学号 消歧
  const registerMember = (id: number, display: string, studentId?: string) => {
    let key = display
    const existing = memberMapRef.current.get(key)
    if (existing !== undefined && existing !== id) {
      key = studentId ? `${display}#${studentId}` : `${display}#${id}`
    }
    memberMapRef.current.set(key, id)
    return key
  }

  // 加载竞赛列表用于关联项选择
  useEffect(() => {
    const fetchCompetitions = async () => {
      try {
        const res = await competitionService.getAllCompetitions()
        if (res.code === 200 && res.data) {
          setCompetitionOptions(res.data.map((c) => ({ value: c.name, label: c.name })))
        }
      } catch (error) {
        console.error('Failed to fetch competitions:', error)
      }
    }
    fetchCompetitions()
  }, [])

  // 首次输入 @ 时加载全量成员列表，后续按 Mentions 客户端过滤
  const handleMemberSearch = async () => {
    if (membersLoadedRef.current) {
      return
    }
    setMemberSearching(true)
    try {
      const res = await adminUserService.getList({ size: 100 })
      if (res.code === 200 && res.data) {
        const options = res.data.content
          // 过滤内置 system 用户，避免被关联到成就
          .filter((user) => user.username !== 'system')
          .map((user) => {
            // 以姓名作为 @ 展示名，与成就回显的 members[].username 保持一致
            const key = registerMember(user.id, user.username, user.studentId)
            return {
              value: key,
              label: user.nickname ? `${key}（${user.nickname}）` : key,
            }
          })
        setMemberOptions(options)
        membersLoadedRef.current = true
      }
    } catch (error) {
      console.error('Failed to search members:', error)
    } finally {
      setMemberSearching(false)
    }
  }

  // 从当前 Mentions 文本中解析已选中的成员 ID，避免重复选择
  const selectedMemberIds = useMemo(() => {
    const ids = new Set<number>()
    const text = String(membersText)
    memberMapRef.current.forEach((id, display) => {
      if (text.includes(`@${display}`)) {
        ids.add(id)
      }
    })
    return ids
  }, [membersText])

  // 下拉选项中排除已选成员
  const availableMemberOptions = useMemo(() => {
    return memberOptions.filter((option) => {
      const id = memberMapRef.current.get(option.value)
      return id !== undefined && !selectedMemberIds.has(id)
    })
  }, [memberOptions, selectedMemberIds])

  // 当打开或数据/模式变化时重置表单
  useEffect(() => {
    if (open) {
      if (mode === 'create') {
        form.resetFields()
        setFileId(null)
        setSelectedType(undefined)
      } else if (record) {
        // 回显系统内成员：登记姓名映射并拼接 Mentions 文本
        const membersText = (record.members || [])
          .map((member) => `@${registerMember(member.userId, member.username)}`)
          .join(' ')
        form.setFieldsValue({
          title: record.title,
          type: record.type,
          relateTo: record.relateTo,
          achieveAt: record.achieveAt ? dayjs(record.achieveAt) : null,
          awardLevel: record.awardLevel,
          awardName: record.awardName,
          fileId: record.fileId,
          members: membersText,
          externalMembers: record.externalMembers || [],
        })
        setFileId(record.fileId)
        setSelectedType(record.type)
      }
    }
  }, [open, mode, record, form])

  // 从 Mentions 文本中反解已选系统内成员的用户ID
  const extractUserIds = (mentionsText: string): number[] => {
    const userIds: number[] = []
    memberMapRef.current.forEach((id, display) => {
      if (mentionsText.includes(`@${display}`) && !userIds.includes(id)) {
        userIds.push(id)
      }
    })
    return userIds
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      const achieveAt = values.achieveAt ? dayjs(values.achieveAt).format('YYYY-MM-DD') : null
      const relateTo = values.relateTo?.trim() || null
      const userIds = extractUserIds(values.members || '')
      const externalMembers = (values.externalMembers || [])
        .map((name) => name.trim())
        .filter((name) => name.length > 0)

      setSaving(true)
      if (mode === 'create') {
        const payload: CreateAchievementRequestDTO = {
          title: values.title,
          type: values.type!,
          relateTo,
          achieveAt: achieveAt!,
          awardLevel: values.type === 'COMPETITION' ? values.awardLevel || null : null,
          awardName: values.type === 'COMPETITION' ? values.awardName || null : null,
          fileId: fileId!,
          userIds,
          externalMembers,
        }
        await adminAchievementService.create(payload)
        messageApi.success('创建成功')
      } else if (record) {
        const payload: UpdateAchievementRequestDTO = {
          title: values.title,
          type: values.type,
          relateTo,
          achieveAt: achieveAt ?? undefined,
          awardLevel: values.type === 'COMPETITION' ? values.awardLevel || null : null,
          awardName: values.type === 'COMPETITION' ? values.awardName || null : null,
          fileId: fileId ?? undefined,
          userIds,
          externalMembers,
        }
        await adminAchievementService.update(record.id, payload)
        messageApi.success('更新成功')
      }
      onSuccess()
    } catch (error) {
      console.error('保存失败:', error)
      const err = error as { response?: { data?: { msg?: string } } }
      messageApi.error(err.response?.data?.msg || '保存失败')
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
          tooltip={
            selectedType === 'COMPETITION'
              ? '请选择已有竞赛或输入新的竞赛名称'
              : '竞赛为赛项名，论文为期刊名，专利可为空'
          }
          rules={[{ required: selectedType != 'PATENT', message: '请输入关联项' }]}
        >
          {selectedType === 'COMPETITION' ? (
            <Select
              showSearch
              allowClear
              placeholder="请选择或输入竞赛名称"
              options={competitionOptions}
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              onChange={(value) => form.setFieldValue('relateTo', value)}
            />
          ) : (
            <Input placeholder="例如：蓝桥杯、计算机学报" maxLength={100} />
          )}
        </Form.Item>

        <Form.Item
          label="获奖日期"
          name="achieveAt"
          rules={[{ required: true, message: '请选择获奖日期' }]}
        >
          <DatePicker className="w-full" placeholder="请选择日期" format="YYYY-MM-DD" />
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

        <Form.Item
          label="系统内成员"
          name="members"
          tooltip="输入 @ 搜索并选择系统内成员，可关联多名成员"
        >
          <Mentions
            placeholder="输入 @ 搜索系统内成员"
            options={availableMemberOptions}
            onSearch={handleMemberSearch}
            loading={memberSearching}
            autoSize={{ minRows: 2, maxRows: 4 }}
          />
        </Form.Item>

        <Form.Item
          label="外部协作者"
          name="externalMembers"
          tooltip="非本系统用户的合作成员，输入姓名后回车添加"
        >
          <Select
            mode="tags"
            placeholder="输入姓名后回车添加，例如：张三-外校"
            open={false}
            suffixIcon={null}
            tokenSeparators={[',', '，']}
            maxCount={20}
          />
        </Form.Item>

        <Form.Item label="成就图片" extra="支持 JPG/PNG 格式，建议尺寸 800x600">
          <Spin spinning={fileUploading}>
            <div className="flex flex-col gap-2">
              {fileId && (
                <Image
                  src={`${API_BASE_URL}/file/download/${fileId}`}
                  alt="成就图片"
                  className="max-w-full h-auto rounded-lg"
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
