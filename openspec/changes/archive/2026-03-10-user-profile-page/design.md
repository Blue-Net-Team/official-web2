## Context

蓝网团队招新系统需要一个个人主页功能，用于展示和管理用户的个人信息、考核进度、项目/竞赛/实习经历。当前系统已有基础的登录认证和成员列表功能，需要在此基础上新增用户个人中心模块。

### 现有技术栈
- Next.js 14+ (App Router)
- React 18+
- TypeScript
- CSS Modules
- Ant Design (部分组件)
- Zustand (状态管理)

### UI设计参考
设计稿位于 `docs/UI/user-profile/index.html`，采用暗色主题，包含：
- 左侧固定个人信息卡片（340px宽度）
- 右侧Tab切换内容区
- 响应式布局适配

**注意**: 设计稿中的顶部导航栏不需要实现，因为现有的 `(public)/layout.tsx` 已经包含了 `PublicNavbar` 组件，页面会自动继承该导航栏。

## Goals / Non-Goals

**Goals:**
- 实现完整的个人主页UI，遵循设计稿
- 使用Mock数据模拟后端API
- 页面采用服务端组件架构
- 实现Tab切换时数据按需加载
- CSS样式与现有页面风格一致

**Non-Goals:**
- 不实现邮箱修改功能（UI保留但禁用）
- 不实现GitHub绑定功能（UI保留但禁用）
- 不实现真实的后端API集成
- 不实现头像上传功能（仅展示）

## Decisions

### D1: 路由结构
**决定**: 使用 `(public)/(other)/profile` 路由结构

**理由**:
- 复用现有的 `(public)` 路由组，自动继承 `PublicNavbar` 导航栏
- 与现有的 `(public)/(other)/login`、`members` 等页面保持一致
- 无需单独实现顶部导航栏
- 后续可以通过中间件添加认证保护

**路由位置**: `src/app/(public)/(other)/profile/`

**注意**: 不实现设计稿中的顶部导航栏（`.top-nav`），复用现有layout中的 `PublicNavbar`

### D2: 组件架构
**决定**: 页面为服务端组件，交互部分为客户端组件

**架构**:
```
page.tsx (服务端组件)
├── ProfileSidebar (服务端组件) - 左侧信息卡片
├── ProfileTabs (客户端组件) - Tab切换控制
│   ├── ProfileInfo (客户端组件) - 表单编辑
│   ├── AssessmentList (服务端组件) - 考核列表
│   └── ExperienceSection (客户端组件) - 经历管理
└── Tab内容 (通过Suspense + URL参数实现服务端渲染)
```

**理由**:
- 最大化服务端组件使用
- Tab切换通过URL参数 `?tab=xxx` 控制
- 每个Tab内容可以独立进行服务端渲染
- 仅交互部分（表单提交、编辑删除）需要客户端组件

### D3: Tab数据加载策略
**决定**: 使用URL参数 + 服务端渲染

**实现**:
- URL格式: `/profile?tab=assessment`
- 页面根据tab参数渲染对应内容
- 每个Tab的数据在服务端获取
- Tab计数通过独立API获取（或包含在profile API中）

**理由**:
- 支持直接链接到特定Tab
- SEO友好
- 符合Next.js App Router最佳实践
- 减少客户端JavaScript

**替代方案**: 纯客户端Tab切换
- 优点：切换更快
- 缺点：无法分享链接，首屏需加载所有数据

### D4: Mock数据层设计
**决定**: 创建独立的Mock服务层

**结构**:
```
src/mocks/
├── data/
│   └── profile.ts          # Mock数据定义
├── services/
│   └── profile.service.ts  # Mock API服务
└── index.ts                # 导出
```

**理由**:
- 与真实API服务层结构一致
- 便于后续替换为真实API
- 支持模拟延迟、错误等场景

### D5: CSS样式方案
**决定**: 使用CSS Modules，参考现有页面写法

**参考**: `src/app/(public)/(other)/members/styles.module.css`

**样式命名约定**:
```css
.pageContainer { }      /* 页面容器 */
.pageBg { }             /* 页面背景 */
.mainContent { }        /* 主内容区 */
.sidebar { }            /* 侧边栏 */
.contentArea { }        /* 内容区 */
.sectionTabs { }        /* Tab容器 */
.tabBtn { }             /* Tab按钮 */
.tabBtnActive { }       /* 激活Tab */
```

**理由**:
- 与项目现有风格一致
- 不引入额外依赖
- 支持CSS变量和媒体查询

## Risks / Trade-offs

### R1: 服务端组件与客户端交互
**风险**: Tab切换时服务端渲染可能导致页面闪烁
**缓解**:
- 使用 `loading.tsx` 提供加载状态
- 考虑使用 `useTransition` 进行平滑过渡
- 对于编辑等交互使用客户端组件

### R2: Mock数据与真实API的差异
**风险**: Mock数据结构可能与后续真实API不一致
**缓解**:
- 定义完整的TypeScript类型
- Mock服务层模拟真实API行为
- 在设计文档中明确API契约

### R3: 响应式布局复杂度
**风险**: 设计稿包含大量响应式样式，实现工作量大
**缓解**:
- 优先实现桌面端布局
- 使用CSS媒体查询逐步适配移动端
- 参考设计稿中的断点设置

## Migration Plan

### 阶段1: 基础结构 (Day 1)
1. 创建路由和页面文件
2. 实现基础CSS样式
3. 创建Mock数据和服务

### 阶段2: 左侧卡片 (Day 1)
1. 实现ProfileSidebar组件
2. 实现头像展示区域
3. 实现基本信息展示
4. 实现统计数据展示

### 阶段3: Tab系统 (Day 2)
1. 实现Tab切换逻辑
2. 实现个人信息Tab
3. 实现考核列表Tab

### 阶段4: 经历管理 (Day 2-3)
1. 实现项目经历Tab
2. 实现竞赛经历Tab
3. 实现实习经历Tab

### 阶段5: 优化和测试 (Day 3)
1. 响应式适配
2. 交互优化
3. 边界情况处理

## Open Questions

1. **Tab数据缓存策略**: 是否需要在客户端缓存已加载的Tab数据？
   - 当前方案：每次Tab切换都重新获取
   - 可选方案：使用React Query或SWR进行缓存

2. **编辑表单弹窗 vs 页面内编辑**:
   - 设计稿显示为页面内编辑
   - 如果后续需要弹窗编辑，需要调整组件结构

3. **头像上传**:
   - 当前仅展示，后续需要确定上传方案
   - 可选：本地预览 + 预签名URL上传到OSS
