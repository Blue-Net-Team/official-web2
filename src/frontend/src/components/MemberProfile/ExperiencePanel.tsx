'use client'

import React from 'react'
import { UserExperience } from '@/apis/schema/type'
import { ExperienceCard } from './ExperienceCard'
import styles from './MemberProfile.module.css'
import { FolderOutlined, TrophyOutlined, SolutionOutlined } from '@ant-design/icons'

interface ExperiencePanelProps {
  type: 'project' | 'competition' | 'internship'
  title: string
  experiences: UserExperience[]
}

export const ExperiencePanel: React.FC<ExperiencePanelProps> = ({ type, title, experiences }) => {
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

  const getEmptyText = () => {
    switch (type) {
      case 'project':
        return '暂无项目经历'
      case 'competition':
        return '暂无竞赛经历'
      case 'internship':
        return '暂无实习经历'
    }
  }

  return (
    <div className={styles.experiencePanel}>
      <div className={styles.panelHeader}>
        {getIcon()}
        <h2>{title}</h2>
      </div>

      {experiences.length > 0 ? (
        <div className={styles.experienceList}>
          {experiences.map((experience) => (
            <ExperienceCard key={experience.id} experience={experience} />
          ))}
        </div>
      ) : (
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>{getIcon()}</div>
          <h3 className={styles.emptyTitle}>{getEmptyText()}</h3>
        </div>
      )}
    </div>
  )
}
