---
apply: 按文件模式
模式: src/backend/src/main/java/**/*.java
---

# 后端 DDD 架构规范

技术栈：Spring Boot 3.5.10 + Java 21 + MyBatis-Plus 3.5.7 + PostgreSQL 15+

## 四层架构

| 层级 | 路径 | 职责 |
|------|------|------|
| 接口层 | `api/` | HTTP 处理、参数校验、响应返回 |
| 应用层 | `application/` | 用例编排、事务控制、VO/DTO 转换 |
| 领域层 | `domain/` | 核心业务逻辑，仅操作 VO |
| 基础设施层 | `infrastructure/` | 仓储实现、配置、安全 |

## 权限注解（必须）

```java
@RequiresPermission(value = "enrollment:approve", name = "审核报名", access = AccessLevel.PROTECTED)
```

访问级别：`PUBLIC`（公开）、`AUTHENTICATED`（需登录）、`PROTECTED`（需权限）

## 关键约束

- 所有接口返回 `ResponseMessage<T>`
- 不使用物理外键，应用层维护关系
- 表以 `tb_` 开头，含 `deleted` 软删除字段
- DTO/接口必须添加 `@Schema`、`@Operation` 注解

## 命名规范

- Controller: `XxxController`
- Service: `XxxService` / `XxxServiceImpl`
- DTO: `XxxRequestDTO` / `XxxDTO`
- VO: `XxxVO`
- Converter: `XxxConverter`
