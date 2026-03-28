## 1. 基础设施搭建

- [x] 1.1 创建 `(public)/(other)/profile` 路由结构（复用现有layout，不创建新路由组）
- [x] 1.2 创建 `page.tsx` 服务端组件和 `styles.module.css` 样式文件
- [x] 1.3 创建 `loading.tsx` 加载状态组件
- [x] 1.4 定义TypeScript类型 (`src/types/profile.ts`)
  - UserProfile, UserStats, Assessment, Project, Competition, Internship
- [x] 1.5 注意：不实现顶部导航栏，复用现有layout的 `PublicNavbar`

## 2. Mock数据层

- [x] 2.1 创建Mock数据文件 (`src/mocks/data/profile.ts`)
  - 用户基本信息数据
  - 考核列表数据（进行中/已结束/未开始三种状态）
  - 项目经历数据
  - 竞赛经历数据
  - 实习经历数据
- [x] 2.2 创建Mock服务文件 (`src/mocks/services/profile.service.ts`)
  - getProfile() - 获取用户信息和统计
  - updateProfile() - 更新用户信息
  - getAssessments() - 获取考核列表
  - getProjects() / createProject() / updateProject() / deleteProject()
  - getCompetitions() / createCompetition() / updateCompetition() / deleteCompetition()
  - getInternships() / createInternship() / updateInternship() / deleteInternship()
  - getTabCounts() - 获取各Tab计数

## 3. 页面基础样式

- [x] 3.1 创建页面容器样式 (.pageContainer, .pageBg)
- [x] 3.2 创建主内容区布局样式 (.mainContent, .sidebar, .contentArea)
- [x] 3.3 创建响应式媒体查询（1024px, 640px断点）

## 4. 左侧用户信息卡片组件

- [x] 4.1 创建 `ProfileSidebar` 服务端组件
- [x] 4.2 实现头像展示区域
  - 头像环效果
  - 头像编辑按钮（仅UI）
- [x] 4.3 实现姓名、昵称、角色标签展示
- [x] 4.4 实现个人简介区域
- [x] 4.5 实现基本信息列表（学院、专业、年级图标+文字）
- [x] 4.6 实现报名方向展示
- [x] 4.7 实现统计数据区域（考核轮次、已完成、平均分）

## 5. Tab导航系统

- [x] 5.1 创建Tab导航样式 (.sectionTabs, .tabBtn, .tabBtnActive)
- [x] 5.2 实现Tab导航组件 (服务端渲染)
  - 个人信息Tab
  - 我的考核Tab（带计数Badge）
  - 项目经历Tab（带计数Badge）
  - 竞赛经历Tab（带计数Badge）
  - 实习经历Tab（带计数Badge）
- [x] 5.3 实现Tab URL参数切换逻辑 (?tab=xxx)

## 6. 个人信息Tab

- [x] 6.1 创建 `ProfileInfo` 客户端组件
- [x] 6.2 实现基本信息表单
  - 姓名（必填）
  - 昵称
  - 学号（禁用）
  - 年级（必填，下拉选择）
  - 学院（必填，下拉选择）
  - 专业（必填，输入框）
  - 报名方向（必填，下拉选择）
  - GitHub链接
  - 个人简介（多行文本）
- [x] 6.3 实现邮箱设置区域（只读展示，修改按钮禁用）
- [x] 6.4 实现保存/取消按钮
- [x] 6.5 实现表单验证和提交逻辑

## 7. 我的考核Tab

- [x] 7.1 创建 `AssessmentList` 服务端组件
- [x] 7.2 创建考核卡片样式
  - .assessmentCard, .assessmentHeader, .assessmentIcon
  - .assessmentStatus (not-started/in-progress/ended)
  - .progressBar, .progressFill
- [x] 7.3 实现进行中考核卡片
  - 剩余时间显示
  - 进度条
  - "继续答题"入口
- [x] 7.4 实现已结束考核卡片
  - 最终得分显示
  - "查看详情"入口
- [x] 7.5 实现未开始考核卡片
  - 距开始时间显示
  - "暂不可进入"提示
- [x] 7.6 实现空状态展示

## 8. 项目经历Tab

- [x] 8.1 创建 `ExperienceSection` 客户端组件（通用经历管理组件）
- [x] 8.2 创建项目经历卡片样式
  - .experienceCard, .experienceIcon.project
  - .techTag 技术标签
  - .experienceLinks, .experienceActions
- [x] 8.3 实现项目卡片展示
  - 项目名称、角色、时间
  - 项目描述
  - 技术栈标签
  - GitHub/演示链接
- [x] 8.4 实现"添加项目"按钮和表单（Modal或页面内）
- [x] 8.5 实现项目编辑功能
- [x] 8.6 实现项目删除确认功能

## 9. 竞赛经历Tab

- [x] 9.1 创建竞赛经历卡片样式
  - .experienceIcon.competition
  - .awardBadge (first/second/third)
  - .competitionMeta
- [x] 9.2 实现竞赛卡片展示
  - 竞赛名称、角色、获奖等级
  - 竞赛时间、团队人数
  - 竞赛描述
- [x] 9.3 实现"添加竞赛"按钮和表单
- [x] 9.4 实现竞赛编辑功能
- [x] 9.5 实现竞赛删除确认功能

## 10. 实习经历Tab

- [x] 10.1 创建实习经历卡片样式
  - .experienceIcon.internship
  - .internshipBadge (在职/已离职)
  - .internshipAchievement
- [x] 10.2 实现实习卡片展示
  - 公司、职位、时间
  - 在职状态
  - 工作描述
  - 主要成就
- [x] 10.3 实现"添加实习"按钮和表单
- [x] 10.4 实现实习编辑功能
- [x] 10.5 实现实习删除确认功能

## 11. 响应式适配

- [x] 11.1 实现1024px断点适配
  - 左右布局改为上下布局
  - Tab导航样式调整
- [x] 11.2 实现640px断点适配
  - 表单单列布局
  - 卡片内容紧凑显示
  - 按钮全宽显示

## 12. 测试和优化

- [x] 12.1 验证所有Tab切换功能
- [x] 12.2 验证表单验证逻辑
- [x] 12.3 验证响应式布局
- [x] 12.4 代码清理和注释
