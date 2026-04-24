import { ThemeConfig } from 'antd'
import { Direction, Gender } from '@/apis/schema/type'
import cvIcon from '@/assets/icon/direction/cv_icon.png'
import structIcon from '@/assets/icon/direction/struct_icon.png'
import embedIcon from '@/assets/icon/direction/embed_icon.png'

export const DIRECTIONS = [
  {
    key: 'COMPUTER_VISION' as Direction,
    name: '计算机视觉',
    desc: 'AI图像识别',
    icon: cvIcon,
    theme: 'computerVision',
  },
  {
    key: 'STRUCTURAL_DESIGN' as Direction,
    name: '结构设计',
    desc: '机械结构设计',
    icon: structIcon,
    theme: 'structuralDesign',
  },
  {
    key: 'EMBEDDED' as Direction,
    name: '嵌入式开发',
    desc: '硬件软件开发',
    icon: embedIcon,
    theme: 'embedded',
  },
]

export const GENDER_OPTIONS = [
  { value: 'MALE' as Gender, label: '男' },
  { value: 'FEMALE' as Gender, label: '女' },
]

export const customTheme: ThemeConfig = {
  token: {
    colorError: '#FF6B35',
    colorErrorBorder: '#FF6B35',
    colorErrorOutline: 'rgba(255, 107, 53, 0.3)',
    colorPrimary: '#6677FF',
    colorPrimaryHover: '#7a89ff',
    colorPrimaryActive: '#5a6ce0',
    colorBorder: 'rgba(255, 255, 255, 0.1)',
    colorBorderSecondary: 'rgba(255, 255, 255, 0.05)',
    colorBgContainer: 'rgba(255, 255, 255, 0.05)',
    colorBgElevated: '#1a1a2e',
    colorText: '#ffffff',
    colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
    colorTextDisabled: 'rgba(255, 255, 255, 0.3)',
    borderRadius: 10,
  },
  components: {
    Input: {
      colorBgContainer: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(255, 255, 255, 0.1)',
      colorInfoBorderHover: '#6677FF',
      colorError: '#FF6B35',
      colorErrorBorder: '#FF6B35',
      colorText: '#ffffff',
      colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
      activeShadow: '0 0 0 2px rgba(102, 119, 255, 0.2)',
    },
    Select: {
      colorBgContainer: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(255, 255, 255, 0.1)',
      colorInfoBorderHover: '#6677FF',
      colorError: '#FF6B35',
      colorErrorBorder: '#FF6B35',
      colorText: '#ffffff',
      colorTextPlaceholder: 'rgba(255, 255, 255, 0.4)',
      colorBgElevated: '#1a1a2e',
      optionSelectedBg: 'rgba(102, 119, 255, 0.2)',
      optionActiveBg: 'rgba(102, 119, 255, 0.1)',
    },
    Button: {
      colorPrimary: '#6677FF',
      colorPrimaryHover: '#7a89ff',
      colorPrimaryActive: '#5a6ce0',
      primaryShadow: '0 0 20px rgba(102, 119, 255, 0.4)',
      defaultBg: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(255, 255, 255, 0.1)',
      defaultColor: '#ffffff',
    },
    Upload: {
      colorBgContainer: 'rgba(255, 255, 255, 0.05)',
      colorBorder: 'rgba(102, 119, 255, 0.4)',
      colorInfoBorderHover: '#6677FF',
    },
  },
}
