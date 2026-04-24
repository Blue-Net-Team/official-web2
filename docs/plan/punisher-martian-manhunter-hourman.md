# 前端架构重构与规范整改开发计划（整合版）

## 项目现状

- **前端框架**: Next.js 15 (App Router) + React 19 + TypeScript 5.9
- **UI 库**: Ant Design 6 + Tailwind CSS 4
- **状态管理**: Zustand
- **HTTP 客户端**: Axios
- **包管理器**: pnpm
- **当前服务**: 端口 3000 有 Next.js dev server 运行，端口 8080 后端运行

## 整合方案说明

本计划整合了深度代码审查发现的问题与 GLM 提供的架构优化方案，按 **P0（Critical）→ P1（High）→ P2（Medium）** 优先级推进。

**验证方式**：用户启动前端 dev server，我使用 playwright 进行页面功能验证和视觉回归检查，**不执行 build** 以避免意外问题。

---

## 组件重叠深度分析（用户重点关注）

### 重叠 1：`MemberProfile` vs `Profile`（严重程度：高）

两个目录分别服务于"成员详情页"（`/members/[id]`）和"个人中心"（`/profile`），但存在大量重复/可复用的组件和逻辑：

#### 1.1 `ProfileInfo` vs `ProfilePanel` —— 完全相同的展示字段

| 字段 | `ProfileInfo`（个人中心） | `ProfilePanel`（成员详情） |
|------|-------------------------|--------------------------|
| 用户名 | ✅ 展示 + 编辑 | ✅ 只读展示 |
| 昵称 | ✅ 展示 + 编辑 | ✅ 只读展示 |
| 年级 | ✅ 展示（不可编辑） | ✅ 只读展示 |
| 学院 | ✅ 展示 + 编辑 | ✅ 只读展示 |
| 专业 | ✅ 展示 + 编辑 | ✅ 只读展示 |
| 方向 | ✅ 展示 + 编辑 | ✅ 只读展示 |
| 性别 | ✅ 展示 + 编辑 | ✅ 只读展示 |
| 角色 | ✅ Tag 展示 | ✅ Tag 展示 |
| 个人简介 | ✅ 展示 + 编辑 | ✅ 只读展示 |

**结论**：`ProfilePanel`（95行）是 `ProfileInfo`（322行）的只读子集。`ProfileInfo` 内已经包含只读展示模式（`isEditing === false` 时），完全可复用。

#### 1.2 `ExperienceSection` vs `ExperiencePanel` —— 相同的渲染逻辑

- `ExperienceSection`（301行）：个人中心，支持增删改 + Modal 表单
- `ExperiencePanel`（61行）：成员详情页，只读展示
- **两者都渲染 `ExperienceCard`**，都有 `getIcon()` 和空状态渲染
- `ExperiencePanel` 是 `ExperienceSection` 的只读子集

#### 1.3 `MemberProfileContent` vs `ProfileTabs` —— 完全相同的 Tab 导航

两者 Tab 配置**字段、图标、样式完全一致**：
```typescript
// MemberProfileContent 的 TAB_CONFIG
{ key: 'profile', icon: UserOutlined },
{ key: 'projects', icon: FolderOutlined },
{ key: 'competitions', icon: TrophyOutlined },
{ key: 'internships', icon: SolutionOutlined }

// ProfileTabs 的 allTabs（前4个相同）
{ key: 'profile', icon: <UserOutlined /> },
{ key: 'projects', icon: <FolderOutlined /> },
{ key: 'competitions', icon: <TrophyOutlined /> },
{ key: 'internships', icon: <SolutionOutlined /> }
```

样式也完全复制：相同的 `nav className`、`backdrop-blur-[20px]`、`bg-gradient-to-br from-[#6677ff] to-[#2f27b0]` active 状态。

#### 1.4 页面级重复 —— `members/[id]/page.tsx` vs `profile/page.tsx`

- 两者页面结构：**左侧 `ProfileSidebar` + 右侧内容区**
- 背景样式：**完全复制粘贴**（相同的 radial-gradient CSS）
- 加载状态/错误状态：**完全复制粘贴**

#### 1.5 `ProfileSidebar` 已意识到重叠

