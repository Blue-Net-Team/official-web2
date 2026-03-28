'use client'

import { useState } from 'react'
import type { Experience, ExperienceType, InternshipStatus } from '@/types/profile'
import styles from './styles.module.css'
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
      return styles.awardBadgeFirst
    case '二等奖':
    case 'second':
      return styles.awardBadgeSecond
    case '三等奖':
    case 'third':
    case '铜牌':
      return styles.awardBadgeThird
    default:
      return ''
  }
}

function getInternshipBadgeClass(status: InternshipStatus): string {
  return status === 'active' ? styles.internshipBadge : styles.internshipBadgeEnded
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
        return styles.experienceIconProject
      case 'competition':
        return styles.experienceIconCompetition
      case 'internship':
        return styles.experienceIconInternship
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
        className={`${styles.experienceCard} ${isCompetition ? styles.competitionCard : ''} ${isInternship ? styles.internshipCard : ''}`}
      >
        <div className={styles.experienceHeader}>
          <div className={`${styles.experienceIcon} ${getIconClass()}`}>{getIcon()}</div>
          <div className={styles.experienceInfo}>
            <div className={styles.experienceName}>{displayName}</div>
            {displayRole && <div className={styles.experienceRole}>{displayRole}</div>}
          </div>
          {isCompetition && item.award && (
            <div className={`${styles.awardBadge} ${getAwardBadgeClass(item.award)}`}>
              {item.award}
            </div>
          )}
          {isInternship && item.status && (
            <div className={`${styles.internshipBadge} ${getInternshipBadgeClass(item.status)}`}>
              {getInternshipStatusText(item.status)}
            </div>
          )}
          <div className={styles.experienceDate}>{displayDate}</div>
        </div>

        {isCompetition && (
          <div className={styles.competitionMeta}>
            {item.date && (
              <div className={styles.metaItem}>
                <FileTextOutlined />
                <span>{item.date}</span>
              </div>
            )}
            {item.teamSize && (
              <div className={styles.metaItem}>
                <TeamOutlined />
                <span>{item.teamSize}人团队</span>
              </div>
            )}
          </div>
        )}

        {isInternship && item.achievements && item.achievements.length > 0 && (
          <div className={styles.internshipAchievement}>
            <StarOutlined />
            <span>{item.achievements.join('；')}</span>
          </div>
        )}

        {item.description && <p className={styles.experienceDesc}>{item.description}</p>}

        {type === 'project' && item.techStack && item.techStack.length > 0 && (
          <div className={styles.experienceTech}>
            {item.techStack.map((tech) => (
              <span key={tech} className={styles.techTag}>
                {tech}
              </span>
            ))}
          </div>
        )}

        <div className={styles.experienceFooter}>
          <div className={styles.experienceLinks}>
            {type === 'project' && item.demoUrl && (
              <a
                href={item.demoUrl}
                target="_blank"
                rel="noopener noreferrer"
                className={styles.experienceLink}
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
                className={styles.experienceLink}
              >
                <FileTextOutlined />
                获奖证书
              </a>
            )}
          </div>
          <div className={styles.experienceActions}>
            <button
              className={`${styles.actionBtn} ${styles.actionBtnEdit}`}
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
              <button className={`${styles.actionBtn} ${styles.actionBtnDelete}`}>
                <DeleteOutlined />
              </button>
            </Popconfirm>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.experienceSection}>
      <div className={styles.sectionHeader}>
        <h3 className={styles.sectionTitle}>{title}</h3>
        <Button
          type="primary"
          className={`${styles.btn} ${styles.btnPrimary} ${styles.btnSm}`}
          icon={<PlusOutlined />}
          onClick={() => handleOpenModal()}
        >
          添加{type === 'project' ? '项目' : type === 'competition' ? '竞赛' : '实习'}
        </Button>
      </div>

      {data.length > 0 ? (
        <div className={styles.experienceList}>{data.map((item) => renderItem(item))}</div>
      ) : (
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>{getIcon()}</div>
          <h3 className={styles.emptyTitle}>
            暂无{type === 'project' ? '项目' : type === 'competition' ? '竞赛' : '实习'}经历
          </h3>
          <p className={styles.emptyDesc}>点击上方按钮添加你的经历</p>
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
