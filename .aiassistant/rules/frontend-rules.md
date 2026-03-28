---
apply: 按文件模式
模式: src/frontend/**.ts
---

# 前端开发规范

技术栈：Next.js 15 + React 19 + TypeScript + Ant Design 6 + Zustand

## 项目结构

```
src/frontend/src/
├── app/           # Next.js App Router
├── components/    # 可复用组件
├── hooks/         # 自定义 Hooks
├── stores/        # Zustand 状态
├── services/      # API 服务
├── types/         # TS 类型
└── utils/         # 工具函数
```

## 认证对接

- 所有请求 `withCredentials: true` 或 `credentials: 'include'`
- 状态修改请求 Header 携带 `X-CSRF-Token`

## 命名规范

- 组件：PascalCase（`UserProfile.tsx`）
- Hooks：`use` 前缀（`useAuth.ts`）
- 类型：PascalCase（`User.ts`）

## 代码规范

- 使用 TypeScript 严格模式
- 函数式组件 + Hooks
- 提交前运行 `pnpm lint`
