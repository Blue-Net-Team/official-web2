'use client'

import { useEffect, useState } from 'react'
import { Avatar, Button, Descriptions, Divider, Drawer, InputNumber, Spin, Tag } from 'antd'
import { UserOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons'
import type { EnrollmentDetailDTO } from '@/apis/schema/type'
import { DIRECTION_LABELS, GENDER_LABELS } from '@/apis/schema/enumerate'
import type { Direction } from '@/apis/schema/enumerate'
import type { Gender } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'

interface EnrollmentDrawerProps {
  open: boolean
  detail: EnrollmentDetailDTO | null
  loading: boolean
  onClose: () => void
  onApprove: (id: number, assessmentGradeYear?: number) => Promise<void>
  onReject: (id: number) => void
}

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'processing', label: '待审核' },
  APPROVED: { color: 'success', label: '已通过' },
  REJECTED: { color: 'error', label: '已拒绝' },
}

export const EnrollmentDrawer: React.FC<EnrollmentDrawerProps> = ({
  open,
  detail,
  loading,
  onClose,
  onApprove,
  onReject,
}) => {
  const [avatarError, setAvatarError] = useState(false)
  const [approving, setApproving] = useState(false)

  const defaultAssessmentGradeYear =
    detail?.studentId && /^\d{4}/.test(detail.studentId)
      ? Number.parseInt(detail.studentId.slice(0, 4), 10)
      : undefined
  const [assessmentGradeYear, setAssessmentGradeYear] = useState<number | undefined>(
    defaultAssessmentGradeYear
  )
  const effectiveAssessmentGradeYear = assessmentGradeYear ?? defaultAssessmentGradeYear

  useEffect(() => {
    setAssessmentGradeYear(defaultAssessmentGradeYear)
  }, [defaultAssessmentGradeYear])

  const handleApprove = async () => {
    if (!detail) return
    setApproving(true)
    try {
      await onApprove(detail.id, effectiveAssessmentGradeYear)
    } finally {
      setApproving(false)
    }
  }

  const handleReject = () => {
    if (!detail) return
    onReject(detail.id)
  }

  const statusCfg = detail
    ? (STATUS_CONFIG[detail.status] ?? { color: 'default', label: detail.status })
    : null
  const directionLabel = detail
    ? (DIRECTION_LABELS[detail.direction as Direction] ?? detail.direction)
    : ''
  const genderLabel = detail?.gender
    ? (GENDER_LABELS[detail.gender as Gender] ?? detail.gender)
    : '未知'
  const avatarSrc = detail?.avatarFileId
    ? `${API_BASE_URL}/file/download/${detail.avatarFileId}`
    : undefined

  return (
    <Drawer
      title="报名详情"
      placement="right"
      size="default"
      open={open}
      onClose={onClose}
      styles={{ body: { padding: 0 } }}
    >
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Spin />
        </div>
      ) : detail ? (
        <div className="flex flex-col">
          {/* Avatar section */}
          <div className="flex flex-col items-center gap-3 py-6 px-6">
            <Avatar
              size={80}
              src={avatarSrc && !avatarError ? avatarSrc : undefined}
              icon={!avatarSrc || avatarError ? <UserOutlined /> : undefined}
              onError={() => {
                setAvatarError(true)
                return true
              }}
            />
            <div className="text-lg font-semibold text-white/85">{detail.username}</div>
            {statusCfg && <Tag color={statusCfg.color}>{statusCfg.label}</Tag>}
          </div>

          <Divider style={{ margin: 0 }} />

          {/* Info fields */}
          <div className="px-6 py-5">
            <Descriptions
              column={1}
              size="small"
              colon={false}
              labelStyle={{ width: 60, color: 'rgba(255,255,255,0.55)' }}
            >
              <Descriptions.Item label="学号">{detail.studentId}</Descriptions.Item>
              <Descriptions.Item label="邮箱">{detail.email}</Descriptions.Item>
              <Descriptions.Item label="学院">{detail.collegeName}</Descriptions.Item>
              <Descriptions.Item label="专业">{detail.major}</Descriptions.Item>
              <Descriptions.Item label="性别">{genderLabel}</Descriptions.Item>
              <Descriptions.Item label="考核届别">
                <InputNumber
                  min={2000}
                  max={2100}
                  precision={0}
                  value={effectiveAssessmentGradeYear}
                  disabled={detail.status !== 'PENDING'}
                  onChange={(value) => setAssessmentGradeYear(value ?? undefined)}
                  style={{ width: 120 }}
                />
              </Descriptions.Item>
              <Descriptions.Item label="方向">
                <span className="text-[#fa8c16]">{directionLabel}</span>
              </Descriptions.Item>
              {detail.referralUserName && (
                <Descriptions.Item label="推荐人">{detail.referralUserName}</Descriptions.Item>
              )}
            </Descriptions>
          </div>

          <Divider style={{ margin: 0 }} />

          {/* Introduction */}
          <div className="px-6 py-5">
            <div className="text-[13px] text-white/55 mb-2">自我介绍</div>
            <div className="text-[13px] text-white/75 leading-relaxed whitespace-pre-wrap break-words">
              {detail.introduction || '无'}
            </div>
          </div>

          {/* Actions */}
          {detail.status === 'PENDING' && (
            <>
              <Divider style={{ margin: 0 }} />
              <div className="flex gap-3 justify-center py-5 px-6">
                <Button
                  type="primary"
                  color="green"
                  variant="outlined"
                  icon={<CheckOutlined />}
                  loading={approving}
                  onClick={handleApprove}
                >
                  通过
                </Button>
                <Button danger icon={<CloseOutlined />} onClick={handleReject}>
                  拒绝
                </Button>
              </div>
            </>
          )}
        </div>
      ) : null}
    </Drawer>
  )
}
