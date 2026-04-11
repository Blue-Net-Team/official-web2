'use client'

import { useState } from 'react'
import { Avatar, Button, Descriptions, Divider, Drawer, Spin, Tag } from 'antd'
import { UserOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons'
import type { EnrollmentDetailDTO } from '@/apis/schema/type'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'
import type { Direction } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'

interface EnrollmentDrawerProps {
  open: boolean
  detail: EnrollmentDetailDTO | null
  loading: boolean
  onClose: () => void
  onApprove: (id: number) => Promise<void>
  onReject: (id: number) => void
}

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'processing', label: '待审核' },
  APPROVED: { color: 'success', label: '已通过' },
  REJECTED: { color: 'error', label: '已拒绝' },
}

const GRADE_LABELS = ['大一', '大二', '大三', '大四', '大五', '大六']

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

  const handleApprove = async () => {
    if (!detail) return
    setApproving(true)
    try {
      await onApprove(detail.id)
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
              <Descriptions.Item label="年级">
                {GRADE_LABELS[detail.grade - 1] ?? `${detail.grade}`}
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
