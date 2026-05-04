## Why

当前系统缺乏完整的用户认证机制，登录接口仅有空框架而无实际实现。需要建立基于 JWT + Redis 白名单的认证体系，支持学号密码登录，实现安全的令牌签发、验证和吊销功能。

## What Changes

- **新增 JWT 工具类**：支持生成、解析和验证 JWT Token
- **新增 Redis 白名单服务**：存储有效 Token，支持登出吊销
- **实现学号登录接口**：验证学号密码，签发 JWT
- **新增 JWT 认证过滤器**：自动验证请求中的 Token
- **修改 UserAuthResponseDTO**：移除 refreshToken 字段（仅保留 token）
- **新增测试覆盖**：JWT 工具类和白名单服务的单元测试

## Capabilities

### New Capabilities
- `jwt-authentication`: JWT 认证核心功能，包括 Token 生成、验证、吊销
- `student-login`: 学号密码登录认证流程
- `auth-whitelist`: Redis 白名单管理，存储有效 Token 并支持登出吊销

### Modified Capabilities
- `user-management`: 补充登录接口 DTO 和响应结构

## Impact

- **API 变更**: `/api/v1/auth/login/student-id` 接口将返回 JWT token
- **新增依赖**: jjwt (JWT 库)、spring-data-redis (Redis 支持)
- **配置变更**: application.yml 需添加 JWT 密钥配置
- **数据库**: 无需变更，继续使用现有 tb_user 表
- **测试**: 需要添加 JWT 工具类和认证服务的单元测试
