'use client'

import React from 'react'
import { UserExperience } from '@/apis/schema/type'
import { ExperienceCard } from './ExperienceCard'
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
    <div className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-8 animate-[fadeIn_0.3s_ease] max-[480px]:p-4">
      <div className="text-lg font-semibold text-white mb-6 flex items-center gap-2.5 [&_svg]:w-5 [&_svg]:h-5 [&_svg]:text-[#6677ff]">
        {getIcon()}
        <h2>{title}</h2>
      </div>

      {experiences.length > 0 ? (
        <div className="flex flex-col gap-4">
          {experiences.map((experience) => (
            <ExperienceCard key={experience.id} experience={experience} />
          ))}
        </div>
      ) : (
        <div className="text-center py-12 px-6">
          <div className="w-16 h-16 rounded-2xl bg-white/[0.02] flex items-center justify-center mx-auto mb-4 text-[#8c8c8d] [&_svg]:w-8 [&_svg]:h-8">
            {getIcon()}
          </div>
          <h3 className="text-base font-semibold text-white mb-2">{getEmptyText()}</h3>
        </div>
      )}
    </div>
  )
}