注释明确说明：
> "统一的侧边栏用户数据接口，适配 UserInfo（个人主页）和 MemberDetailDTO（成员详情页）"

但存在手动适配函数 `adaptToSidebarProfile`，说明**后端 DTO 字段命名不一致**（`role` vs `roleName`）。

### 重叠 2：`AssessmentCard` 的复杂度膨胀（严重程度：中）

`AssessmentCard`（204行）接受 `Assessment | AssessmentTimeDTO` 两种类型：
- 需要 `isAssessmentTimeDTO` 类型守卫
- 需要从 title 中 `extractEpochFromTitle` 解析轮次
- 内部定义了 `formatDate`、`getEpochLabel`、`getAssessmentStatus`、`extractEpochFromTitle`
- 这些工具函数应该提取到 `utils/assessment.ts`

### 重叠 3：`AssessmentStatus` 类型重复定义（严重程度：低）

- `types/profile.ts` 第21行：`export type AssessmentStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED'`
- `apis/schema/assessment.dto.ts`：也定义了 `AssessmentStatus`

---

## 组件重叠重构方案

### 方案：合并 `MemberProfile` 到 `Profile` 模块

**目标**：删除 `components/MemberProfile/` 目录，所有功能由 `Profile` 模块复用提供。

**重构后目录**：
```
components/Profile/
├── ProfileSidebar/              # 保持（已统一，但移除 adaptToSidebarProfile）
├── ProfileInfo/
│   ├── index.tsx                # 管理编辑状态（原 ProfileInfo）
│   ├── ProfileInfoDisplay.tsx   # 纯展示模式（替代 ProfilePanel）
│   └── ProfileInfoEdit.tsx      # 编辑表单
├── ExperienceSection/
│   ├── index.tsx                # 支持 readOnly prop（替代 ExperiencePanel）
│   └── hooks/
│       └── useExperience.ts     # 经历 CRUD
├── ExperienceCard/              # 保持
├── ProfileTabs/                 # 保持，扩展为通用 TabNav
├── AssessmentList/              # 保持
├── AvatarCropModal/             # 保持
└── hooks/
    ├── useProfileData.ts
    └── useExperienceActions.ts
```

**删除**：
- `components/MemberProfile/` 整个目录
- `components/MemberProfile/ProfilePanel.tsx` → 由 `ProfileInfoDisplay` 替代
- `components/MemberProfile/ExperiencePanel.tsx` → 由 `ExperienceSection(readOnly)` 替代
- `components/MemberProfile/MemberProfileContent.tsx` → 由 `ProfileTabs` + `ExperienceSection` 替代

**修改页面**：
- `members/[id]/page.tsx`：
  ```tsx
  import { ProfileSidebar, ProfileInfoDisplay, ExperienceSection, ProfileTabs } from '@/components/Profile'
  // 不再需要从 MemberProfile 导入
  ```
- `profile/page.tsx`：导入路径保持不变

**后端配合**：统一 `MemberDetailDTO.role` 和 `UserInfo.roleName` 字段名，消除 `adaptToSidebarProfile` 的必要性。

---

## Phase 0: 环境准备（0.5 天）

| 任务 | 说明 | 负责人 |
|------|------|--------|
| 关闭/重启前端 dev server | 杀掉 PID 52776（端口 3000 占用） | 用户 |
| 确认后端服务可用 | 8080 端口后端正常运行 | 用户 |
| 创建 feature branch | `git checkout -b refactor/frontend-standards` | 我 |
| 首次 playwright 截图存档 | 记录各页面当前状态作为回归基线 | 我 |

---

## Phase 1: Critical 修复 + 基础设施（1.5 天）

**目标**: 修复会直接导致运行时错误 or 安全问题的代码，建立 hooks 层基础

### 1.1 API Client 重构（Critical）

**修复项**:
- **超时错误处理**：`client.ts` 中移除 `Promise.resolve` 伪装，改为正常 reject
- **重复代码消除**：用 `createApiClient` 工厂函数统一 `apiClient` 和 `publicClient` 的公共逻辑（CSRF 注入、超时处理）
- **错误拦截器**：统一在 `apiClient` 中处理 `code >= 400` 的响应，使用 Ant Design `message`（浏览器环境安全）
- **清理废弃代码**：删除 `setCsrfToken` 空函数及相关调用

