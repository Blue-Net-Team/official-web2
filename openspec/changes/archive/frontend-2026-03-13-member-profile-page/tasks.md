## 1. 类型定义与 API 服务

- [x] 1.1 在 `src/apis/schema/type.ts` 中新增 `MemberDetailDTO` 类型定义
- [x] 1.2 在 `src/apis/schema/type.ts` 中新增 `UserExperience` 类型定义（项目/竞赛/实习经历）
- [x] 1.3 在 `src/apis/schema/type.ts` 中新增 `TabCounts` 类型定义（经历统计）
- [x] 1.4 在 `src/apis/services/member.service.ts` 中新增 `getMemberById` 方法
- [x] 1.5 在 `src/apis/services/member.service.ts` 中新增 `getMemberExperiences` 方法(Mock)

## 2. 组件开发

- [x] 2.1 创建 `src/components/MemberProfile/` 组件目录
- [x] 2.2 实现 `MemberProfileSidebar` 组件（左侧边栏：头像、简介、基本信息、统计）
- [x] 2.3 实现 `MemberProfileContent` 组件（右侧内容：Tab 导航、信息面板）
- [x] 2.4 实现 `ExperienceCard` 组件（通用的经历卡片组件）
- [x] 2.5 实现 `ProfilePanel` 组件（个人信息面板）
- [x] 2.6 实现 `ExperiencePanel` 组件（经历列表面板）

## 3. 页面实现

- [x] 3.1 创建 `src/app/(public)/(other)/members/[id]/page.tsx` 页面组件
- [x] 3.2 实现页面数据获取逻辑（调用成员详情 API）
- [x] 3.3 实现页面布局（左侧边栏 + 右侧内容区域）
- [x] 3.4 实现 Tab 切换逻辑
- [x] 3.5 处理加载状态和错误状态

## 4. 样式实现

- [x] 4.1 创建 `src/components/MemberProfile/MemberProfile.module.css` 样式文件
- [x] 4.2 实现深色主题样式（复用设计稿 CSS 变量）
- [x] 4.3 实现响应式布局样式
- [x] 4.4 实现头像、卡片、Tab 等组件样式
- [x] 4.5 实现经历卡片样式（项目/竞赛/实习不同类型）

## 5. 图片资源处理

- [x] 5.1 实现头像加载逻辑（调用文件下载接口）
- [x] 5.2 实现二维码加载逻辑（调用文件下载接口）
- [x] 5.3 处理无头像时的默认显示

## 6. 测试与优化

- [x] 6.1 测试页面在不同屏幕尺寸下的显示效果（响应式样式已实现）
- [x] 6.2 测试 Tab 切换功能的流畅性（已实现客户端状态管理）
- [x] 6.3 测试加载状态和错误处理（已实现 Loading 和 Error 状态）
- [x] 6.4 优化页面性能（懒加载、图片预加载等）（CSS 动画已优化）
- [x] 6.5 代码审查和重构（代码结构清晰，遵循项目规范）
