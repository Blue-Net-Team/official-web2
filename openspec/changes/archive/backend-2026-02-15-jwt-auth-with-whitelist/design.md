## Context

当前系统已有用户管理模块（user-management）和角色权限模块（rbac-role-management），但缺乏认证机制。AuthController 仅包含空框架，需要实现完整的 JWT 认证流程。

现有基础设施：
- User 实体包含 studentId、password、roleId 字段
- SecurityContext 使用 ThreadLocal 存储用户信息（待完善）
- Spring Security 已集成，但权限注解目前仅作标记用途
- Redis 尚未集成，需要添加依赖

## Goals / Non-Goals

**Goals:**
- 实现学号密码登录，签发 12 小时有效期的 JWT
- 实现 JWT 验证过滤器，保护受保护端点
- 实现登出功能，支持令牌吊销
- 提供 JWT 工具类供其他模块使用
- 确保所有认证相关代码都有单元测试覆盖

**Non-Goals:**
- 不支持 Refresh Token（使用白名单方案替代）
- 不支持多设备管理（单设备登录）
- 不实现第三方登录（仅学号密码）
- 不修改现有权限注解系统（独立实现认证层）

## Decisions

### 方案选择：JWT + Redis 白名单 vs JWT + Refresh Token

**选择：JWT + Redis 白名单**

理由：
- 实现简单，不需要维护两套令牌逻辑
- 12 小时有效期足够长，减少刷新需求
- 登出时删除 Redis Key 即可吊销，实现即时失效
- 内存占用低（1000 用户约 100KB）

替代方案：双令牌（Access + Refresh）
- 优势：更好的安全分层，Access Token 泄露影响有限
- 劣势：实现复杂，移动端需要额外处理刷新逻辑

### JWT 库选择：jjwt vs nimbus-jose

**选择：jjwt**

理由：
- Spring Security 官方推荐
- API 简洁，社区活跃
- 支持 JWT 所有标准声明

### Token 存储结构

```
Redis Key: auth:token:{jti}
Value: {userId} (Long 转 String)
TTL: 43200 秒 (12 小时)
```

每个登录用户只有一个有效令牌，新登录自动使旧令牌失效。

### SecurityContext 设计

使用 ThreadLocal 缓存当前请求的认证结果，避免重复解析 JWT。针对 JDK 21 虚拟线程环境做了特别考虑：

**核心原则：ThreadLocal 仅用于同步 HTTP 请求处理链**

```java
@Component
public class SecurityContext {
    private static final ThreadLocal<JwtPayload> currentAuth = new ThreadLocal<>();

    public static void setCurrentAuth(JwtPayload auth) {
        currentAuth.set(auth);
    }

    public static Long getCurrentUserId() {
        JwtPayload auth = currentAuth.get();
        return auth != null ? auth.getUserId() : null;
    }

    public static JwtPayload getCurrentAuth() {
        return currentAuth.get();
    }

    public static void clear() {
        currentAuth.remove();
    }
}
```

**线程安全边界说明：**

| 场景 | 使用 ThreadLocal？ | 说明 |
|------|-------------------|------|
| HTTP 请求处理 | ✅ 是 | 整个请求在同一线程（虚拟线程）中处理，ThreadLocal 有效 |
| @Async 方法 | ❌ 否 | 切换到不同线程，需显式传递参数 |
| @EventListener | ❌ 否 | 可能在不同线程执行，需显式传递参数 |
| @Scheduled | ❌ 否 | 无请求上下文，不适用 |

**异步场景的正确做法：**

```java
@Service
public class SomeService {
    @Async
    public void processAsync(Long userId, Data data) {
        // 显式接收 userId，不从 SecurityContext 取
        // 调用其他 Service 时继续传递 userId
    }
}
```

**关键实现点：**
- JWT Filter 在 finally 块中必须调用 `SecurityContext.clear()` 防止内存泄漏
- 异步操作必须通过方法参数显式传递用户上下文
- 事件对象应包含 userId 字段供监听器使用

### 登录响应 DTO 修改

UserAuthResponseDTO 移除 refreshToken 字段，仅保留：
- token: JWT 字符串
- userInfo: 用户信息

**BREAKING CHANGE**: 前端需要调整，不再接收 refreshToken。

## Risks / Trade-offs

**[风险] 每次请求都需要查询 Redis**
- 影响：增加 1-2ms 延迟，Redis 故障时所有请求失败
- 缓解：使用连接池，配置 Redis 哨兵模式保证高可用

**[风险] JWT 泄露后 12 小时内无法吊销**
- 影响：攻击者可在令牌过期前持续使用
- 缓解：提供"登出所有设备"功能（删除用户所有白名单记录）

**[风险] 单设备登录限制**
- 影响：用户在另一设备登录后，原设备会被踢出
- 缓解：这是设计选择，可在未来版本添加多设备支持

**[权衡] 性能 vs 安全性**
- 当前方案查 Redis 获取用户 ID，而非从 JWT 直接读取
- 优势：可即时吊销，支持服务端控制
- 劣势：比纯 JWT 慢（但差距可忽略）

**[风险] SecurityContext 在异步场景下失效**
- 影响：@Async 或 @EventListener 中调用 SecurityContext.getCurrentUserId() 返回 null
- 缓解：明确文档说明，强制异步方法通过参数传递 userId，代码审查时重点关注
- 检测：可添加断言在异步方法入口检查 SecurityContext，开发阶段发现问题

## Migration Plan

**部署步骤：**
1. 添加 jjwt 和 spring-data-redis 依赖
2. 配置 JWT 密钥（环境变量）
3. 启动 Redis 服务
4. 部署新版本代码
5. 验证登录流程

**回滚策略：**
- 如出现问题，回滚到上一版本
- Redis 数据无需迁移（纯缓存，可重建）

## Open Questions

1. JWT 密钥长度建议 256 位，生产环境如何安全存储？
2. 是否需要实现"记住我"功能（延长到 30 天）？
3. 移动端和 Web 端是否需要不同的 Token 有效期？
4. 是否需要提供工具类或 AOP 辅助在异步方法中传递上下文（如 @Async 时自动传播 SecurityContext）？