**验证**: playwright 访问登录/报名页面，确认 API 调用正常、错误提示正确弹出

### 1.2 删除 Wrapper.tsx（Critical）

- 删除 `components/Wrapper.tsx`
- 修改 `app/(public)/(home)/page.tsx` 和其他使用 `Wrapper` 的页面，直接传递数据
- 错误提示由 `client.ts` 拦截器接管

**验证**: playwright 截图对比首页，确认无 UI 变化

### 1.3 统一 Service 层返回值（Critical）

- **所有 service 统一返回 `Promise<ResponseMessage<T>>`**
- 修复 `member.service.ts` 中 `return response.data.data!` 直接解包的问题
- `MemberService` → `memberService`（camelCase），同步修改所有调用方
- `CompetitionService` → `competitionService`，同步修改所有调用方

**验证**: `pnpm lint` 无 TS 错误

### 1.4 移除前端密码哈希（Critical）

- `authStore.ts` 中移除 `hashPassword` 调用
- 直接传递原始密码（依赖 HTTPS）
- 保留 `utils/passwordHash.ts` 但标记为 `@deprecated`

**验证**: playwright 测试登录流程正常

### 1.5 修复 Footer 残留文案（Critical）→ 延后处理

- ~~将 "© 2024 Ant Design Demo. All rights reserved." 改为项目实际信息~~
- **用户确认：Footer 先不改，延后处理**

### 1.6 创建 hooks 层基础（P0 from GLM）

新建目录结构：
```
src/hooks/
├── useApi.ts              # 通用 API 调用 Hook
├── usePagination.ts       # 分页数据获取 Hook
├── useAuth.ts             # 认证状态 Hook（封装 authStore）
├── index.ts               # 统一导出入口
├── assessment/
│   ├── useAssessmentTime.ts
│   └── useAssessmentSession.ts
├── admin/
│   ├── useAdminTable.ts   # 通用表格 CRUD
│   └── useDrawer.ts       # Drawer 开关管理
└── profile/
    ├── useProfileData.ts
    └── useExperienceActions.ts
```

**核心 Hook 设计**：
```typescript
// useApi.ts
interface UseApiReturn<T> {
  data: T | null
  loading: boolean
  error: unknown
  execute: (...args: unknown[]) => Promise<T | null>
  reset: () => void
}
```

**首批迁移目标**（从重复最多的模式开始）：
- `profile/page.tsx` 的数据获取逻辑 → `useProfileData.ts`
- `members/page.tsx` 成员列表获取 → `useApi(memberService.getMemberList)`
- `competitions/page.tsx` 竞赛列表获取 → `usePagination`

**验证**: playwright 访问 profile、members、competitions 页面，确认数据和加载状态正常

---

## Phase 2: 组件重叠重构 + 巨型组件拆分（2.5 天）

**目标**: 解决 `MemberProfile` vs `Profile` 重叠，拆分 God Component

### 2.1 合并 `MemberProfile` → `Profile` 模块

**步骤**：
1. **`ProfileInfo` 拆分**：
   - 提取只读展示部分为 `ProfileInfoDisplay.tsx`（替代 `ProfilePanel`）
   - 保留 `ProfileInfo/index.tsx` 管理编辑状态
   - `ProfileInfoDisplay` 的 props 使用 `SidebarProfile` 类型（已统一）

2. **`ExperienceSection` 添加 `readOnly` prop**：
   - `readOnly={true}` 时：隐藏添加按钮、编辑/删除操作按钮、Modal
   - `readOnly={false}` 时：保持现有完整功能
   - 删除 `ExperiencePanel`

3. **`ProfileTabs` 扩展为通用组件**：
   - 支持传入 `tabs` 配置（个人中心有5个Tab，成员详情有4个Tab）
   - `MemberProfileContent` 中的 Tab 导航由 `ProfileTabs` 替代

4. **删除 `components/MemberProfile/` 目录**

