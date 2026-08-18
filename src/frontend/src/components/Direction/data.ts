import { DirectionData, DirectionSlug } from './types'

import cvJobCore from '@/assets/direction/cv/job-core.png'
import cvJobTransfer from '@/assets/direction/cv/job-transfer.png'
import cvCompanyAi from '@/assets/direction/cv/company-ai.png'
import cvCompanyInternet from '@/assets/direction/cv/company-internet.png'

import embedJobCore from '@/assets/direction/embed/job-core.png'
import embedJobTransfer from '@/assets/direction/embed/job-transfer.png'
import embedCompanyHardware from '@/assets/direction/embed/company-hardware.png'
import embedCompanyAuto from '@/assets/direction/embed/company-auto.png'

import structJobCore from '@/assets/direction/struct/job-core.png'
import structJobTransfer from '@/assets/direction/struct/job-transfer.png'
import structCompanyMfg from '@/assets/direction/struct/company-mfg.png'
import structCompanyAero from '@/assets/direction/struct/company-aero.png'

const cvData: DirectionData = {
  slug: 'cv',
  title: '计算机视觉方向',
  subtitle:
    '让机器看懂世界，用深度学习赋予设备视觉感知能力。探索图像处理、目标检测、语义分割等前沿技术，掌握OpenCV、PyTorch、YOLO等核心工具，为自动驾驶、智能安防、医疗影像等领域注入AI力量。',
  theme: {
    primary: '#8B5CF6',
    secondary: '#A78BFA',
    gradientStart: '#8B5CF6',
    gradientEnd: '#4C1D95',
  },
  techStack: [
    { name: 'Python', description: '算法开发语言' },
    { name: 'OpenCV', description: '计算机视觉库' },
    { name: 'PyTorch', description: '深度学习框架' },
    { name: 'YOLO', description: '目标检测算法' },
  ],
  careers: [
    {
      title: '核心岗位',
      details: ['算法工程师', '视觉工程师', 'AI应用工程师'],
      image: cvJobCore.src,
    },
    {
      title: '可转岗方向',
      details: ['软件开发', '前端开发', '后端开发'],
      image: cvJobTransfer.src,
    },
    {
      title: 'AI公司',
      details: ['深度求索', '字节跳动', '智谱', 'kimi'],
      image: cvCompanyAi.src,
    },
    {
      title: '互联网大厂',
      details: ['阿里巴巴集团', '腾讯', '字节跳动', '美团', '京东'],
      image: cvCompanyInternet.src,
    },
  ],
  recruitment: {
    requirements: [
      '对人工智能和视觉算法有浓厚兴趣',
      '具备Python编程基础',
      '了解基本的数学和统计知识',
      '有持续学习的热情',
    ],
  },
}

const embedData: DirectionData = {
  slug: 'embed',
  title: '嵌入式开发方向',
  subtitle:
    '从芯片到系统，用代码驱动硬件，让智能设备拥有灵魂。深入学习STM32、Arduino等平台开发，掌握传感器接口、通信协议与实时操作系统，成为连接物理世界与数字世界的关键桥梁。',
  theme: {
    primary: '#10B981',
    secondary: '#34D399',
    gradientStart: '#10B981',
    gradientEnd: '#065F46',
  },
  techStack: [
    { name: 'STM32', description: 'ARM Cortex-M开发' },
    { name: 'PCB/原理图设计', description: '硬件电路设计与仿真' },
    { name: '信号分析', description: '电路信号调试与优化' },
    { name: 'FreeRTOS', description: '实时操作系统' },
  ],
  careers: [
    {
      title: '核心岗位',
      details: [
        '嵌入式软件工程师',
        '固件开发工程师',
        '硬件工程师',
        'IoT开发工程师',
        '驱动开发工程师',
      ],
      image: embedJobCore.src,
    },
    {
      title: '可转岗方向',
      details: ['Linux开发', 'ROS机器人工程师', '自动化控制工程师', '电气工程师'],
      image: embedJobTransfer.src,
    },
    {
      title: '物联网企业',
      details: ['华为技术', '大疆创新', '小米科技', '海康威视', '优信电子'],
      image: embedCompanyHardware.src,
    },
    {
      title: '新能源/汽车企业',
      details: ['比亚迪', '蔚来', '小鹏', '理想', '小米'],
      image: embedCompanyAuto.src,
    },
  ],
  recruitment: {
    requirements: ['对嵌入式开发有浓厚兴趣', '具备C语言基础', '了解基本电路知识', '有动手实践能力'],
  },
}

const structData: DirectionData = {
  slug: 'struct',
  title: '结构设计方向',
  subtitle:
    '从概念到实体，用CAD与3D建模技术打造精密机械结构。在这里，你将学习SolidWorks、AutoCAD等专业工具，掌握从零件设计到装配仿真的完整流程，为智能制造和航空航天领域培养核心人才。',
  theme: {
    primary: '#3B82F6',
    secondary: '#1E3D9A',
    gradientStart: '#1E3D9A',
    gradientEnd: '#0F1F4D',
  },
  techStack: [
    { name: 'SolidWorks', description: '三维机械设计' },
    { name: 'AutoCAD', description: '工程制图' },
    { name: 'ANSYS / Altair Inspire', description: '有限元分析与轻量化设计' },
    { name: '3D打印', description: '快速原型制作' },
  ],
  careers: [
    {
      title: '核心岗位',
      details: ['机械工程师', '结构设计师', 'CAE仿真工程师', '产品结构工程师', '工装夹具工程师'],
      image: structJobCore.src,
    },
    {
      title: '可转岗方向',
      details: ['装配工程师', '工业设计师', '产品经理', '机床操作工程师'],
      image: structJobTransfer.src,
    },
    {
      title: '智能制造企业',
      details: ['大疆创新', '比亚迪', '美的集团', '格力电器', '华为技术'],
      image: structCompanyMfg.src,
    },
    {
      title: '汽车/航空航天企业',
      details: ['中国航天工业集团', '比亚迪', '特斯拉', '吉利汽车', '长安汽车'],
      image: structCompanyAero.src,
    },
  ],
  recruitment: {
    requirements: [
      '对机械设计有浓厚兴趣',
      '具备基本的空间想象能力',
      '愿意学习CAD/CAE软件',
      '有团队合作精神',
    ],
  },
}

export const directionDataMap: Record<DirectionSlug, DirectionData> = {
  cv: cvData,
  embed: embedData,
  struct: structData,
}

export const allDirections: DirectionData[] = [cvData, embedData, structData]

export function getDirectionBySlug(slug: string): DirectionData | null {
  if (slug in directionDataMap) {
    return directionDataMap[slug as DirectionSlug]
  }
  return null
}

export function getValidSlugs(): DirectionSlug[] {
  return ['cv', 'embed', 'struct']
}
