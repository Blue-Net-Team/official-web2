'use client'

import { useState } from 'react'
import type { Experience, ExperienceType, InternshipStatus } from '@/types/profile'
import {
  FolderOutlined,
  TrophyOutlined,
  SolutionOutlined,
  LinkOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  FileTextOutlined,
  TeamOutlined,
  StarOutlined,
} from '@ant-design/icons'
import { Button, Modal, Form, Input, Select, message, Popconfirm } from 'antd'

interface ExperienceSectionProps {
  type: ExperienceType
  title: string
  data: Experience[]
  onAdd: (data: Omit<Experience, 'id'>) => Promise<void>
  onUpdate: (id: string, data: Partial<Experience>) => Promise<void>
  onDelete: (id: string) => Promise<void>
}

function getAwardBadgeClass(award: string): string {
  switch (award) {
    case '一等奖':
    case 'first':
      return 'bg-[linear-gradient(135deg,#ffd700_0%,#ffa500_100%)] text-black'
    case '二等奖':
    case 'second':
      return 'bg-[linear-gradient(135deg,#c0c0c0_0%,#a0a0a0_100%)] text-black'
    case '三等奖':
    case 'third':
    case '铜牌':
      return 'bg-[linear-gradient(135deg,#cd7f32_0%,#b87333_100%)] text-white'
    default:
      return ''
  }
}

function getInternshipBadgeClass(status: InternshipStatus): string {
  return status === 'active'
    ? 'bg-[rgba(102,119,255,0.15)] text-[#6677ff]'
    : 'bg-[rgba(140,140,141,0.2)] text-[rgba(140,140,141,1)]'
}

function getInternshipStatusText(status: InternshipStatus): string {
  return status === 'active' ? '在职' : '已离职'
}

