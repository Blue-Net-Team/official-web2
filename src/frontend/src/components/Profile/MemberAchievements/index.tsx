'use client'

import { useCallback, useEffect, useState } from 'react'
import { Avatar, Flex, Spin, Tag } from 'antd'
import { TrophyOutlined, UserOutlined } from '@ant-design/icons'
import Link from 'next/link'
import Image from 'next/image'
import type { AchievementDTO } from '@/apis/schema/type'
import { ACHIEVEMENT_TYPE_LABELS, AWARD_LEVEL_LABELS } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'
import { memberService } from '@/apis/services/member.service'

interface MemberAchievementsProps {
  /** 成员用户ID */
  memberId: number
}

const AWARD_LEVEL_COLOR: Record<string, string> = {
  NATIONAL: 'gold',
  PROVINCIAL: 'cyan',
  SCHOOL: 'orange',
}

const TYPE_TAG_COLOR: Record<string, string> = {
  PAPER: 'purple',
  PATENT: 'magenta',
  COMPETITION: 'blue',
}

/**
 * 个人成就只读列表
 *
 * 用于成员公开主页和个人中心，展示由管理员维护的成员关联成就。
 */
export default function MemberAchievements({ memberId }: MemberAchievementsProps) {
  const [achievements, setAchievements] = useState<AchievementDTO[]>([])
  const [loading, setLoading] = useState(true)

  const fetchAchievements = useCallback(async () => {
    setLoading(true)
    try {
      const res = await memberService.getMemberAchievements(memberId)
      if (res.code === 200 && res.data) {
        setAchievements(res.data)
      } else {
        setAchievements([])
      }
    } catch (error) {
      console.error('Failed to fetch member achievements:', error)
      setAchievements([])
    } finally {
      setLoading(false)
    }
  }, [memberId])

  useEffect(() => {
    fetchAchievements()
  }, [fetchAchievements])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-6 max-[640px]:p-4">
      <div className="flex items-center justify-between mb-5 pb-4 border-b border-white/[0.05]">
        <h3 className="text-lg font-semibold text-white m-0">个人成就</h3>
      </div>

      {achievements.length === 0 ? (
        <div className="text-center py-[60px] px-5 bg-white/[0.03] rounded-2xl">
          <div className="w-20 h-20 mx-auto mb-5 rounded-full bg-[rgba(102,119,255,0.1)] flex items-center justify-center [&>svg]:w-10 [&>svg]:h-10 [&>svg]:text-[#6677ff]">
            <TrophyOutlined />
          </div>
          <h3 className="text-lg font-semibold text-white mb-2 m-0">暂无个人成就</h3>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {achievements.map((achievement) => {
            const members = achievement.members ?? []
            const externalMembers = achievement.externalMembers ?? []
            return (
              <div
                key={achievement.id}
                className="bg-white/[0.02] border border-white/[0.05] rounded-xl p-5 transition-all duration-300 hover:bg-white/[0.04] hover:border-[#6677ff]/20"
              >
                <Flex align="start" gap={16} className="max-md:flex-wrap">
                  {achievement.fileUrl && (
                    <div className="relative w-24 h-18 rounded-lg overflow-hidden shrink-0 aspect-[4/3]">
                      <Image
                        src={achievement.fileUrl}
                        alt={achievement.title}
                        fill
                        className="object-cover"
                        unoptimized
                      />
                    </div>
                  )}
                  <div className="flex-1 min-w-0">
                    <Flex align="center" gap={8} wrap="wrap">
                      <span className="text-base font-semibold text-white">
                        {achievement.title}
                      </span>
                      <Tag color={TYPE_TAG_COLOR[achievement.type]} className="border-none">
                        {ACHIEVEMENT_TYPE_LABELS[achievement.type]}
                      </Tag>
                      {achievement.awardLevel && (
                        <Tag
                          color={AWARD_LEVEL_COLOR[achievement.awardLevel]}
                          className="border-none"
                        >
                          {AWARD_LEVEL_LABELS[achievement.awardLevel]}
                        </Tag>
                      )}
                    </Flex>
                    {achievement.relateTo && (
                      <div className="text-sm text-white/65 mt-1">{achievement.relateTo}</div>
                    )}
                    {achievement.awardName && (
                      <div className="text-sm text-white/70 mt-1">{achievement.awardName}</div>
                    )}
                    <div className="text-xs text-white/50 mt-2">{achievement.achieveAt}</div>
                    {(members.length > 0 || externalMembers.length > 0) && (
                      <Flex align="center" gap={8} wrap="wrap" className="mt-3">
                        {members.map((member) => (
                          <Link
                            key={member.userId}
                            href={`/members/${member.userId}`}
                            className="hover:opacity-80"
                          >
                            <Flex align="center" gap={4}>
                              <Avatar
                                size={20}
                                icon={<UserOutlined />}
                                src={
                                  member.avatarFileId
                                    ? `${API_BASE_URL}/file/download/${member.avatarFileId}`
                                    : undefined
                                }
                              />
                              <span className="text-xs text-white/70">{member.username}</span>
                            </Flex>
                          </Link>
                        ))}
                        {externalMembers.map((name) => (
                          <Flex key={name} align="center" gap={4}>
                            <Avatar size={20} icon={<UserOutlined />} />
                            <span className="text-xs text-white/60">{name}</span>
                          </Flex>
                        ))}
                      </Flex>
                    )}
                  </div>
                </Flex>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
