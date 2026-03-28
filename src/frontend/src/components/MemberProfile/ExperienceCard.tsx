'use client'

import React from 'react'
import { UserExperience } from '@/apis/schema/type'
import styles from './MemberProfile.module.css'
import {
  FolderOutlined,
  TrophyOutlined,
  SolutionOutlined,
  LinkOutlined,
  FileTextOutlined,
} from '@ant-design/icons'

interface ExperienceCardProps {
  experience: UserExperience
}

export const ExperienceCard: React.FC<ExperienceCardProps> = ({ experience }) => {
  const isProject = experience.type === 'project'
  const isCompetition = experience.type === 'competition'
  const isInternship = experience.type === 'internship'

  // 获取显示名称
  const displayName = isInternship ? experience.company || experience.name : experience.name

  // 获取角色/职位
  const displayRole = isInternship ? experience.position : experience.role

  // 获取时间显示
  const displayDate = experience.startDate
    ? `${experience.startDate} - ${experience.endDate || '至今'}`
    : experience.date || ''

  // 获取图标
  const getIcon = () => {
    switch (experience.type) {
      case 'project':
        return <FolderOutlined />
      case 'competition':
        return <TrophyOutlined />
      case 'internship':
        return <SolutionOutlined />
    }
  }

  // 获取图标样式类
  const getIconClass = () => {
    switch (experience.type) {
      case 'project':
        return styles.experienceIconProject
      case 'competition':
        return styles.experienceIconCompetition
      case 'internship':
        return styles.experienceIconInternship
    }
  }

  // 获取角色样式类
  const getRoleClass = () => {
    switch (experience.type) {
      case 'competition':
        return styles.experienceRoleCompetition
      case 'internship':
        return styles.experienceRoleInternship
      default:
        return ''
    }
  }

  return (
    <div className={styles.experienceCard}>
      <div className={styles.experienceHeader}>
        <div className={`${styles.experienceIcon} ${getIconClass()}`}>{getIcon()}</div>
        <div className={styles.experienceInfo}>
          <div className={styles.experienceName}>{displayName}</div>
          {displayRole && (
            <div className={`${styles.experienceRole} ${getRoleClass()}`}>
              {displayRole}
              {isCompetition && experience.award && ` · ${experience.award}`}
            </div>
          )}
        </div>
        <div className={styles.experienceDate}>{displayDate}</div>
      </div>

      {/* 竞赛元信息 */}
      {isCompetition && (experience.date || experience.teamSize) && (
        <div
          className={styles.competitionMeta}
          style={{
            display: 'flex',
            gap: '16px',
            marginBottom: '12px',
            fontSize: '13px',
            color: 'rgba(140, 140, 141, 1)',
          }}
        >
          {experience.date && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <FileTextOutlined style={{ fontSize: '14px' }} />
              <span>{experience.date}</span>
            </div>
          )}
          {experience.teamSize && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                style={{ width: '14px', height: '14px' }}
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              <span>{experience.teamSize}人团队</span>
            </div>
          )}
        </div>
      )}

      {/* 描述 */}
      {experience.description && <p className={styles.experienceDesc}>{experience.description}</p>}

      {/* 技术栈（项目） */}
      {isProject && experience.techStack && experience.techStack.length > 0 && (
        <div className={styles.experienceTech}>
          {experience.techStack.map((tech, index) => (
            <span key={index} className={styles.techTag}>
              {tech}
            </span>
          ))}
        </div>
      )}

      {/* 链接 */}
      <div className={styles.experienceFooter}>
        <div className={styles.experienceLinks}>
          {isProject && experience.demoUrl && (
            <a
              href={experience.demoUrl}
              target="_blank"
              rel="noopener noreferrer"
              className={styles.experienceLink}
            >
              <LinkOutlined />
              项目演示
            </a>
          )}
          {isCompetition && experience.certificateUrl && (
            <a
              href={experience.certificateUrl}
              target="_blank"
              rel="noopener noreferrer"
              className={styles.experienceLink}
            >
              <FileTextOutlined />
              获奖证书
            </a>
          )}
        </div>
      </div>
    </div>
  )
}
