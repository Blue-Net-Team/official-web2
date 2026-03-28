## Why

目前蓝网团队招新系统缺少个人主页功能，用户（考生/成员）无法查看和管理自己的个人信息、考核进度、项目经历、竞赛经历和实习经历。这导致用户需要通过管理员才能了解自己的考核状态和成绩，也无法展示个人技术成长轨迹。为提升用户体验和系统完整性，需要开发一个功能完整的个人主页界面。

## What Changes

### 新增功能
- **个人主页页面**: 创建 `/profile` 路由，展示用户完整个人信息
- **左侧用户信息卡片**:
  - 用户头像（支持编辑）
  - 姓名、昵称
  - 角色标签（考生/正式成员）
  - 个人简介
  - 基本信息（学院、专业、年级）
  - 报名方向展示
  - 统计数据（考核轮次、已完成数、平均分）
- **右侧Tab内容区**:
  - **个人信息Tab**: 基本信息表单编辑（姓名、昵称、年级、学院、专业、方向、GitHub、个人简介）
  - **我的考核Tab**: 考核列表卡片（进行中/已结束/未开始状态）
  - **项目经历Tab**: 项目经历卡片列表（支持增删改）
  - **竞赛经历Tab**: 竞赛经历卡片列表（支持增删改，包含获奖等级）
  - **实习经历Tab**: 实习经历卡片列表（支持增删改，包含在职状态）
- **Tab计数Badge**: 显示各类型数据数量（从后端获取）
- **Mock数据服务**: 创建完整的mock数据层，模拟所有API响应

### 暂不实现的功能
- 修改邮箱功能（UI保留但禁用）
- 绑定GitHub功能（UI保留但禁用）

### 技术约束
- 页面(page.tsx)必须是Next.js服务端组件
- 尽可能使用服务端组件
- 样式使用CSS Modules（参考现有page写法），不使用Tailwind CSS
- 切换Tab时通过URL参数或服务端渲染获取数据

## Capabilities

### New Capabilities
- `user-profile`: 个人主页功能，包含用户信息展示、编辑以及各类经历管理
- `user-experience`: 用户经历管理功能，包含项目经历、竞赛经历、实习经历的增删改查

### Modified Capabilities
<!-- 无现有capability需要修改 -->

## Impact

### 新增文件
- `src/app/(public)/(other)/profile/page.tsx` - 个人主页服务端组件
- `src/app/(public)/(other)/profile/styles.module.css` - 页面样式
- `src/app/(public)/(other)/profile/_components/` - 页面子组件目录
  - `ProfileSidebar.tsx` - 左侧信息卡片组件
  - `ProfileTabs.tsx` - Tab切换组件
  - `ProfileInfo.tsx` - 个人信息编辑组件
  - `AssessmentList.tsx` - 考核列表组件
  - `ExperienceSection.tsx` - 经历管理组件（通用）
- `src/mocks/services/profile.service.ts` - Mock数据服务
- `src/mocks/data/profile.ts` - Mock数据

### 依赖关系
- 复用现有的 `(public)` 路由组，继承 `PublicNavbar` 导航栏
- 复用现有的API服务层结构
- 需要定义用户画像相关的TypeScript类型

### 注意事项
- **不实现设计稿中的顶部导航栏**：现有layout已有 `PublicNavbar`，页面自动继承
- 页面内容从设计稿中的 `.main-content` 开始实现

### API接口（Mock）
- `GET /api/profile` - 获取用户基本信息和统计数据
- `PUT /api/profile` - 更新用户基本信息
- `GET /api/profile/assessments` - 获取考核列表
- `GET /api/profile/projects` - 获取项目经历列表
- `POST /api/profile/projects` - 添加项目经历
- `PUT /api/profile/projects/:id` - 更新项目经历
- `DELETE /api/profile/projects/:id` - 删除项目经历
- `GET /api/profile/competitions` - 获取竞赛经历列表
- `POST /api/profile/competitions` - 添加竞赛经历
- `PUT /api/profile/competitions/:id` - 更新竞赛经历
- `DELETE /api/profile/competitions/:id` - 删除竞赛经历
- `GET /api/profile/internships` - 获取实习经历列表
- `POST /api/profile/internships` - 添加实习经历
- `PUT /api/profile/internships/:id` - 更新实习经历
- `DELETE /api/profile/internships/:id` - 删除实习经历
