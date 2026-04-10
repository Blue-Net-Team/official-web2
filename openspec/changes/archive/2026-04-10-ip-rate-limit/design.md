## Context

当前验证码发送接口的限频实现在应用层通过数据库查询完成（`findLatestByEmailWithinSeconds` / `findLatestByEmailAndSceneWithinSeconds`），每次发送验证码都需要查询 `tb_verify_code` 表。高并发攻击场景下，限频查询本身会成为数据库瓶颈。

同时 `tb_verify_code.ip_address` 列在整个写入链路上断裂：`VerifyCodeVO` 无 IP 字段、领域服务丢弃 IP 参数、`VerificationCodeRepositoryImpl.save()` 未映射 IP。该列从未被有效使用。

项目已有 Redis 基础设施（`StringRedisTemplate`），已用于 Token 管理、密码重置状态、OAuth 状态等场景。已有 AOP 模式（`@RequiresPermission` + `PermissionAspect`）。

## Goals / Non-Goals

**Goals:**
- 提供声明式的 IP 限频能力，通过注解即可为任意接口添加限频
- 限频逻辑基于 Redis，不依赖数据库查询
- 每个接口的限频独立计算（同一 IP 在接口 A 被限不影响接口 B）
- 清理 `ip_address` 相关的全部死代码
- 复用现有异常体系（`GlobalException` 子类）

**Non-Goals:**
- 不实现分布式令牌桶或滑动窗口（固定窗口 SETNX 已足够）
- 不实现多维度限频（如按邮箱限频），仅按 IP 维度
- 不实现注解级别的自定义 key（如 SpEL 表达式），仅固定按 IP + 请求路径
- 不审计 IP 记录（IP 审计在其他系统处理）

## Decisions

### 1. 限频算法：Redis SETNX + TTL（固定窗口）

**选择**：使用 `StringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(interval))`

**理由**：
- 原子操作，无需额外加锁
- 语义精确：key 不存在 → 放行并设置 TTL；key 存在 → 间隔未到，拒绝
- 无新增依赖，项目已有 `StringRedisTemplate`

**替代方案**：
- 滑动窗口（Sorted Set）：更精确但实现复杂，验证码场景不需要
- 令牌桶（Bucket4j）：需要额外依赖，过于重量级
- 数据库查询：当前方案，高并发下性能差

### 2. Redis Key 设计：`rate_limit:{ip}:{http_method}:{uri}`

**选择**：按 IP + HTTP 方法 + 请求路径组合作为 Key

**理由**：
- 每个接口独立计算，同一 IP 在不同接口互不影响
- 使用 `request.getRequestURI()` + `request.getMethod()` 作为标识，调试时直观可读
- 不同接口可配置不同 interval

**替代方案**：
- `rate_limit:{ip}` 全局限频：过于粗暴，一个接口被限会影响所有接口
- `rate_limit:{ip}:{class.method}`：调试时不直观，需要映射到 URL

### 3. 异常处理：复用 GlobalException 体系

**选择**：新增 `TooManyRequests extends GlobalException { code = 429 }`

**理由**：
- 项目已有 `BadRequest(400)`、`Unauthorized(401)`、`Forbidden(403)` 等模式
- `GlobalExceptionHandler` 通过 `ex.getCode()` 自动映射状态码，无需修改
- 429 是 HTTP 标准的限频状态码

### 4. IP 提取：支持反向代理

**选择**：新增 `IpUtils.getClientIp(HttpServletRequest)`，按优先级提取：
1. `X-Forwarded-For` 头的第一个 IP
2. `X-Real-IP` 头
3. `request.getRemoteAddr()`

**理由**：生产环境通常部署在 Nginx 后面，`getRemoteAddr()` 返回的是 Nginx 的 IP 而非客户端真实 IP。

### 5. 注解参数：仅 `interval` 一个参数

**选择**：`@RateLimit(interval = 60)` — 最小请求间隔（秒）

**理由**：用户需求明确，单一职责。未来如需扩展（自定义 key、按用户限频等），再加参数即可。

### 6. 删除 ip_address 列

**选择**：通过 Flyway 迁移删除 `tb_verify_code.ip_address` 列，同时清理所有相关代码。

**理由**：该列从未被正确使用（三层断裂），IP 审计不在本项目范围内。

## Risks / Trade-offs

- **[限频维度单一]** → 仅按 IP 限频，攻击者可通过大量代理 IP 绕过。这是可接受的权衡：验证码本身有 6 位随机码的暴力破解保护，且邮件服务通常也有发送频率限制。后续可扩展注解支持多维度。
- **[Redis 不可用]** → 如果 Redis 宕机，`setIfAbsent` 抛异常会导致所有限频接口不可用。切面应 catch Redis 异常并放行（降级策略），避免 Redis 故障导致业务中断。
- **[限频状态丢失]** → Redis 重启后限频状态清零。影响极小：最多让一个请求提前通过，不构成安全风险。
- **[X-Forwarded-For 伪造]** → 攻击者可伪造该头绕过限频。需确保可信代理（Nginx）会覆盖或追加该头。
