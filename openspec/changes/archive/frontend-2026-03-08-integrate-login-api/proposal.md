## Why

当前登录页面仅实现了 UI 交互，尚未对接后端认证接口。用户无法通过学号密码完成实际登录操作，导致系统缺少核心的身份认证功能。需要将前端登录表单与后端 `/api/v1/auth/login/student-id` 接口集成，实现完整的用户认证流程。

## What Changes

- 实现登录表单与后端 API 的对接
- 添加密码前端 SHA-256 哈希处理，增强安全性
- 实现 JWT Token 的本地存储与管理
- 添加登录状态的全局管理（使用 Zustand）
- 实现登录成功后的页面跳转逻辑
- 添加登录失败的错误提示处理
- 实现登录页面的加载状态展示

## Capabilities

### New Capabilities

- `login-integration`: 登录功能集成，包括表单提交、密码哈希、API 调用、Token 存储、状态管理

### Modified Capabilities

- `auth-api-service`: 扩展现有认证 API 服务规范，增加密码哈希处理说明和错误处理规范

## Impact

- **前端代码**:
  - `src/app/(public)/(other)/login/page.tsx` - 登录页面组件
  - `src/apis/services/auth.service.ts` - 认证服务（已存在，需确认兼容）
  - `src/apis/schema/type.ts` - 类型定义（已存在，需确认兼容）
  - 新增 `src/stores/authStore.ts` - 认证状态管理
  - 新增 `src/utils/passwordHash.ts` - 密码哈希工具

- **依赖关系**:
  - 需要安装 `zustand` 用于状态管理
  - 使用浏览器原生 Web Crypto API 进行 SHA-256 哈希

- **后端接口**:
  - `POST /api/v1/auth/login/student-id` - 学号登录接口
