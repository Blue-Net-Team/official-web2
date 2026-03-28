## Why

前端需要与后端认证接口对接，实现用户登录和退出功能。当前 `src/apis` 目录已有基础的 axios 客户端配置，但缺少：
1. 环境变量配置的统一管理（API 地址拼接）
2. 认证相关的 TypeScript 类型定义
3. 认证相关的 API 服务函数

## What Changes

- 新增 `src/apis/config.ts` - 统一管理环境变量，拼接 API 基础地址
- 完善 `src/apis/schema/enumerate.ts` - 添加用户角色、方向、性别等枚举类型
- 完善 `src/apis/schema/type.ts` - 添加通用响应类型和认证相关类型定义
- 新增 `src/apis/services/auth.service.ts` - 认证服务函数（登录、退出）
- 修改 `src/apis/client.ts` - 使用统一的配置模块

## Capabilities

### New Capabilities

- `auth-api-service`: 认证 API 服务，包含学号登录和用户退出两个接口调用函数，与后端 AuthController 接口对应

### Modified Capabilities

无

## Impact

- 新增文件：`src/apis/config.ts`、`src/apis/services/auth.service.ts`
- 修改文件：`src/apis/client.ts`、`src/apis/schema/enumerate.ts`、`src/apis/schema/type.ts`
- 依赖后端接口：`POST /api/v1/auth/login/student-id`（公开）、`POST /api/v1/auth/logout`（需认证）
