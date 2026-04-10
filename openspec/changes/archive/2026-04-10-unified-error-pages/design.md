## Context

当前前端没有任何自定义错误页面。Next.js 默认 404 页面与项目暗色主题不协调，`/admin/*` 路由无前端权限拦截（所有用户都能看到后台布局）。项目使用 Ant Design 6 暗色主题 + Next.js 15 App Router + Zustand 状态管理。

前端路由结构：
- `(public)` 路由组：PublicNavBar 布局
- `admin` 路由组：AdminHeadBar + AdminSideBar 布局
- 根 `layout.tsx`：只有 Providers（AntdRegistry、ThemeProvider、AuthProvider、App）

角色等级（已有 `getRoleLevel` 工具）：SUPER_ADMIN=3, DIRECTION_ADMIN=2, MEMBER=1, CANDIDATE=0, 未知=-1。

## Goals / Non-Goals

**Goals:**
- 统一的错误页面视觉风格：居中大图标 + 错误码 + 描述
- 404 页面覆盖所有不存在的路由，带 PublicNavBar
- 403 拦截 roleLevel < 1 的用户访问 admin 页面
- ErrorPageConfig 类型可扩展，便于未来添加更多错误类型

**Non-Goals:**
- 不做服务端权限检查（JWT 不含角色信息，middleware 无法判断）
- 不做 500/通用错误页面（当前只做 404 和 403）
- 不修改后端代码

## Decisions

### 1. 404 页面放在根级 `app/not-found.tsx`

**选择**：根级 `not-found.tsx` + 手动引入 PublicNavBar
**替代方案**：放在 `(public)/` 下 — 会导致完全不存在于路由树的 URL 走默认页面
**理由**：根级 `not-found.tsx` 覆盖所有 404 场景（路由不匹配 + `notFound()` 调用）。root layout 只有 Providers 无 NavBar，所以需要在组件内手动引入。

### 2. 403 在 admin layout 内直接渲染（方案 E）

**选择**：admin layout 检查 roleLevel，不够时渲染 PublicNavBar + ErrorPage(403)
**替代方案**：
- Middleware + API 调用查角色 — 复杂度高，每个请求多一次查询
- Redirect 到 `/forbidden` — 跳转丢失原始 URL 上下文
**理由**：admin layout 已经是 Client Component（依赖 Zustand store），直接读取 `roleName` 用 `getRoleLevel` 判断最简单。不跳转保持 URL 不变，用户知道被拦截的位置。

### 3. ErrorPageConfig 类型设计

```typescript
type ErrorPageConfig = {
  icon: ReactNode       // 大图标
  statusCode: number    // 错误码
  description: string   // 描述文案
}
```

三个字段足够覆盖当前需求。配置集中管理在 `configs.ts` 中，通过 `statusCode` 作为 key 查找。

### 4. ErrorPage 组件为 Client Component

**选择**：`'use client'`
**理由**：admin layout 是 Client Component，`not-found.tsx` 在 Next.js 中也需要作为组件使用。使用 Ant Design 的 Flex 等布局组件也需要客户端渲染。

## Risks / Trade-offs

- **[Admin 布局闪烁]** → admin layout 先挂载后检查角色，可能出现极短暂的管理后台结构闪现。可通过 loading 状态或 Suspense 缓解，但当前影响极小（Zustand store 是同步读取）。
- **[403 绕过]** → 纯客户端检查可被开发者工具绕过，但后端有 `@RequiresPermission` 注解保护 API，前端拦截仅为 UX 优化。