export default function ExperienceSection({
  type,
  title,
  data,
  onAdd,
  onUpdate,
  onDelete,
}: ExperienceSectionProps) {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<Experience | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [form] = Form.useForm()

  const handleOpenModal = (item?: Experience) => {
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
    setIsSubmitting(true)
    try {
      const submitData = { ...values, type } as Omit<Experience, 'id'>
      if (editingItem) {
        await onUpdate(editingItem.id, values as Partial<Experience>)
        message.success('更新成功')
      } else {
        await onAdd(submitData)
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
    try {
      await onDelete(id)
      message.success('删除成功')
    } catch {
      message.error('删除失败')
    }
  }

  const getIcon = () => {
    switch (type) {
      case 'project':
        return <FolderOutlined />
      case 'competition':
        return <TrophyOutlined />
      case 'internship':
        return <SolutionOutlined />
    }
  }

  const getIconClass = () => {
    switch (type) {
      case 'project':
        return 'bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)]'
      case 'competition':
        return 'bg-[linear-gradient(135deg,#ff6b35_0%,#ff8c42_100%)]'
      case 'internship':
        return 'bg-[linear-gradient(135deg,#059669_0%,#10b981_100%)]'
    }
  }

  const renderFormItems = () => {
    switch (type) {
      case 'project':
        return (
          <>
            <Form.Item name="name" label="项目名称" rules={[{ required: true }]}>
              <Input placeholder="请输入项目名称" />
            </Form.Item>
            <Form.Item name="role" label="担任角色" rules={[{ required: true }]}>
              <Input placeholder="如：项目负责人、核心开发" />
            </Form.Item>
            <div style={{ display: 'flex', gap: 16 }}>
              <Form.Item
                name="startDate"
                label="开始时间"
                rules={[{ required: true }]}
                style={{ flex: 1 }}
              >
                <Input placeholder="如：2024.09" />
              </Form.Item>
              <Form.Item name="endDate" label="结束时间" style={{ flex: 1 }}>
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
      case 'competition':
        return (
          <>
            <Form.Item name="name" label="竞赛名称" rules={[{ required: true }]}>
              <Input placeholder="请输入竞赛名称" />
            </Form.Item>
            <Form.Item name="role" label="担任角色" rules={[{ required: true }]}>
              <Input placeholder="如：团队负责人、技术负责人" />
            </Form.Item>
            <div style={{ display: 'flex', gap: 16 }}>
              <Form.Item name="startDate" label="开始时间" style={{ flex: 1 }}>
                <Input placeholder="如：2024.08" />
              </Form.Item>
              <Form.Item name="endDate" label="结束时间" style={{ flex: 1 }}>
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
      case 'internship':
        return (
          <>
            <Form.Item name="company" label="公司名称" rules={[{ required: true }]}>
              <Input placeholder="请输入公司名称" />
            </Form.Item>
            <Form.Item name="position" label="职位" rules={[{ required: true }]}>
              <Input placeholder="如：算法实习生" />
            </Form.Item>
            <div style={{ display: 'flex', gap: 16 }}>
              <Form.Item
                name="startDate"
                label="开始时间"
                rules={[{ required: true }]}
                style={{ flex: 1 }}
              >
                <Input placeholder="如：2024.06" />
              </Form.Item>
              <Form.Item name="endDate" label="结束时间" style={{ flex: 1 }}>
                <Input placeholder="如：2024.09（在职可不填）" />
              </Form.Item>
            </div>
            <Form.Item name="status" label="实习状态" rules={[{ required: true }]}>
              <Select
                options={[
                  { value: 'active', label: '在职' },
                  { value: 'ended', label: '已离职' },
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

  const renderItem = (item: Experience) => {
    const isCompetition = type === 'competition'
    const isInternship = type === 'internship'
    const displayName = isInternship ? item.company || item.name : item.name
    const displayRole = isInternship ? item.position : item.role
    const displayDate = item.startDate
      ? `${item.startDate} - ${item.endDate || '至今'}`
      : item.date || ''

    return (
      <div
        key={item.id}
        className={`bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-6 transition-all duration-300 mb-4 last:mb-0 hover:-translate-y-1 hover:border-[rgba(102,119,255,0.2)] hover:shadow-[0_8px_32px_rgba(102,119,255,0.1)] max-[640px]:p-4
          ${isCompetition ? 'border-l-4 border-l-[#ff6b35]' : ''}
          ${isInternship ? 'border-l-4 border-l-[#059669]' : ''}
        `}
      >
        <div className="flex items-center gap-3 mb-4 max-[640px]:flex-wrap">
          <div
            className={`w-11 h-11 rounded-[10px] flex items-center justify-center shrink-0 [&>svg]:w-5 [&>svg]:h-5 [&>svg]:text-white ${getIconClass()}`}
          >
            {getIcon()}
          </div>
          <div className="flex flex-col gap-1 flex-1 min-w-0">
            <div className="text-base font-semibold text-white">{displayName}</div>
            {displayRole && <div className="text-sm text-[rgba(140,140,141,1)]">{displayRole}</div>}
          </div>
          {isCompetition && item.award && (
            <div
              className={`px-3.5 py-1.5 rounded-[20px] text-xs font-semibold whitespace-nowrap shrink-0 ${getAwardBadgeClass(item.award)}`}
            >
              {item.award}
            </div>
          )}
          {isInternship && item.status && (
            <div
              className={`px-3.5 py-1.5 rounded-[20px] text-xs font-semibold whitespace-nowrap shrink-0 max-[640px]:ml-auto ${getInternshipBadgeClass(item.status)}`}
            >
              {getInternshipStatusText(item.status)}
            </div>
          )}
          <div className="text-sm text-[rgba(140,140,141,1)] whitespace-nowrap shrink-0 max-[640px]:w-full max-[640px]:mt-1">
            {displayDate}
          </div>
        </div>

        {isCompetition && (
          <div className="flex flex-wrap gap-4 mb-4 max-[640px]:flex-col max-[640px]:gap-2">
            {item.date && (
              <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
                <FileTextOutlined />
                <span>{item.date}</span>
              </div>
            )}
            {item.teamSize && (
              <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
                <TeamOutlined />
                <span>{item.teamSize}人团队</span>
              </div>
            )}
          </div>
        )}

        {isInternship && item.achievements && item.achievements.length > 0 && (
          <div className="flex items-center gap-2 py-2.5 px-3.5 bg-[rgba(255,193,7,0.1)] border border-[rgba(255,193,7,0.2)] rounded-lg mb-4">
            <StarOutlined className="w-4 h-4 text-[#ffc107]" />
            <span className="text-[13px] text-[#ffc107] font-medium">
              {item.achievements.join('；')}
            </span>
          </div>
        )}

        {item.description && (
          <p className="text-sm text-white/70 leading-relaxed m-0 mb-4">{item.description}</p>
        )}

        {type === 'project' && item.techStack && item.techStack.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-4">
            {item.techStack.map((tech) => (
              <span
                key={tech}
                className="px-2.5 py-1 rounded-md text-xs bg-[rgba(102,119,255,0.1)] text-[#6677ff] border border-[rgba(102,119,255,0.2)]"
              >
                {tech}
              </span>
            ))}
          </div>
        )}

        <div className="flex items-center justify-between pt-4 border-t border-white/[0.05] max-[640px]:flex-col max-[640px]:gap-3 max-[640px]:items-start">
          <div className="flex gap-4">
            {type === 'project' && item.demoUrl && (
              <a
                href={item.demoUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1.5 text-[13px] text-[#6677ff] no-underline transition-colors duration-300 hover:text-[#8895ff]"
              >
                <LinkOutlined />
                项目演示
              </a>
            )}
            {isCompetition && item.certificateUrl && (
              <a
                href={item.certificateUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1.5 text-[13px] text-[#6677ff] no-underline transition-colors duration-300 hover:text-[#8895ff]"
              >
                <FileTextOutlined />
                获奖证书
              </a>
            )}
          </div>
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
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-6 max-[640px]:p-4">
      <div className="flex items-center justify-between mb-5 pb-4 border-b border-white/[0.05] max-[640px]:flex-col max-[640px]:gap-3 max-[640px]:items-start">
        <h3 className="text-lg font-semibold text-white m-0">{title}</h3>
        <Button
          type="primary"
          className="!px-6 !py-3 !rounded-[10px] !text-sm !font-medium cursor-pointer transition-all duration-300 !border-none !bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)] hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(102,119,255,0.4)] flex items-center gap-2"
          icon={<PlusOutlined />}
          onClick={() => handleOpenModal()}
        >
          添加{type === 'project' ? '项目' : type === 'competition' ? '竞赛' : '实习'}
        </Button>
      </div>

      {data.length > 0 ? (
        <div className="flex flex-col">{data.map((item) => renderItem(item))}</div>
      ) : (
        <div className="text-center py-[60px] px-5 bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl">
          <div className="w-20 h-20 mx-auto mb-5 rounded-full bg-[rgba(102,119,255,0.1)] flex items-center justify-center [&>svg]:w-10 [&>svg]:h-10 [&>svg]:text-[#6677ff]">
            {getIcon()}
          </div>
          <h3 className="text-lg font-semibold text-white mb-2 m-0">
            暂无{type === 'project' ? '项目' : type === 'competition' ? '竞赛' : '实习'}经历
          </h3>
          <p className="text-sm text-[rgba(140,140,141,1)] m-0">点击上方按钮添加你的经历</p>
        </div>
      )}

      <Modal
        title={editingItem ? '编辑' : '添加'}
        open={isModalOpen}
        onCancel={handleCloseModal}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit} style={{ marginTop: 24 }}>
          {renderFormItems()}
          <Form.Item style={{ marginBottom: 0, marginTop: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
              <Button onClick={handleCloseModal}>取消</Button>
              <Button type="primary" htmlType="submit" loading={isSubmitting}>
                {editingItem ? '保存' : '添加'}
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
