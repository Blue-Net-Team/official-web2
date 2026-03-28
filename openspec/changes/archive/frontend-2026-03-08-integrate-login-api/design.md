## Context

当前登录页面已实现 UI 组件，但尚未与后端 API 集成。后端提供学号登录接口 `POST /api/v1/auth/login/student-id`，返回 JWT Token 和用户信息。前端需要实现完整的认证流程，包括密码哈希、API 调用、Token 存储和状态管理。

**技术栈约束**:
- React 19 + Next.js 15
- Ant Design 6
- Zustand 5 (已安装)
- Axios (已配置拦截器)
- TypeScript

**后端接口约束**:
- 后端使用 BCrypt 验证密码
- 前端需先对密码进行 SHA-256 哈希，哈希值作为"原始密码"发送
- 返回 JWT Token 存储于 localStorage
- 响应格式: `ResponseMessage<UserAuthResponseDTO>`

## Goals / Non-Goals

**Goals:**
- 实现登录表单与后端 API 的完整对接
- 使用 SHA-256 对密码进行前端哈希处理
- 使用 Zustand 管理全局认证状态
- 实现 Token 的持久化存储与自动恢复
- 提供良好的用户体验（加载状态、错误提示）

**Non-Goals:**
- 不实现"记住我"功能（后续迭代）
- 不实现邮箱登录（后端接口暂未提供）
- 不实现 Token 自动刷新机制（后续迭代）

## Decisions

### 1. 密码哈希方案

**决策**: 使用 Web Crypto API 的 SHA-256 算法

**理由**:
- 浏览器原生支持，无需额外依赖
- SHA-256 是广泛使用的安全哈希算法
- 与后端 BCrypt 形成双重哈希保护

**实现**:
```typescript
async function hashPassword(password: string): Promise<string> {
  const encoder = new TextEncoder();
  const data = encoder.encode(password);
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}
```

### 2. 状态管理方案

**决策**: 使用 Zustand + persist 中间件

**理由**:
- 项目已安装 Zustand 5
- Zustand 轻量且 TypeScript 友好
- persist 中间件可自动持久化到 localStorage

**状态设计**:
```typescript
interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: StudentIdLoginRequestDTO) => Promise<void>;
  logout: () => Promise<void>;
  hydrate: () => void;
}
```

### 3. Token 存储方案

**决策**: 使用 localStorage 存储 Token

**理由**:
- 与现有 axios 拦截器实现一致
- 简单可靠，适合当前需求
- Zustand persist 中间件自动同步

### 4. 错误处理方案

**决策**: 使用 Ant Design message 组件展示错误

**理由**:
- 与项目 UI 风格一致
- 提供良好的用户反馈体验
- 支持多种状态（success, error, loading）

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| SHA-256 哈希可能被彩虹表攻击 | 后端 BCrypt 提供第二层保护；建议用户使用复杂密码 |
| Token 存储在 localStorage 可能受 XSS 攻击 | 后续可迁移到 HttpOnly Cookie |
| 页面刷新时状态恢复延迟 | Zustand persist 中间件自动处理；添加 hydration 状态 |
| 网络请求失败无响应 | axios 拦截器已处理超时；添加重试提示 |

## Migration Plan

1. 创建密码哈希工具函数
2. 创建 Zustand 认证 Store
3. 修改登录页面组件，集成 API 调用
4. 测试完整登录流程
5. 验证 Token 持久化和自动恢复

**回滚策略**: 登录页面修改为增量变更，可随时注释 API 调用代码回退到纯 UI 状态。
