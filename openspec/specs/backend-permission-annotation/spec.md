# 权限注解规范

## 概述

定义权限控制注解 `@Permission`，用于标记 Controller 方法的访问权限级别。

## 功能需求

### FR-ANNOTATION-001: 注解定义

`@Permission` 注解必须包含以下属性：

| 属性 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `value` | String | 是 | 权限唯一标识，格式为 `resource:action` | `user:create` |
| `name` | String | 否 | 权限显示名称 | `创建用户` |
| `access` | AccessLevel | 否 | 访问级别，默认为 `PROTECTED` | `AccessLevel.PUBLIC` |

### FR-ANNOTATION-002: 权限标识格式

权限 `value` 必须符合以下规范：

- 格式：`^[a-z]+:[a-z]+$`
- 只能包含小写字母和冒号
- 冒号前后必须有内容
- 不允许空格、数字、大写字母

**有效示例**:
- `user:create`
- `evaluation:update`
- `file:download`

**无效示例**:
- `userCreate` (缺少冒号)
- `User:Create` (包含大写字母)
- `user:create:extra` (多个冒号)
- `user create` (包含空格)

### FR-ANNOTATION-003: 访问级别定义

`AccessLevel` 枚举必须包含以下级别：

| 级别 | 说明 | 校验逻辑 |
|------|------|----------|
| `PUBLIC` | 公开访问 | 无需任何校验，直接放行 |
| `AUTHENTICATED` | 登录用户 | 检查 JWT Token 是否有效 |
| `PROTECTED` | 受保护 | 检查用户角色是否拥有该权限 |

### FR-ANNOTATION-004: 注解位置

`@Permission` 注解可以标注在：

1. **方法级别**：标记单个接口的权限
   ```java
   @Permission(value="user:create", name="创建用户")
   @PostMapping("/users")
   public Result createUser() {}
   ```

2. **类级别**：为类中所有方法设置默认权限
   ```java
   @RestController
   @RequestMapping("/users")
   @Permission(value="user", name="用户管理")
   public class UserController {}
   ```

**优先级规则**：
- 方法级别注解优先级高于类级别
- 若方法有注解，使用方法的 `value` 和 `access`
- 若方法无注解但类有，使用类的配置
- 若都无任何注解，按全局策略处理（默认拒绝或公开）

### FR-ANNOTATION-005: 注解校验

应用启动时必须对 `@Permission` 注解进行校验：

- `value` 不能为空
- `value` 必须符合格式规范 `^[a-z]+:[a-z]+$`
- 若 `value` 格式错误，抛出 `IllegalArgumentException`，阻止应用启动

## 非功能需求

### NFR-ANNOTATION-001: 运行时保留

注解必须保留到运行时（`@Retention(RetentionPolicy.RUNTIME)`），以便 AOP 拦截器读取。

### NFR-ANNOTATION-002: 性能要求

注解解析必须在编译时完成，运行时仅读取预解析的元数据，不影响请求处理性能。

## 错误处理

| 场景 | 行为 |
|------|------|
| `value` 为空 | 启动时抛出异常，提示："Permission value cannot be empty" |
| `value` 格式错误 | 启动时抛出异常，提示："Permission value must match pattern [resource:action]" |
| 重复 `value` | 允许重复（不同 URL 可以共享同一权限标识） |

## 示例

### 公开接口

```java
@RestController
public class AuthController {

    @Permission(value="auth:login", access=AccessLevel.PUBLIC)
    @PostMapping("/login")
    public Result login() {}

    @Permission(value="auth:register", access=AccessLevel.PUBLIC)
    @PostMapping("/register")
    public Result register() {}
}
```

### 受保护接口

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Permission(value="user:view", name="查看用户")
    @GetMapping
    public Result list() {}

    @Permission(value="user:create", name="创建用户")
    @PostMapping
    public Result create() {}

    @Permission(value="user:update", name="更新用户")
    @PutMapping("/{id}")
    public Result update() {}

    @Permission(value="user:delete", name="删除用户")
    @DeleteMapping("/{id}")
    public Result delete() {}
}
```

### 登录即可访问

```java
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Permission(value="profile:view", access=AccessLevel.AUTHENTICATED)
    @GetMapping
    public Result getProfile() {}
}
```
