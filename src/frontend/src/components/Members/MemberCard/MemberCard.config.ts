import { Direction, Role, Gender } from '@/apis/schema/enumerate'
import type { StaticImageData } from 'next/image'
import sexManIcon from '@/assets/icon/gender/sex_man.svg'
import sexWomanIcon from '@/assets/icon/gender/sex_woman.svg'
import cvIcon from '@/assets/icon/direction/cv_icon.png'
import structIcon from '@/assets/icon/direction/struct_icon.png'
import embedIcon from '@/assets/icon/direction/embed_icon.png'

interface DirectionConfig {
  label: string
  icon: string
  iconImg: string | StaticImageData
  className: string
}

interface RoleConfig {
  label: string
  className: string
}

interface GenderConfig {
  label: string
  icon: string | null
}

export const DIRECTION_CONFIG: Record<Direction, DirectionConfig> = {
  COMPUTER_VISION: {
    label: '计算机视觉',
    icon: 'CV',
    iconImg: cvIcon,
    className:
      'bg-gradient-to-br from-[#6677ff]/15 to-[#2f27b0]/15 border border-[#6677ff]/30 text-[#6677ff]',
  },
  STRUCTURAL_DESIGN: {
    label: '结构设计',
    icon: 'SD',
    iconImg: structIcon,
    className:
      'bg-gradient-to-br from-[#ff6b35]/15 to-[#ff8c42]/15 border border-[#ff6b35]/30 text-[#ff6b35]',
  },
  EMBEDDED: {
    label: '嵌入式开发',
    icon: 'EM',
    iconImg: embedIcon,
    className:
      'bg-gradient-to-br from-[#2ecc71]/15 to-[#27ae60]/15 border border-[#2ecc71]/30 text-[#2ecc71]',
  },
}

export const ROLE_CONFIG: Record<Role, RoleConfig> = {
  SUPER_ADMIN: {
    label: '超级管理员',
    className:
      'bg-gradient-to-br from-[#ff6b35]/20 to-[#ff8c42]/20 border border-[#ff6b35]/40 text-[#ff6b35]',
  },
  DIRECTION_ADMIN: {
    label: '方向管理员',
    className:
      'bg-gradient-to-br from-[#6677ff]/20 to-[#2f27b0]/20 border border-[#6677ff]/40 text-[#6677ff]',
  },
  MEMBER: {
    label: '成员',
    className: 'bg-white/[0.08] border border-white/[0.15] text-white/60',
  },
  CANDIDATE: {
    label: '考生',
    className: 'bg-white/[0.08] border border-white/[0.15] text-white/60',
  },
}

export const GENDER_CONFIG: Record<Gender, GenderConfig> = {
  MALE: {
    label: '男',
    icon: sexManIcon,
  },
  FEMALE: {
    label: '女',
    icon: sexWomanIcon,
  },
  UNKNOWN: {
    label: '未知',
    icon: null,
  },
}
