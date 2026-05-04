## Context

### 当前状态

前端认证流程：
1. 用户登录 → 后端返回 JWT token
2. 前端将 `token` 存储到 `localStorage`
3. Zustand authStore 持久化存储 `token` 和 `userInfo`
4. axios 拦截器从 localStorage 读取 token，添加到 `Authorization` header

### 目标状态

1. 用户登录 → 后端设置 HttpOnly Cookie + 返回 `csrfToken`
2. 前端将 `csrfToken` 和 `userInfo` 存储到 Zustand（仅内存，不持久化）
3. 浏览器自动携带 Cookie（`withCredentials: true`）
4. 状态修改请求添加 `X-CSRF-Token` header

## Goals / Non-Goals

**Goals:**
- 移除 localStorage 存储敏感 token
- 实现基于 Cookie 的认证
- 实现 CSRF Token 保护
- 页面刷新后恢复登录状态

**Non-Goals:**
- 后端 Rate Limiting（单独变更）
- CAPTCHA 验证（后续按需添加）
- OAuth/第三方登录

## Decisions

### D1: CSRF Token 存储策略

**决定**: csrfToken 存储在 Zustand 内存中，不持久化

**理由**:
- 页面刷新后通过 `/auth/me` 重新获取
- 避免复杂的安全存储问题
- 简单可靠

### D2: 登录状态持久化

**决定**: userInfo 可选择性持久化到 localStorage（仅用于 UI 展示）

**理由**:
- 快速渲染顶部导航栏用户信息
- 真正的认证状态由 Cookie 决定
- 即使 localStorage 被篡改，后端会验证 Cookie

**替代方案**: 完全不持久化，每次刷新都调用 `/auth/me`
- ❌ 增加网络请求延迟
- ❌ 用户体验差（导航栏闪烁）

### D3: axios 配置

**决定**: 全局配置 `withCredentials: true`

```typescript
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true,  // 新增
  headers: {
    'Content-Type': 'application/json',
  },
});
```

**理由**:
- 确保所有请求都携带 Cookie
- 统一配置，避免遗漏

### D4: CSRF Token 注入策略

**决定**: 请求拦截器自动添加 `X-CSRF-Token` header（仅 POST/PUT/DELETE/PATCH）

```typescript
apiClient.interceptors.request.use((config) => {
  const method = config.method?.toUpperCase();
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method || '')) {
    const csrfToken = useAuthStore.getState().csrfToken;
    if (csrfToken) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
  }
  return config;
});
```

**理由**:
- 自动化，避免每次请求手动添加
- 仅对状态修改请求添加，GET 请求不需要

### D5: 应用初始化流程

**决定**: 在 RootLayout 中初始化认证状态

```typescript
// layout.tsx 或 _app.tsx
useEffect(() => {
  // 检查 localStorage 中是否有 userInfo（快速渲染）
  const storedUserInfo = localStorage.getItem('auth-user-info');
  if (storedUserInfo) {
    authStore.setUserInfo(JSON.parse(storedUserInfo));
  }

  // 验证 Cookie 有效性，获取最新 csrfToken
  authStore.checkAuthStatus().catch(() => {
    // Cookie 无效，清除本地状态
    authStore.clearAuth();
  });
}, []);
```

**理由**:
- 快速渲染 UI（使用缓存的 userInfo）
- 异步验证真正的认证状态
- 处理过期场景

## Risks / Trade-offs

### R1: CORS 配置
**风险**: `withCredentials: true` 要求服务器返回具体的 `Access-Control-Allow-Origin`，不能用 `*`
**缓解**: 后端已配置 `CORS_ALLOWED_ORIGINS` 环境变量

### R2: 跨域开发环境
**风险**: localhost:3000 和 localhost:8080 是跨域
**缓解**:
- 后端 CORS 配置允许 `http://localhost:3000`
- Cookie `SameSite=Lax` 允许跨端口

### R3: 前后端必须同步部署
**风险**: 前后端变更需要同步上线
**缓解**:
- 过渡期后端同时支持 Cookie 和 Header 认证
- 先部署后端，再部署前端

## Migration Plan

### Phase 1: 前端准备
1. 修改 axios 配置：添加 `withCredentials: true`
2. 添加 CSRF Token 请求拦截器
3. 修改 authStore：移除 token，添加 csrfToken

### Phase 2: API 服务改造
1. 新增 `authService.getAuthMe()` 方法
2. 修改 `authService.login()` 处理新响应格式
3. 修改 `authService.logout()` 添加 CSRF header

### Phase 3: 应用初始化
1. 在 RootLayout 添加认证状态检查
2. 处理页面刷新后的状态恢复

### Phase 4: 清理
1. 移除所有 localStorage token 相关代码
2. 移除 Authorization header 相关代码

### 部署顺序
1. 部署后端（Phase 1-2，支持两种认证方式）
2. 部署前端（Phase 1-3）
3. 验证功能正常
4. 后端执行清理（移除 Header 认证支持）