5. **修改 `members/[id]/page.tsx`**：
   ```tsx
   import { 
     ProfileSidebar, 
     ProfileInfoDisplay, 
     ExperienceSection, 
     ProfileTabs 
   } from '@/components/Profile'
   ```
   - **后端统一 `role` → `roleName`**，移除 `adaptToSidebarProfile`

6. **提取页面公共布局**：
   - `members/[id]/page.tsx` 和 `profile/page.tsx` 的背景、加载、错误状态完全一致
   - 提取为 `components/layout/ProfileLayout.tsx`

**验证**: playwright 截图对比 `members/1` 和 `/profile` 页面，确认 UI 完全一致

### 2.2 拆分 QuestionDetailPage（最高优先级，1522 行）

```
components/Assessment/
├── QuestionDetail/
│   ├── index.tsx                 # 主页面（~200 行，仅组合子组件）
│   ├── FileUploadQuestion.tsx    # 文件上传题
│   ├── ChoiceQuestion.tsx        # 选择题（单选+多选）
│   ├── AlgorithmQuestion.tsx     # 算法题
│   ├── QuestionSidebar.tsx       # 右侧边栏
│   ├── JudgeResultPanel.tsx      # 判题结果面板
│   ├── CountdownSection.tsx      # 倒计时区域
│   └── hooks/
│       └── useQuestionDetail.ts  # 页面级 Hook（状态管理、API 调用）
```

**迁移逻辑**：
- 四种题型的渲染逻辑 → 独立组件
- 状态管理和 API 调用 → `useQuestionDetail.ts`
- 工具函数（`getStatusInfo`、`formatDate`、`formatFileSize`）→ `utils/assessment.ts`、`utils/date.ts`、`utils/file.ts`

### 2.3 拆分 PublicNavbar（403 行）

```
components/PublicNavbar/
├── index.tsx                     # 主组件（~150 行）
├── UserDropdown.tsx              # 用户信息和下拉菜单
├── LoginButton.tsx               # 登录按钮（桌面/移动端）
├── MobileDrawer.tsx              # 移动端抽屉菜单
└── useMenuItems.ts               # 菜单项生成 Hook
```

### 2.4 拆分 EnrollPage（735 行）

```
components/Enroll/
├── ConsultationQrcode/           # 现有
├── DirectionSidebar.tsx          # 方向侧边栏
├── MobileDirectionSelector.tsx   # 移动端方向选择
├── AvatarUpload.tsx              # 头像上传
└── hooks/
    └── useEnrollForm.ts          # 表单逻辑 + API 调用
```

### 2.5 提取 Admin 通用模式

```
components/Admin/
├── ...（现有组件保持）
├── hooks/
│   ├── useAdminTable.ts          # 通用表格 CRUD（分页、搜索、删除）
│   └── useDrawer.ts              # Drawer 开关/编辑模式管理
└── AdminTable/                   # 通用管理表格组件（可选）
```

**验证**: playwright 截图对比拆分前后的页面，确认 UI 完全一致

---

## Phase 3: 类型系统 + 目录重组（1 天）

### 3.1 统一类型系统

**规则**：
- `apis/schema/` — 仅存放后端 DTO 对应类型，不修改
- `types/` — 仅存放前端独有类型、标签映射、UI 状态类型，**消除 re-export**

**具体动作**：
- 合并重复的 `AssessmentStatus` 到 `types/common.ts`
- `types/assessment.ts` — 合并标签映射，去除纯 re-export
- `types/competition.ts` — 去除纯 re-export
- `types/profile.ts` — 去除纯 re-export
- 消费者直接从 `apis/schema/` 或 `types/` 导入，路径明确

### 3.2 拆分 `apis/schema/type.ts`（722 行）

按模块拆分为：
```
apis/schema/
├── index.ts              # 统一导出入口
├── common.ts             # ResponseMessage, PageDTO
├── auth.dto.ts
├── member.dto.ts
├── competition.dto.ts
├── enrollment.dto.ts
├── achievement.dto.ts
├── venue.dto.ts
├── direction.dto.ts      # 已存在
├── profile.dto.ts        # 已存在
├── assessment.dto.ts     # 已存在
└── enumerate.ts          # 保持不变
```

### 3.3 重组组件目录

