## Why

用户从主页的 DirectionIntroduce 组件点击方向卡片后，需要能够查看各方向（计算机视觉、嵌入式开发、结构设计）的详细信息页面。目前这些路由目标页面尚未实现，用户点击后无法获取方向介绍、技术栈、学习路径、职业发展和招新要求等关键信息。

## What Changes

- 新增 `/direction/cv` 路由页面 - 计算机视觉方向详情页
- 新增 `/direction/embed` 路由页面 - 嵌入式开发方向详情页
- 新增 `/direction/struct` 路由页面 - 结构设计方向详情页
- 每个页面包含以下模块：
  - Hero Section：方向标题、描述、背景装饰
  - 核心技术栈：4个技术卡片展示
  - 学习路径：4步学习流程展示
  - 职业发展方向：核心岗位、转型方向、就业公司展示
  - 招新信息：招新要求和"立即申请"按钮
- 支持桌面端(1440px)和移动端(375px)响应式布局
- 各方向使用不同主题色（计算机视觉-紫色、嵌入式-绿色、结构设计-蓝色）

## Capabilities

### New Capabilities

- `direction-detail-page`: 方向详情页面组件，展示团队各方向的详细介绍、技术栈、学习路径、职业发展和招新信息

### Modified Capabilities

- 无（纯新增页面，不修改现有功能需求）

## Impact

### 前端代码

- 新增 `src/app/(public)/(other)/direction/[slug]/page.tsx` - 动态路由页面
- 新增 `src/components/Direction/` 目录，包含：
  - `HeroSection/` - Hero 区域组件
  - `TechStack/` - 技术栈展示组件
  - `LearningPath/` - 学习路径组件
  - `CareerSection/` - 职业发展组件
  - `RecruitmentInfo/` - 招新信息组件
- 新增 `src/data/directions/` 目录，存放各方向的静态数据

### 设计资源

- 需从设计文件 `docs/UI/directions.pen` 导出相关图片资源

### 依赖

- 无新增外部依赖，使用现有 Ant Design 组件和项目样式体系
