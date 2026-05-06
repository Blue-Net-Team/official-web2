'use client'

import { useEffect, useState } from 'react'
import { App, Button, Drawer, Form, Input } from 'antd'
import type { CollegeDTO, CreateCollegeRequestDTO } from '@/apis/schema/type'
import { adminCollegeService } from '@/apis/services/admin-college.service'

export type DrawerMode = 'create' | 'edit'

interface CollegeDrawerProps {
  open: boolean
  college: CollegeDTO | null
  mode: DrawerMode
  onClose: () => void
  onSuccess: () => void
}

export default function CollegeDrawer({
  open,
  college,
  mode,
  onClose,
  onSuccess,
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

  const title = mode === 'create' ? '新增学院' : '编辑学院'

  return (
    <Drawer
      title={title}
      placement="right"
      width={480}
      open={open}
      onClose={onClose}
      footer={
        <div className="flex justify-end gap-2">
          <Button onClick={onClose}>取消</Button>
          <Button type="primary" loading={saving} onClick={handleSave}>
            {mode === 'create' ? '创建' : '保存'}
          </Button>
        </div>
      }
    >
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
    </Drawer>
  )
}
