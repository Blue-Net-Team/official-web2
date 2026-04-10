## 1. 类型定义与配置

- [x] 1.1 创建 `src/components/ErrorPage/configs.ts`，定义 `ErrorPageConfig` 类型（icon、statusCode、description）和 `ERROR_CONFIGS` 配置集合（403、404）

## 2. 共享组件

- [x] 2.1 创建 `src/components/ErrorPage/index.tsx`，实现 `ErrorPage` 组件（Client Component，居中布局：图标 + 错误码 + 描述）

## 3. 404 页面

- [x] 3.1 创建 `src/app/not-found.tsx`，引入 PublicNavBar + ErrorPage(404)，替换 Next.js 默认 404

## 4. 403 权限拦截

- [x] 4.1 修改 `src/app/admin/layout.tsx`，使用 `getRoleLevel` + Zustand authStore 检查角色，roleLevel < 1 时渲染 PublicNavBar + ErrorPage(403)
