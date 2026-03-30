import { Direction, Role, Gender } from '@/apis/schema/enumerate'
import styles from './MemberCard.module.css'
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
    className: styles.directionCV,
  },
  STRUCTURAL_DESIGN: {
    label: '结构设计',
    icon: 'SD',
    iconImg: structIcon,
    className: styles.directionStruct,
  },
  EMBEDDED: {
    label: '嵌入式开发',
    icon: 'EM',
    iconImg: embedIcon,
    className: styles.directionEmbedded,
  },
}

export const ROLE_CONFIG: Record<Role, RoleConfig> = {
  SUPER_ADMIN: { label: '超级管理员', className: styles.roleAdmin },
  DIRECTION_ADMIN: { label: '方向管理员', className: styles.roleLeader },
  MEMBER: { label: '成员', className: styles.roleMember },
  CANDIDATE: { label: '考生', className: styles.roleMember },
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
