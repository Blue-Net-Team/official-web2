# AOP 权限拦截器规范

## 概述

定义基于 Spring AOP 的权限校验拦截器，实现对 `@Permission` 注解的访问控制。

## 功能需求

### FR-AOP-001: 拦截范围

拦截器必须拦截所有标注 `@Permission` 注解的方法：

- 拦截 `@RestController` 和 `@Controller` 类中的方法
- 支持类级别和方法级别的 `@Permission`
- 方法级别注解优先级高于类级别

### FR-AOP-002: 访问级别处理

根据 `@Permission.access` 的值执行不同的校验逻辑：

#### PUBLIC 级别

- 直接放行，不执行任何校验
- 适用于登录、注册等公开接口

```java
@Permission(value="auth:login", access=AccessLevel.PUBLIC)
@PostMapping("/login")
public Result login() {}
```

#### AUTHENTICATED 级别

- 检查请求是否包含有效的 JWT Token
- Token 无效或过期：返回 401 Unauthorized
- Token 有效：放行

```java
@Permission(value="profile:view", access=AccessLevel.AUTHENTICATED)
@GetMapping("/profile")
public Result getProfile() {}
```

#### PROTECTED 级别（默认）

- 检查用户是否已登录（Token 有效）
- 检查用户角色是否拥有该权限
- 权限判定逻辑：
  1. 查询当前用户的角色 ID
  2. 查询该角色的所有权限值列表
  3. 检查请求的权限值是否在列表中
  4. 若是：放行；若否：返回 403 Forbidden

```java
@Permission(value="user:create")
@PostMapping("/users")
public Result createUser() {}
```

### FR-AOP-003: 孤儿权限处理

对于数据库中存在但没有任何角色关联的权限（孤儿权限）：

- 视为公开访问，任何已登录用户都可访问
- 记录警告日志，提示管理员为该权限分配角色

### FR-AOP-004: 无注解接口处理

对于没有 `@Permission` 注解的接口：

**方案 A（默认公开）**：
- 允许所有访问（包括未登录用户）

**方案 B（默认拒绝）**：
- 返回 403 Forbidden
- 登录相关接口（`/login`, `/register`, `/oauth/*` 等）除外

**推荐采用方案 A**，通过代码审查确保所有敏感接口都添加注解。

### FR-AOP-005: 权限数据直查

认证阶段必须从持久层直接读取权限数据，不得依赖应用启动阶段预加载的内存缓存：

- 认证时按角色查询其绑定的权限值集合，写入 `SecurityPrincipal` 并放入请求上下文
- 权限判定阶段直接读取 `SecurityPrincipal` 携带的权限集，不得再次查询持久层
- 每请求的权限查询次数固定为一次，与接口内的权限检查次数无关

原「启动时预加载到内存 + 刷新策略」的规定已废止。原因：预加载的内容取决于 Bean 初始化顺序，而权限数据的写入方（权限扫描器）在加载之后才执行，导致缓存可能为空；且原实现的读取无持久层回退，空的缓存会使全部权限校验失效。

若将来需要重新引入缓存，必须同时满足：具备失效机制（而非仅在启动时加载一次）、具备明确的加载顺序保证、且读取路径保留持久层回退。

### FR-AOP-006: 用户信息获取

拦截器必须从请求上下文中获取当前用户信息：

- 从 HTTP Header 中提取 `Authorization: Bearer <token>`
- 解析 JWT Token 获取用户 ID 和角色 ID
- 将用户信息存入 ThreadLocal，供业务代码使用

```java
// 工具类
public class SecurityContext {
    private static final ThreadLocal<UserInfo> context = new ThreadLocal<>();

    public static UserInfo getCurrentUser() {
        return context.get();
    }

    public static boolean hasPermission(String value) {
        // 检查当前用户是否有指定权限
    }
}
```

### FR-AOP-007: 异常处理

拦截器必须处理以下异常情况：

| 场景 | HTTP 状态码 | 响应体 |
|------|------------|--------|
| Token 缺失 | 401 | `{ "code": 401, "message": "Token is required" }` |
| Token 无效 | 401 | `{ "code": 401, "message": "Invalid token" }` |
| Token 过期 | 401 | `{ "code": 401, "message": "Token expired" }` |
| 无权限访问 | 403 | `{ "code": 403, "message": "Access denied" }` |
| 权限未配置 | 403 | `{ "code": 403, "message": "Permission not configured" }` |

## 非功能需求

### NFR-AOP-001: 性能要求

- 权限校验耗时 < 5ms（99 分位）
- 内存缓存命中率 > 99%
- 不支持分布式缓存（单机部署）

### NFR-AOP-002: 线程安全

- ThreadLocal 使用必须正确清理（`try-finally` 或 `@After`）
- 权限集随 `SecurityPrincipal` 按请求独立构造，为不可变集合，不存在跨请求共享的可变状态

### NFR-AOP-003: 日志记录

- 拒绝访问时必须记录日志（用户 ID、IP、请求的权限、时间）
- 可用于安全审计

```
[WARN] Access denied: userId=123, ip=192.168.1.100, permission=user:delete, uri=/users/456
```

## 错误处理

| 场景 | 行为 |
|------|------|
| 权限查询异常或无绑定 | 视为无任何权限，拒绝访问；异常额外记录 error 级别日志（SUPER_ADMIN 的角色级别绕过除外） |
| 用户角色为空 | 视为无任何权限，拒绝访问 |
| 权限值为空 | 记录错误，拒绝访问 |
| ThreadLocal 未清理 | 可能导致内存泄漏，必须确保清理 |

## 示例

### 拦截器伪代码

```java
@Aspect
@Component
public class PermissionAspect {

    // 权限集由认证阶段（JwtAuthenticationFilter）从持久层读取后写入 SecurityPrincipal，
    // 本切面不再自行查询或缓存权限数据。

    @Around("@annotation(permission)")
    public Object checkPermission(ProceedingJoinPoint pjp, Permission permission) throws Throwable {
        // 1. 获取当前请求
        HttpServletRequest request = ((ServletRequestAttributes)
            RequestContextHolder.getRequestAttributes()).getRequest();

        // 2. 解析 Token 获取用户信息
        String token = extractToken(request);
        UserInfo user = JwtUtil.parseToken(token);

        // 3. 根据访问级别处理
        switch (permission.access()) {
            case PUBLIC:
                return pjp.proceed();

            case AUTHENTICATED:
                if (user == null) {
                    throw new UnauthorizedException("Token required");
                }
                return pjp.proceed();

            case PROTECTED:
                if (user == null) {
                    throw new UnauthorizedException("Token required");
                }
                if (!hasPermission(permission.value())) {
                    log.warn("Access denied: userId={}, permission={}",
                        user.getId(), permission.value());
                    throw new ForbiddenException("Access denied");
                }
                return pjp.proceed();
        }

        return pjp.proceed();
    }

    private boolean hasPermission(Long roleId, String permissionValue) {
        // 直接读 SecurityPrincipal 携带的权限集，不查询持久层、不读缓存
        return UserCTX.getPrincipal().hasPermission(permissionValue);
    }
}
```

### 异常处理器

```java
@RestControllerAdvice
public class PermissionExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401)
            .body(Result.fail(401, e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(403)
            .body(Result.fail(403, e.getMessage()));
    }
}
```
