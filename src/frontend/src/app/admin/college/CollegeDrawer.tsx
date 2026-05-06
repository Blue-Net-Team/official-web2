'use client'

import { useEffect, useState } from 'react'
import { App, Button, Descriptions, Drawer, Form, Input } from 'antd'
import { DeleteOutlined, EditOutlined } from '@ant-design/icons'
import type { CollegeDTO, CreateCollegeRequestDTO } from '@/apis/schema/type'
import { adminCollegeService } from '@/apis/services/admin-college.service'

export type DrawerMode = 'view' | 'edit' | 'create'

interface CollegeDrawerProps {
  open: boolean
  college: CollegeDTO | null
  mode: DrawerMode
  onClose: () => void
  onSuccess: () => void
  onDelete: (college: CollegeDTO) => void
  onEdit: () => void
}

export default function CollegeDrawer({
  open,
  college,
  mode,
  onClose,
  onSuccess,
  onDelete,
  onEdit,
}: CollegeDrawerProps) {
  const [form] = Form.useForm<CreateCollegeRequestDTO>()
  const { message: messageApi } = App.useApp()
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (open) {
      if (mode === 'create') {
        form.resetFields()
      } else if (college) {
        form.setFieldsValue({ name: college.name })
      }
    }
  }, [open, mode, college, form])

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      if (mode === 'create') {
        await adminCollegeService.create(values)
      } else if (college) {
        await adminCollegeService.update(college.id, values)
      }
      onSuccess()
    } catch {
      messageApi.error('保存失败')
    } finally {
      setSaving(false)
    }
  }

  const title =
    mode === 'create' ? '新增学院' : mode === 'edit' ? '编辑学院' : college?.name || '学院详情'

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
          {mode === 'view' && college ? (
            <>
              <Button danger icon={<DeleteOutlined />} onClick={() => onDelete(college)}>
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
      {mode === 'view' && college ? <DetailView college={college} /> : <FormView form={form} />}
    </Drawer>
  )
}

function DetailView({ college }: { college: CollegeDTO }) {
  return (
    <div className="p-4">
      <Descriptions column={1} size="small">
        <Descriptions.Item label="学院ID">{college.id}</Descriptions.Item>
        <Descriptions.Item label="学院名称">{college.name}</Descriptions.Item>
      </Descriptions>
    </div>
  )
}

function FormView({ form }: { form: ReturnType<typeof Form.useForm<CreateCollegeRequestDTO>>[0] }) {
  return (
    <div className="p-4">
      <Form form={form} layout="vertical" size="middle">
        <Form.Item
          name="name"
          label="学院名称"
          rules={[
            { required: true, message: '请输入学院名称' },
            { max: 100, message: '学院名称最多100个字符' },
          ]}
        >
          <Input placeholder="请输入学院名称" maxLength={100} />
        </Form.Item>
      </Form>
    </div>
  )
}