```
components/
├── providers/              # 新建
│   ├── AuthProvider.tsx    # 从根目录移入
│   ├── ThemeProvider.tsx   # 从根目录移入
│   └── index.ts
├── common/                 # 新建
│   ├── LoadingSpinner.tsx  # 统一加载组件
│   ├── ErrorState.tsx      # 统一错误状态
│   ├── ErrorPage/          # 从根目录移入
│   └── Footer/             # 从根目录移入
├── layout/                 # 新建
│   ├── ProfileLayout.tsx   # 提取 members/[id] 和 profile 公共布局
│   ├── PublicNavbar/       # 移入
│   └── AdminNav/           # 移入
├── Assessment/             # 保持
├── Home/                   # 保持
│   └── Competitions/
│       └── CompetitionCard/  # 从 components/ 根目录移入
├── Direction/              # 保持
├── Profile/                # 保持，合并 MemberProfile
│   ├── ProfileSidebar/
│   ├── ProfileInfo/
│   ├── ExperienceSection/
│   ├── ExperienceCard/
│   ├── ProfileTabs/
│   ├── AssessmentList/
│   ├── AvatarCropModal/
│   └── hooks/
├── Members/                # 保持（ Members 列表页组件）
├── Enroll/                 # 保持
├── Admin/                  # 保持
└── Achievements/           # 保持
```

**废弃移动**：
- `components/Wrapper.tsx` → 删除
- `components/CompetitionCard/` → 移入 `Home/Competitions/`
- `components/MemberProfile/` → 合并到 `Profile/`

**验证**: `pnpm lint` 无错误，playwright 各页面访问正常

---

## Phase 4: Next.js 架构优化（1 天）

### 4.1 添加 `middleware.ts` 路由守卫

```typescript
// src/middleware.ts
import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const PROTECTED_ROUTES = ['/admin', '/profile', '/assessment']
const AUTH_COOKIE = 'auth_token'

export function middleware(request: NextRequest) {
  const hasAuth = request.cookies.has(AUTH_COOKIE)
  const isProtected = PROTECTED_ROUTES.some(route =>
    request.nextUrl.pathname.startsWith(route)
  )

  if (isProtected && !hasAuth) {
    return NextResponse.redirect(new URL('/login', request.url))
  }
  return NextResponse.next()
}
```

**说明**：Middleware 只能检查 Cookie 存在性，完整 Token 验证仍由客户端 `checkAuthStatus` 负责。主要用于快速拦截未登录用户的直接 URL 访问。

**清理 client-side auth check**：
- `assessment/page.tsx` 中的 `useEffect` auth check → 删除（由 middleware 处理）
- `admin/layout.tsx` 中的权限检查 → 保留作为兜底

### 4.2 添加 `error.tsx` 错误边界

为以下路由添加：
- `app/(public)/error.tsx`
- `app/admin/error.tsx`

### 4.3 添加 `loading.tsx`

为数据获取较多的路由添加：
- `app/(public)/(other)/members/loading.tsx`
- `app/(public)/(other)/competitions/loading.tsx`

### 4.4 修复 `not-found.tsx`

- 移除 `<html>` 和 `<body>` 标签
- 移除重复的 `AntdRegistry/ThemeProvider/AuthProvider`
- 使其在 `layout.tsx` 的 children 位置正确渲染

### 4.5 迁移 `next lint` → ESLint CLI

- 更新 `package.json` scripts
- 清理现有 ESLint 警告（未使用变量）

**验证**: playwright 访问各页面，测试路由守卫行为、错误页面、加载状态

---

## Phase 5: 性能与样式优化（0.5 天）

### 5.1 提取业务逻辑到 utils

新建/补充：
```
utils/
├── assessment.ts         # calculateAssessmentStatus、formatTimeRemaining、convertToAssessment
├── date.ts               # formatDate、日期比较（替代 AssessmentCard 内联定义）
├── file.ts               # formatFileSize
├── constants/
│   ├── colors.ts         # 品牌色常量（#6677ff、#ff6b35、#2f27b0）
│   └── roles.ts          # 角色等级常量（替代 Magic Numbers）
└── index.ts              # 统一导出入口
```

### 5.2 修复 `document.querySelector` → `useRef`

- `competitions/page.tsx`
- `Members.tsx`

