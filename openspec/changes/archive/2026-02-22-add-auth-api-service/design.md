## Context

前端项目使用 Next.js + React + TypeScript + Axios，需要与后端 Spring Boot API 对接。当前 `src/apis` 目录已有基础的 axios 客户端配置，但存在以下问题：
1. `client.ts` 直接使用硬编码的 `process.env.NEXT_PUBLIC_API_BASE_URL`，未利用 `.env` 中的分离配置
2. 类型定义文件 `enumerate.ts` 和 `type.ts` 为空
3. 缺少认证相关的服务函数

后端 API 结构：
- 基础路径：`/api/v1`
- 认证接口：`POST /auth/login/student-id`（公开）、`POST /auth/logout`（需认证）
- 响应格式：`ResponseMessage<T>` 包含 `code`、`msg`、`data`

## Goals / Non-Goals

**Goals:**
- 统一管理 API 配置，从 `.env` 读取并拼接完整地址
- 提供与后端 DTO 一致的 TypeScript 类型定义
- 实现认证相关的 API 服务函数，明确标注是否需要认证

**Non-Goals:**
- 不实现登录页面的 UI 组件
- 不实现用户状态管理（userStore）的修改
- 不实现 token 持久化策略（由调用方决定）

## Decisions

### D1: 配置模块设计

**决策**: 创建独立的 `config.ts` 模块统一管理 API 配置

**理由**:
- 避免在代码中直接使用 `process.env`，便于测试和维护
- 集中处理地址拼接逻辑，支持 HTTP/HTTPS 切换
- 符合单一职责原则

**实现**:
```typescript
// src/apis/config.ts
const BACKEND_HOST = process.env.BACKEND_HOST || 'localhost';
const BACKEND_PORT = process.env.BACKEND_PORT || '8080';
const SSL_ENABLED = process.env.SSL_ENABLED === 'true';
const API_PREFIX = process.env.API_PREFIX || '/api/v1';

const protocol = SSL_ENABLED ? 'https' : 'http';
export const API_BASE_URL = `${protocol}://${BACKEND_HOST}:${BACKEND_PORT}${API_PREFIX}`;
```

### D2: 类型定义结构

**决策**: 将类型定义分为 `enumerate.ts`（枚举）和 `type.ts`（接口）

**理由**:
- 枚举类型和接口类型职责不同，分开管理更清晰
- 与后端 Java 的 enum 和 DTO 结构对应
- 便于后续扩展

### D3: 服务函数命名和注释规范

**决策**: 服务函数使用对象封装，每个函数必须注释是否需要认证

**理由**:
- 对象封装便于按模块导入（`import { authService } from '@/apis/services/auth.service'`）
- 明确的认证标注帮助开发者正确使用 `publicClient` 或 `apiClient`
- 与后端 Controller 方法对应，易于维护

**示例**:
```typescript
export const authService = {
  /**
   * 学号登录 - 公开接口，无需认证头
   */
  async login(...) { ... },
  
  /**
   * 用户登出 - 需要认证头
   */
  async logout(...) { ... },
};
```

### D4: 响应拦截器处理

**决策**: 保持现有拦截器逻辑，401 时清除 token 并跳转登录页

**理由**:
- 统一处理认证失败场景
- 避免在每个服务函数中重复处理错误

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 环境变量名与 Next.js 约定不一致（非 NEXT_PUBLIC_ 前缀） | Next.js 默认会将所有环境变量注入到 process.env，无需 NEXT_PUBLIC_ 前缀也可在服务端使用；若需客户端使用，需添加前缀或通过 next.config.ts 暴露 |
| 类型定义与后端不同步 | 建议后端使用 OpenAPI 生成前端类型，或手动保持同步 |

## File Structure

```
src/apis/
├── config.ts                    # API 配置（新增）
├── client.ts                    # Axios 客户端（修改）
├── schema/
│   ├── enumerate.ts             # 枚举类型（完善）
│   └── type.ts                  # 接口类型（完善）
└── services/
    └── auth.service.ts          # 认证服务（新增）
```
