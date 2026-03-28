'use client'

import React from 'react'
import { MemberDetailDTO } from '@/apis/schema/type'
import { DIRECTION_LABELS, GENDER_LABELS } from '@/apis/schema/enumerate'
import styles from './MemberProfile.module.css'

interface ProfilePanelProps {
  member: MemberDetailDTO
}

// 年级映射
const GRADE_LABELS: Record<number, string> = {
  1: '大一',
  2: '大二',
  3: '大三',
  4: '大四',
  5: '研一',
  6: '研二',
}

// 计算年级
const calculateGrade = (enrollmentYear: number): string => {
  const currentYear = new Date().getFullYear()
  const grade = currentYear - enrollmentYear + 1
  return GRADE_LABELS[grade] || `${enrollmentYear}级`
}

// 角色标签映射
const ROLE_LABELS: Record<string, string> = {
  CANDIDATE: '考生',
  MEMBER: '成员',
  SUPER_ADMIN: '超级管理员',
  DIRECTION_ADMIN: '方向管理员',
}

export const ProfilePanel: React.FC<ProfilePanelProps> = ({ member }) => {
  const gradeLabel = calculateGrade(member.enrollmentYear)
  const directionLabel = DIRECTION_LABELS[member.direction] || member.direction
  const genderLabel = GENDER_LABELS[member.gender] || '未知'
  const roleLabel = ROLE_LABELS[member.role] || member.role

  return (
    <div className={styles.profilePanel}>
      <div className={styles.panelHeader}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
        <h2>基本信息</h2>
      </div>

      <div className={styles.infoGrid}>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>用户名</span>
          <span className={styles.infoGridValue}>{member.username}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>昵称</span>
          <span className={styles.infoGridValue}>{member.nickname || member.username}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>年级</span>
          <span className={styles.infoGridValue}>{gradeLabel}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>学院</span>
          <span className={styles.infoGridValue}>{member.college || '未设置'}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>专业</span>
          <span className={styles.infoGridValue}>{member.major || '未设置'}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>报名方向</span>
          <span className={styles.infoGridValue}>{directionLabel}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>性别</span>
          <span className={styles.infoGridValue}>{genderLabel}</span>
        </div>
        <div className={styles.infoGridItem}>
          <span className={styles.infoGridLabel}>角色</span>
          <span className={styles.infoGridValue}>{roleLabel}</span>
        </div>
        {member.bio && (
          <div className={`${styles.infoGridItem} ${styles.infoGridFullwidth}`}>
            <span className={styles.infoGridLabel}>个人简介</span>
            <span className={styles.infoGridValue}>{member.bio}</span>
          </div>
        )}
      </div>
    </div>
  )
}