### 5.3 修复 `CompetitionLogo.tsx`

- Next.js Image 不支持 `onError`，改用 wrapper div + `<img>` 或 background-image 方案

### 5.4 修复 `TopContent` 双重 ConfigProvider

- 合并为一个 ConfigProvider
- 通过 Tailwind 响应式类控制按钮显示/隐藏

### 5.5 `globals.css` 去重

- 移除 `:root` 中与 `@theme` 重复的定义
- 统一使用 Tailwind v4 的 `@theme` 语法

### 5.6 Members 统计优化 → 不添加新接口

- ~~后端需提供 `/members/statistics` 接口~~
- **用户确认：先不添加新后端接口**
- 前端保留当前 `size: 1000` 方案，添加 TODO 注释说明后续优化方向

**验证**: playwright 截图对比，确认无视觉回归

---

## Phase 6: Auth Store 精简 + 收尾（0.5 天）

### 6.1 精简 Auth Store

- 移除 `setCsrfToken` 所有调用（已废弃）
- 将密码哈希逻辑移到调用层（`login/page.tsx`）——如已在前端移除哈希则跳过
- 将 `checkAuthStatus` 中的内存缓存逻辑修复：不依赖内存中的 `userInfo && csrfToken`，始终调用 `/auth/me` 验证

### 6.2 添加通用加载/错误组件

```
components/common/
├── LoadingSpinner.tsx      # 统一 Spin 包装
├── ErrorState.tsx          # 带重试按钮的错误展示
└── EmptyState.tsx          # 空数据状态
```

### 6.3 统一导出模式

全部组件目录的 `index.ts` 统一为：
```typescript
export { default as ComponentName } from './ComponentName'
export type { ComponentNameProps } from './ComponentName'
```

### 6.4 最终验证

| 检查项 | 标准 |
|--------|------|
| `pnpm lint` | 0 errors, 0 warnings |
| `pnpm dev` | 前端服务正常启动 |
| playwright 首页截图 | 与基线对比无异常 |
| playwright 登录流程 | 正常 |
| playwright 报名流程 | 正常 |
| playwright 管理后台 | 正常 |
| playwright 成员列表 | 正常 |
| playwright 考核页面 | 正常 |

---

## 工作流约定

### 开发流程
1. 每个 Phase 作为一个独立的 commit
2. Commit message: `refactor(scope): description`
3. 每次修改后运行 `pnpm lint` 检查
4. playwright 验证关键页面功能

### 用户职责
- 启动/维护前端 dev server（`pnpm dev`，端口 3000）
- 启动/维护后端服务（端口 8080）
- ~~提供 Footer 版权文案~~（延后）
- ~~确认后端接口调整~~（已确认：统一 roleName，不添加新接口）

### 我的职责
- 代码修改和重构
- `pnpm lint` 规范检查
- playwright 页面功能验证和截图对比
- **不执行 `pnpm build`**（按用户要求）

---

## 预计工期

| Phase | 内容 | 预计时间 |
|-------|------|----------|
| Phase 0 | 环境准备 + playwright 基线 | 0.5 天 |
| Phase 1 | Critical 修复 + hooks 层基础 | 1.5 天 |
| Phase 2 | 组件重叠重构 + 巨型组件拆分 | 2.5 天 |
| Phase 3 | 类型系统 + 目录重组 | 1 天 |
| Phase 4 | Next.js 架构优化 | 1 天 |
| Phase 5 | 性能与样式优化 | 0.5 天 |
| Phase 6 | Auth Store 精简 + 收尾 | 0.5 天 |
| **总计** | | **~7.5 天** |

---

## 已确认事项

| 事项 | 用户决策 | 执行方案 |
|------|----------|----------|
| Footer 文案 | 先不改 | 延后处理 |
| 后端字段 `role` vs `roleName` | **统一为 `roleName`** | 我同步调整后端 DTO 和前端适配代码 |
| Members 统计接口 | 不添加 | 前端保留 `size: 1000`，加 TODO 注释 |
| 开发节奏 | 我自己把握 | 优先保证代码质量，逐步推进 |

**启动条件**：用户进行上下文压缩后，我开始执行重构计划。
