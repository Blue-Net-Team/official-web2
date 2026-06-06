'use client'

import { useState } from 'react'
import {
  TeamOutlined,
  CrownOutlined,
  CopyOutlined,
  LogoutOutlined,
  UserSwitchOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons'
import { Alert, Button, Collapse, Tag, Modal, Select, message, Tooltip } from 'antd'
import type { AssessmentTeamDTO, AssessmentTeamMemberDTO } from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'

interface TeamPanelProps {
  team: AssessmentTeamDTO
  currentUserId: number
  onLeaveTeam: () => void
  onTransferLeader: (newLeaderId: number) => void
  onDisbandTeam: () => void
  loading?: boolean
}

export default function TeamPanel({
  team,
  currentUserId,
  onLeaveTeam,
  onTransferLeader,
  onDisbandTeam,
  loading,
}: TeamPanelProps) {
  const isLeader = team.leaderId === currentUserId
  const [leaveModalOpen, setLeaveModalOpen] = useState(false)
  const [transferModalOpen, setTransferModalOpen] = useState(false)
  const [disbandModalOpen, setDisbandModalOpen] = useState(false)
  const [selectedNewLeader, setSelectedNewLeader] = useState<number | null>(null)
  const [copied, setCopied] = useState(false)

  const handleCopyInviteCode = async () => {
    try {
      await navigator.clipboard.writeText(team.inviteCode)
      setCopied(true)
      message.success('邀请码已复制')
      setTimeout(() => setCopied(false), 2000)
    } catch {
      message.error('复制失败')
    }
  }

  const otherMembers = team.members.filter((m) => m.userId !== team.leaderId)
  const leaderMember = team.members.find((m) => m.leader)

  const renderMember = (member: AssessmentTeamMemberDTO) => (
    <div
      key={member.userId}
      className="flex items-center gap-2.5 py-2 px-3 rounded-lg bg-white/[0.04]"
    >
      <div className="w-7 h-7 rounded-full bg-[#6677ff]/[0.15] flex items-center justify-center text-[11px] text-[#6677ff] font-medium shrink-0">
        {member.username.charAt(0)}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-1.5">
          <span className="text-[13px] text-white truncate">{member.username}</span>
          {member.leader && (
            <Tag color="gold" className="!text-[10px] !px-1 !py-0 !leading-4">
              <CrownOutlined className="text-[10px] mr-0.5" />
              队长
            </Tag>
          )}
        </div>
        {member.direction && (
          <span className="text-[11px] text-white/35">
            {DIRECTION_LABELS[member.direction] || member.direction}
          </span>
        )}
      </div>
    </div>
  )

  return (
    <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-5 flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <TeamOutlined className="text-base text-[#6677ff]" />
          <span className="text-sm font-semibold text-white">我的队伍</span>
        </div>
        {isLeader && (
          <Tag color="gold" className="!text-[10px] !px-1.5 !py-0 !leading-5">
            <CrownOutlined className="text-[10px] mr-0.5" />
            队长
          </Tag>
        )}
      </div>

      <hr className="w-full h-px bg-white/[0.04] border-none m-0" />

      {/* 队伍名称 */}
      <div className="flex items-center justify-between">
        <span className="text-[13px] text-white/45">队伍名称</span>
        <span className="text-[13px] text-white font-medium">{team.name}</span>
      </div>

      {/* 邀请码（仅队长可见） */}
      {isLeader && (
        <div className="flex items-center justify-between">
          <span className="text-[13px] text-white/45">邀请码</span>
          <div className="flex items-center gap-2">
            <code className="text-[13px] text-[#6677ff] bg-[#6677ff]/[0.08] px-2 py-0.5 rounded font-mono">
              {team.inviteCode}
            </code>
            <Tooltip title={copied ? '已复制' : '复制邀请码'}>
              <button
                className="w-6 h-6 rounded flex items-center justify-center bg-white/[0.06] border-none cursor-pointer transition-colors hover:bg-white/[0.1]"
                onClick={handleCopyInviteCode}
              >
                {copied ? (
                  <CheckCircleOutlined className="text-[11px] text-[#07c160]" />
                ) : (
                  <CopyOutlined className="text-[11px] text-white/45" />
                )}
              </button>
            </Tooltip>
          </div>
        </div>
      )}

      {/* 组队规则 */}
      <Collapse
        ghost
        size="small"
        items={[
          {
            key: 'rules',
            label: <span className="text-[12px] text-white/45">组队规则与风险提示</span>,
            children: (
              <Alert
                type="warning"
                showIcon
                className="!bg-white/[0.04] !border-white/[0.08]"
                description={
                  <ul className="text-[12px] text-white/55 list-disc pl-4 space-y-1">
                    <li>组队仅支持文件上传题，客观题需独立作答</li>
                    <li>队长提交答案后，队伍锁定，所有成员不可退出</li>
                    <li>队长解散队伍时，所有已提交答案将被删除</li>
                    <li>退出队伍后可重新加入原队伍或其他队伍</li>
                    <li>已有答案者不能加入新队伍</li>
                  </ul>
                }
              />
            ),
          },
        ]}
      />

      {/* 成员列表 */}
      <div className="flex flex-col gap-1.5">
        <span className="text-[13px] text-white/45 mb-1">成员 ({team.members.length} 人)</span>
        <div className="flex flex-col gap-1.5 max-h-[200px] overflow-y-auto">
          {leaderMember && renderMember(leaderMember)}
          {otherMembers.map(renderMember)}
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="flex flex-col gap-2 mt-1">
        {isLeader ? (
          <>
            <Button
              size="small"
              icon={<UserSwitchOutlined />}
              onClick={() => {
                setSelectedNewLeader(null)
                setTransferModalOpen(true)
              }}
              className="!text-[13px]"
            >
              转让队长
            </Button>
            <Button
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={() => setDisbandModalOpen(true)}
              className="!text-[13px]"
            >
              解散队伍
            </Button>
          </>
        ) : (
          <Button
            size="small"
            danger
            icon={<LogoutOutlined />}
            onClick={() => setLeaveModalOpen(true)}
            className="!text-[13px]"
          >
            退出队伍
          </Button>
        )}
      </div>

      {/* 退出队伍确认 */}
      <Modal
        title="确认退出队伍"
        open={leaveModalOpen}
        onOk={() => {
          setLeaveModalOpen(false)
          onLeaveTeam()
        }}
        onCancel={() => setLeaveModalOpen(false)}
        confirmLoading={loading}
        okText="确认退出"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确定要退出队伍「{team.name}」吗？</p>
        <p className="text-white/45 text-[13px] mt-2">退出后可重新加入本队或其他队伍。</p>
      </Modal>

      {/* 转让队长 */}
      <Modal
        title="转让队长"
        open={transferModalOpen}
        onOk={() => {
          if (!selectedNewLeader) {
            message.warning('请选择新队长')
            return
          }
          setTransferModalOpen(false)
          onTransferLeader(selectedNewLeader)
          setSelectedNewLeader(null)
        }}
        onCancel={() => {
          setTransferModalOpen(false)
          setSelectedNewLeader(null)
        }}
        confirmLoading={loading}
        okText="确认转让"
        cancelText="取消"
      >
        <p className="text-white/65 mb-4">请选择新的队长：</p>
        <Select
          className="w-full"
          placeholder="选择新队长"
          value={selectedNewLeader}
          onChange={setSelectedNewLeader}
          options={otherMembers.map((m) => ({
            value: m.userId,
            label: `${m.username}${m.nickname ? ` (${m.nickname})` : ''}`,
          }))}
        />
      </Modal>

      {/* 解散队伍确认 */}
      <Modal
        title="确认解散队伍"
        open={disbandModalOpen}
        onOk={() => {
          setDisbandModalOpen(false)
          onDisbandTeam()
        }}
        onCancel={() => setDisbandModalOpen(false)}
        confirmLoading={loading}
        okText="确认解散"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确定要解散队伍「{team.name}」吗？此操作不可撤销。</p>
        <Alert
          type="warning"
          showIcon
          className="mt-3 !bg-white/[0.04] !border-white/[0.08]"
          message="<span className='text-[13px] text-white/70'>队伍解散后，所有已提交答案将被删除。</span>"
        />
        <p className="text-white/45 text-[13px] mt-2">队伍解散后，所有成员将需要重新组队。</p>
      </Modal>
    </div>
  )
}
