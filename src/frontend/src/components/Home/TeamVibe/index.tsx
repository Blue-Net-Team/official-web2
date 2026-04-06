'use client'

import styles from './styles.module.css'
import teamVibeImage from '@/assets/team_vibe.jpg'

export default function TeamVibe() {
  return (
    <div className="w-full min-h-screen max-lg:min-h-[95vh] flex flex-col box-border pt-[75px] pb-[45px] pl-[93px] max-md:pt-[60px] max-md:pb-[40px] max-md:pl-[40px] max-[767px]:p-[40px_20px]">
      <h2 className="text-[43px] font-bold text-white mb-[36px]! max-md:text-[36px] max-md:mb-[24px] max-[767px]:text-[28px] max-[767px]:mb-[20px]">
        重新定义团队氛围
      </h2>

      <div
        className={`${styles.contentCard} w-full flex-1 border-t-[3px] border-b-[3px] border-l-[3px] border-r-0 border-[#1e3d9a] rounded-l-[36px] rounded-r-0 overflow-hidden relative max-md:rounded-l-[24px] max-md:h-[400px] max-[767px]:rounded-[20px] max-[767px]:border-r-[3px] max-[767px]:min-h-[400px] max-[767px]:h-auto`}
        style={
          {
            '--team-vibe-bg': `url(${teamVibeImage.src})`,
          } as React.CSSProperties
        }
      >
        <div className="w-1/2 h-full !p-[36px_32px] box-border z-[1] flex flex-col max-md:w-3/5 max-md:!p-[30px_24px] max-[767px]:w-full max-[767px]:h-auto max-[767px]:justify-end max-[767px]:!p-[30px_24px]">
          <h3 className="text-[35px] font-bold text-white mb-[28px]! leading-[1.2] max-md:text-[28px] max-[767px]:text-[24px] max-[767px]:mb-[16px]">
            队内氛围融洽，技术精湛
          </h3>
          <p className="text-[20px] font-normal text-white mb-[18px]! leading-[1.6] last:mb-0 max-md:text-[18px] max-[767px]:text-[16px] max-[767px]:mb-[12px] max-[767px]:leading-[1.5]">
            团队氛围轻松融洽，弹性工作，无竞赛、论文等硬性指标。旨在培养学生学习更多新技术应用到工程实践
          </p>
          <p className="text-[20px] font-normal text-white mb-[18px]! leading-[1.6] last:mb-0 max-md:text-[18px] max-[767px]:text-[16px] max-[767px]:mb-[12px] max-[767px]:leading-[1.5]">
            进入团队后，可以跟学长和老师学习行业前沿技术，共同实现项目落地，丰富简历内容
          </p>
        </div>
      </div>
    </div>
  )
}
