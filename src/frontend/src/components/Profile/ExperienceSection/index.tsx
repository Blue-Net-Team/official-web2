'use client'

import { useState } from 'react'
import type { UserExperience } from '@/apis/schema/type'
import type { ExperienceType } from '@/apis/schema/enumerate'
import {
  FolderOutlined,
  TrophyOutlined,
  SolutionOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
} from '@ant-design/icons'
import { Button, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import ExperienceCard from '../ExperienceCard'

interface ExperienceSectionProps {
  type: ExperienceType
  title: string
  data: UserExperience[]
  onAdd?: (data: Omit<UserExperience, 'id'>) => Promise<void>
  onUpdate?: (id: string, data: Partial<UserExperience>) => Promise<void>
  onDelete?: (id: string) => Promise<void>
  /** 只读模式：隐藏添加/编辑/删除操作 */
  readOnly?: boolean
}

export default function ExperienceSection({
  type,
  title,
  data,
  onAdd,
  onUpdate,
  onDelete,
  readOnly = false,
}: ExperienceSectionProps) {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<UserExperience | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [form] = Form.useForm()

  const handleOpenModal = (item?: UserExperience) => {
    if (item) {
      setEditingItem(item)
      form.setFieldsValue(item)
    } else {
      setEditingItem(null)
      form.resetFields()
    }
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setEditingItem(null)
    form.resetFields()
  }

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (readOnly) return
    setIsSubmitting(true)
    try {
      const submitData = { ...values, type } as Omit<UserExperience, 'id'>
      if (editingItem) {
        await onUpdate?.(editingItem.id, values as Partial<UserExperience>)
        message.success('更新成功')
      } else {
        await onAdd?.(submitData)
        message.success('添加成功')
      }
      handleCloseModal()
    } catch {
      message.error('操作失败，请重试')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async (id: string) => {
    if (readOnly) return
    try {
      await onDelete?.(id)
      message.success('删除成功')
    } catch {
      message.error('删除失败')
    }
  }

  const getIcon = () => {
    switch (type) {
      case 'PROJECT':
        return <FolderOutlined />
      case 'COMPETITION':
        return <TrophyOutlined />
      case 'INTERNSHIP':
        return <SolutionOutlined />
    }
  }

  const renderFormItems = () => {
    switch (type) {
      case 'PROJECT':
        return (
          <>
            <Form.Item name="name" label="项目名称" rules={[{ required: true }]}>
              <Input placeholder="请输入项目名称" />
            </Form.Item>
            <Form.Item name="role" label="担任角色" rules={[{ required: true }]}>
              <Input placeholder="如：项目负责人、核心开发" />
            </Form.Item>
            <div className="flex gap-4">
              <Form.Item
                name="startDate"
                label="开始时间"
                rules={[{ required: true }]}
                className="flex-1"
              >
                <Input placeholder="如：2024.09" />
              </Form.Item>
              <Form.Item name="endDate" label="结束时间" className="flex-1">
                <Input placeholder="如：2025.01（进行中可不填）" />
              </Form.Item>
            </div>
            <Form.Item name="description" label="项目描述" rules={[{ required: true }]}>
              <Input.TextArea rows={4} placeholder="描述项目内容、你的贡献和成果" />
            </Form.Item>
            <Form.Item name="techStack" label="技术栈">
              <Select mode="tags" placeholder="输入技术标签，回车添加" options={[]} />
            </Form.Item>
            <Form.Item name="demoUrl" label="演示链接">
              <Input placeholder="https://..." />
            </Form.Item>
          </>
        )
      case 'COMPETITION':
        return (
          <>
            <Form.Item name="name" label="竞赛名称" rules={[{ required: true }]}>
              <Input placeholder="请输入竞赛名称" />
            </Form.Item>
            <Form.Item name="role" label="担任角色" rules={[{ required: true }]}>
              <Input placeholder="如：团队负责人、技术负责人" />
            </Form.Item>
            <div className="flex gap-4">
              <Form.Item name="startDate" label="开始时间" className="flex-1">
                <Input placeholder="如：2024.08" />
              </Form.Item>
              <Form.Item name="endDate" label="结束时间" className="flex-1">
                <Input placeholder="如：2024.08" />
              </Form.Item>
            </div>
            <Form.Item name="date" label="参赛时间" rules={[{ required: true }]}>
              <Input placeholder="如：2024年8月" />
            </Form.Item>
            <Form.Item name="level" label="竞赛级别">
              <Select
                options={[
                  { value: '国家级', label: '国家级' },
                  { value: '省级', label: '省级' },
                  { value: '区域赛', label: '区域赛' },
                  { value: '校级', label: '校级' },
                ]}
                placeholder="请选择竞赛级别"
              />
            </Form.Item>
            <Form.Item name="award" label="获奖等级" rules={[{ required: true }]}>
              <Select
                options={[
                  { value: '一等奖', label: '一等奖' },
                  { value: '二等奖', label: '二等奖' },
                  { value: '三等奖', label: '三等奖' },
                  { value: '铜牌', label: '铜牌' },
                  { value: '参与奖', label: '参与奖' },
                ]}
                placeholder="请选择获奖等级"
              />
            </Form.Item>
            <Form.Item name="teamSize" label="团队人数" rules={[{ required: true }]}>
              <Input type="number" placeholder="请输入团队人数" />
            </Form.Item>
            <Form.Item name="description" label="竞赛描述" rules={[{ required: true }]}>
              <Input.TextArea rows={4} placeholder="描述参赛作品、你的贡献和收获" />
            </Form.Item>
            <Form.Item name="certificateUrl" label="获奖证书链接">
              <Input placeholder="证书图片链接（如有）" />
            </Form.Item>
          </>
        )
      case 'INTERNSHIP':
        return (
          <>
            <Form.Item name="company" label="公司名称" rules={[{ required: true }]}>
              <Input placeholder="请输入公司名称" />
            </Form.Item>
            <Form.Item name="position" label="职位" rules={[{ required: true }]}>
              <Input placeholder="如：算法实习生" />
            </Form.Item>
            <div className="flex gap-4">
              <Form.Item
                name="startDate"
                label="开始时间"
                rules={[{ required: true }]}
                className="flex-1"
              >
                <Input placeholder="如：2024.06" />
              </Form.Item>
              <Form.Item name="endDate" label="结束时间" className="flex-1">
                <Input placeholder="如：2024.09（在职可不填）" />
              </Form.Item>
            </div>
            <Form.Item name="status" label="实习状态" rules={[{ required: true }]}>
              <Select
                options={[
                  { value: 'ACTIVE', label: '在职' },
                  { value: 'ENDED', label: '已离职' },
                ]}
                placeholder="请选择状态"
              />
            </Form.Item>
            <Form.Item name="description" label="工作描述" rules={[{ required: true }]}>
              <Input.TextArea rows={4} placeholder="描述工作内容、技术栈等" />
            </Form.Item>
            <Form.Item name="achievements" label="主要成就">
              <Select mode="tags" placeholder="输入成就，回车添加" options={[]} />
            </Form.Item>
          </>
        )
    }
  }

  const renderCardActions = (item: UserExperience) => {
    if (readOnly) return undefined
    return (
      <div className="flex gap-2">
        <button
          className="w-8 h-8 rounded-lg bg-transparent border border-[rgba(255,255,255,0.1)] flex items-center justify-center cursor-pointer transition-all duration-300 hover:bg-[rgba(102,119,255,0.1)] hover:border-[#6677ff] [&>svg]:w-4 [&>svg]:h-4 [&>svg]:text-[#6677ff]"
          onClick={() => handleOpenModal(item)}
        >
          <EditOutlined />
        </button>
        <Popconfirm
          title="确认删除"
          description="确定要删除这条记录吗？"
          onConfirm={() => handleDelete(item.id)}
          okText="确定"
          cancelText="取消"
        >
          <button className="w-8 h-8 rounded-lg bg-transparent border border-[rgba(255,255,255,0.1)] flex items-center justify-center cursor-pointer transition-all duration-300 hover:bg-[rgba(255,107,53,0.1)] hover:border-[#ff6b35] [&>svg]:w-4 [&>svg]:h-4 [&>svg]:text-[#ff6b35]">
            <DeleteOutlined />
          </button>
        </Popconfirm>
      </div>
    )
  }

  return (
    <div className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-6 max-[640px]:p-4">
      <div className="flex items-center justify-between mb-5 pb-4 border-b border-white/[0.05] max-[640px]:flex-col max-[640px]:gap-3 max-[640px]:items-start">
        <h3 className="text-lg font-semibold text-white m-0">{title}</h3>
        {!readOnly && (
          <Button
            type="primary"
            className="!px-6 !py-3 !rounded-[10px] !text-sm !font-medium cursor-pointer transition-all duration-300 !border-none !bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)] hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(102,119,255,0.4)] flex items-center gap-2"
            icon={<PlusOutlined />}
            onClick={() => handleOpenModal()}
          >
            添加{type === 'PROJECT' ? '项目' : type === 'COMPETITION' ? '竞赛' : '实习'}
          </Button>
        )}
      </div>

      {data.length > 0 ? (
        <div className="flex flex-col gap-4">
          {data.map((item) => (
            <ExperienceCard key={item.id} experience={item} actions={renderCardActions(item)} />
          ))}
        </div>
      ) : (
        <div className="text-center py-[60px] px-5 bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl">
          <div className="w-20 h-20 mx-auto mb-5 rounded-full bg-[rgba(102,119,255,0.1)] flex items-center justify-center [&>svg]:w-10 [&>svg]:h-10 [&>svg]:text-[#6677ff]">
            {getIcon()}
          </div>
          <h3 className="text-lg font-semibold text-white mb-2 m-0">
            暂无{type === 'PROJECT' ? '项目' : type === 'COMPETITION' ? '竞赛' : '实习'}经历
          </h3>
          {!readOnly && (
            <p className="text-sm text-[rgba(140,140,141,1)] m-0">点击上方按钮添加你的经历</p>
          )}
        </div>
      )}

      {!readOnly && (
        <Modal
          title={editingItem ? '编辑' : '添加'}
          open={isModalOpen}
          onCancel={handleCloseModal}
          footer={null}
          width={600}
        >
          <Form form={form} layout="vertical" onFinish={handleSubmit} className="mt-6">
            {renderFormItems()}
            <Form.Item className="mb-0 mt-6">
              <div className="flex justify-end gap-3">
                <Button onClick={handleCloseModal}>取消</Button>
                <Button type="primary" htmlType="submit" loading={isSubmitting}>
                  {editingItem ? '保存' : '添加'}
                </Button>
              </div>
            </Form.Item>
          </Form>
        </Modal>
      )}
    </div>
  )
}
