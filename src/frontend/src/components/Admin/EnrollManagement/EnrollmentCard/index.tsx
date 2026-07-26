'use client'

import { useState } from 'react'
import { Avatar, Button, Card, Tag } from 'antd'
import { UserOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons'
import type { EnrollmentBriefDTO } from '@/apis/schema/type'
import { DIRECTION_LABELS, GENDER_LABELS } from '@/apis/schema/enumerate'
import type { Direction } from '@/apis/schema/enumerate'
import type { Gender } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'

export type { EnrollmentBriefDTO }

interface EnrollmentCardProps {
  enrollment: EnrollmentBriefDTO
  onClick: () => void
  onApprove: (id: number) => Promise<void>
  onReject: (id: number) => void
}

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'processing', label: '待审核' },
  APPROVED: { color: 'success', label: '已通过' },
  REJECTED: { color: 'error', label: '已拒绝' },
}

const cardStyle: React.CSSProperties = { cursor: 'pointer', borderColor: 'rgba(255,255,255,0.12)' }

export const EnrollmentCard: React.FC<EnrollmentCardProps> = ({
  enrollment,
  onClick,
  onApprove,
  onReject,
}) => {
  const [avatarError, setAvatarError] = useState(false)
  const [approving, setApproving] = useState(false)

  const statusCfg = STATUS_CONFIG[enrollment.status] ?? {
    color: 'default',
    label: enrollment.status,
  }
  const directionLabel = DIRECTION_LABELS[enrollment.direction as Direction] ?? enrollment.direction
  const genderLabel = enrollment.gender
    ? (GENDER_LABELS[enrollment.gender as Gender] ?? enrollment.gender)
    : '未知'
  const avatarSrc = enrollment.avatarFileId
    ? `${API_BASE_URL}/file/download/${enrollment.avatarFileId}`
    : undefined

  const handleApprove = async (e: React.MouseEvent) => {
    e.stopPropagation()
    setApproving(true)
    try {
      await onApprove(enrollment.id)
    } finally {
      setApproving(false)
    }
  }

  const handleReject = (e: React.MouseEvent) => {
    e.stopPropagation()
    onReject(enrollment.id)
  }

  return (
    <Card style={cardStyle} styles={{ body: { padding: 20 } }} hoverable onClick={onClick}>
      <div className="flex flex-wrap items-center gap-3 mb-3 justify-center">
        <Avatar
          size={44}
          src={avatarSrc && !avatarError ? avatarSrc : undefined}
          icon={!avatarSrc || avatarError ? <UserOutlined /> : undefined}
          onError={() => {
            setAvatarError(true)
            return true
          }}
        />
        <div className="flex-1 min-w-0">
          <div className="text-[15px] font-semibold text-white/85 leading-tight">
            {enrollment.username}
          </div>
          <div className="text-xs text-white/55 mt-0.5">{enrollment.studentId}</div>
        </div>
      </div>

      <div className="text-[13px] text-white/65 mb-3">
        {enrollment.collegeName} · {genderLabel} · {directionLabel}
      </div>

      <div className="flex flex-wrap gap-2">
        <Tag color={statusCfg.color}>{statusCfg.label}</Tag>
        {enrollment.referralUserName && <Tag color="gold">{enrollment.referralUserName} 内推</Tag>}
        {enrollment.status === 'PENDING' && (
          <>
            <Button
              size="small"
              color="green"
              variant="outlined"
              icon={<CheckOutlined />}
              loading={approving}
              onClick={handleApprove}
            >
              通过
            </Button>
            <Button size="small" danger icon={<CloseOutlined />} onClick={handleReject}>
              拒绝
            </Button>
          </>
        )}
      </div>
    </Card>
  )
}
