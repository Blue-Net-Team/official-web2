/**
 * 个人主页Mock数据
 *
 * 与后端DTO保持一致
 *
 * @author BlueNet Team
 */
import type {
  UserInfo,
  UserStats,
  TabCounts,
  Assessment,
  Experience,
  Direction,
  Gender,
} from '@/types/profile'

// ==================== 用户基础数据 ====================

/** Mock 用户基本信息 - 对应后端 UserInfo */
export const mockUserInfo: UserInfo = {
  id: 1,
  username: 'zhangsan',
  nickname: 'zhangsan_dev',
  college: '计算机学院',
  major: '软件工程',
  grade: '大二',
  enrollmentYear: 2023,
  email: 'zhangsan@example.com',
  avatarFileId: null,
  roleName: 'candidate',
  direction: 'computer_vision' as Direction,
  gender: 'unknown' as Gender,
  bio: '海纳百川，有容乃大。热爱计算机视觉与深度学习，专注于目标检测与图像分割领域。',
}

/** Mock用户统计数据 */
export const mockUserStats: UserStats = {
  assessmentCount: 3,
  completedCount: 1,
  averageScore: 88,
}

/** Mock Tab计数 - 对应后端 TabCountsDTO */
export const mockTabCounts: TabCounts = {
  projects: 2,
  competitions: 3,
  internships: 2,
}

// ==================== 考核数据 ====================

/** Mock考核列表 */
export const mockAssessments: Assessment[] = [
  {
    id: '1',
    title: '计算机视觉方向考核',
    round: '第二轮考核',
    status: 'in-progress',
    startDate: '2025-03-01',
    endDate: '2025-03-15',
    totalQuestions: 5,
    completedQuestions: 3,
    remainingTime: '5 天 12 小时',
  },
  {
    id: '2',
    title: '计算机视觉方向考核',
    round: '第一轮考核',
    status: 'ended',
    startDate: '2025-02-15',
    endDate: '2025-02-28',
    totalQuestions: 3,
    completedQuestions: 3,
    score: 88,
  },
  {
    id: '3',
    title: '计算机视觉方向考核',
    round: '第三轮考核',
    status: 'not-started',
    startDate: '2025-03-20',
    endDate: '2025-04-05',
    totalQuestions: 0,
    completedQuestions: 0,
    daysUntilStart: 10,
  },
]

// ==================== 经历数据（统一格式） ====================

/** Mock项目经历列表 - 对应后端 ExperienceDTO (type=project) */
export const mockExperiences: Experience[] = [
  {
    id: '1',
    type: 'project',
    name: '智能交通监控系统',
    role: '项目负责人',
    startDate: '2024.09',
    endDate: '2025.01',
    description:
      '基于YOLOv8和DeepSort实现的多目标跟踪系统，用于实时监测道路交通流量和违章行为。系统支持同时跟踪多个车辆目标，准确率达到95%以上。',
    techStack: ['Python', 'PyTorch', 'OpenCV', 'YOLOv8'],
    demoUrl: 'https://demo.traffic-monitor.com',
  },
  {
    id: '2',
    type: 'project',
    name: '人脸识别门禁系统',
    role: '核心开发',
    startDate: '2024.06',
    endDate: '2024.08',
    description:
      '为实验室开发的智能门禁系统，采用FaceNet进行人脸识别，支持活体检测防止照片欺骗。系统已部署使用，日均识别成功率99.2%。',
    techStack: ['Python', 'TensorFlow', 'Flask', 'MySQL'],
    demoUrl: 'https://github.com/zhangsan/face-access',
  },
  {
    id: '3',
    type: 'competition',
    name: '全国大学生计算机设计大赛',
    role: '团队负责人',
    startDate: '2024.08',
    endDate: '2024.08',
    date: '2024年8月',
    level: '国家级',
    award: '一等奖',
    teamSize: 3,
    description:
      '参赛作品为"基于深度学习的智能垃圾分类系统"，在全国总决赛中获得一等奖。负责算法设计和模型训练，项目准确率达到94.5%。',
    certificateUrl: '#',
  },
  {
    id: '4',
    type: 'competition',
    name: '"互联网+"大学生创新创业大赛',
    role: '技术负责人',
    startDate: '2024.06',
    endDate: '2024.06',
    date: '2024年6月',
    level: '省级',
    award: '二等奖',
    teamSize: 5,
    description:
      '参赛项目为"智能养老陪伴机器人"，负责机器人视觉系统开发和语音交互模块设计。项目获得省级二等奖。',
  },
  {
    id: '5',
    type: 'competition',
    name: 'ACM程序设计大赛',
    role: '参赛选手',
    startDate: '2024.04',
    endDate: '2024.04',
    date: '2024年4月',
    level: '区域赛',
    award: '铜牌',
    teamSize: 3,
    description: '代表学校参加ACM-ICPC亚洲区域赛，在团队中负责动态规划和图论题目，最终获得铜牌。',
  },
  {
    id: '6',
    type: 'internship',
    name: '字节跳动',
    company: '字节跳动',
    position: '算法实习生',
    startDate: '2025.01',
    endDate: '',
    status: 'active',
    description:
      '在推荐算法团队参与短视频推荐系统的优化工作，主要负责用户兴趣建模和召回策略的改进。',
    achievements: ['优化用户兴趣模型，点击率提升3%', '设计新的召回策略，覆盖用户数增加15%'],
  },
  {
    id: '7',
    type: 'internship',
    name: '腾讯',
    company: '腾讯',
    position: '后端开发实习生',
    startDate: '2024.06',
    endDate: '2024.09',
    status: 'ended',
    description: '在微信支付团队参与商户管理系统开发，负责交易流水查询和账单生成模块的开发和维护。',
    achievements: ['独立完成账单导出功能开发', '参与系统性能优化，查询速度提升40%'],
  },
]

/** Mock项目经历列表（兼容旧代码） */
export const mockProjects = mockExperiences.filter((e) => e.type === 'project')

/** Mock竞赛经历列表（兼容旧代码） */
export const mockCompetitions = mockExperiences.filter((e) => e.type === 'competition')

/** Mock实习经历列表（兼容旧代码） */
export const mockInternships = mockExperiences.filter((e) => e.type === 'internship')
