# 权限扫描器规范

## 概述

定义启动时自动扫描 `@Permission` 注解并同步到数据库的扫描器组件。

## 功能需求

### FR-SCAN-001: 扫描范围

扫描器必须扫描以下范围内的类：

- 所有标注 `@RestController` 或 `@Controller` 的类
- 类中所有标注 `@RequestMapping` 及其派生注解（`@GetMapping`, `@PostMapping` 等）的方法
- 提取方法的 URL 路径和 HTTP 方法

### FR-SCAN-002: 权限信息提取

对于每个扫描到的方法，提取以下信息：

| 字段 | 来源 | 说明 |
|------|------|------|
| `value` | `@Permission.value` | 权限唯一标识 |
| `name` | `@Permission.name` | 权限名称（可为空） |
| `url` | `RequestMappingInfo` | 完整 URL 路径 |
| `method` | `RequestMappingInfo` | HTTP 方法（GET/POST/PUT/DELETE 等） |

**URL 处理规则**：
- 拼接类级别和方法级别的路径
- 每个方法仅处理第一个 URL 模式（不支持多路径）
- 保留路径变量（如 `/users/{id}`）

### FR-SCAN-003: 数据库同步策略

扫描完成后，必须执行以下同步操作：

#### 新增权限

若数据库中不存在该 `value`，插入新记录：
- 状态：`active`
- 创建时间：当前时间
- 更新时间：当前时间

#### 更新权限

若数据库中已存在该 `value`，更新以下字段：
- `name`：更新为注解中的新名称
- `url`：更新为新 URL
- `method`：更新为新 HTTP 方法
- `updated_at`：当前时间

#### 删除幽灵数据

扫描结束后，对于数据库中存在但本次扫描未发现的权限：
- 物理删除该权限记录
- 级联删除 `role_permission` 关联表中的相关记录
- 记录删除日志（权限值、删除时间）

### FR-SCAN-004: 批量处理

扫描器必须使用批量操作优化性能：

- 一次性查询数据库中所有现有权限
- 在内存中对比差异（新增/更新/删除）
- 批量执行 INSERT 和 UPDATE（单条或批量 SQL）
- 批量执行 DELETE

**性能目标**：
- 100 个接口的扫描时间 < 500ms
- 数据库往返次数 < 5 次

### FR-SCAN-005: 启动时执行

扫描器必须在应用启动完成后执行：

- 实现 `InitializingBean` 接口
- 或使用 `@PostConstruct` 注解
- 或使用 `ApplicationRunner`

**执行顺序**：
- 必须在数据库连接池初始化之后
- 必须在其他业务组件初始化之前（避免权限未就绪）
- 建议顺序：`@Order(Ordered.HIGHEST_PRECEDENCE + 100)`

### FR-SCAN-006: 异常处理

扫描过程中的异常处理策略：

| 场景 | 行为 |
|------|------|
| 数据库连接失败 | 抛出异常，阻止应用启动，记录错误日志 |
| 权限格式校验失败 | 抛出异常，阻止应用启动，显示具体失败的权限值 |
| URL 解析失败 | 记录警告日志，跳过该接口，继续扫描其他 |
| 批量保存失败 | 抛出异常，阻止应用启动，回滚所有变更 |

### FR-SCAN-007: 扫描日志

扫描器必须记录详细的扫描日志：

```
[INFO] Starting permission scan...
[INFO] Found 45 controller methods with @Permission
[INFO] Database has 40 existing permissions
[INFO] New permissions: 5 (user:create, evaluation:submit, ...)
[INFO] Updated permissions: 3 (user:update, ...)
[INFO] Deleted permissions: 2 (old:permission, deprecated:action)
[INFO] Permission scan completed in 120ms
```

## 非功能需求

### NFR-SCAN-001: 性能要求

- 扫描 100 个接口时间 < 500ms
- 内存占用 < 50MB
- 支持并发启动（多实例同时扫描不会冲突）

### NFR-SCAN-002: 可观测性

- 提供 Micrometer 指标（可选）：`permission.scan.duration`, `permission.scan.total`
- 支持通过 Actuator 端点查看权限列表（可选）

## 数据库设计

### Permission 表

```sql
CREATE TABLE tb_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    value VARCHAR(100) NOT NULL UNIQUE COMMENT '权限唯一标识',
    name VARCHAR(200) COMMENT '权限名称',
    url VARCHAR(500) NOT NULL COMMENT 'URL路径',
    method VARCHAR(20) NOT NULL COMMENT 'HTTP方法',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记（扫描器物理删除不使用此字段）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_value (value),
    INDEX idx_url_method (url, method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';
```

**注意**：扫描器使用物理删除，不使用 `deleted` 字段。

## 错误处理

| 场景 | 行为 |
|------|------|
| 数据库连接超时 | 重试 3 次，仍失败则阻止启动 |
| 权限重复（value 相同但 url 不同） | 允许存在，视为同一权限的不同端点 |
| 扫描过程中应用关闭 | 优雅退出，记录中断状态 |

## 示例

### 扫描前数据库状态

```
id | value        | name       | url              | method
---|--------------|------------|------------------|--------
1  | user:create  | 创建用户    | /users           | POST
2  | user:delete  | 删除用户    | /users/{id}      | DELETE
3  | old:api      | 旧接口      | /old/api         | GET    ← 代码中已删除
```

### 代码中的权限

```java
@Permission(value="user:create", name="创建用户")
@PostMapping("/users")
public Result create() {}

@Permission(value="user:update", name="更新用户")  // 新增
@PutMapping("/users/{id}")
public Result update() {}

// user:delete 已从代码中删除
```

### 扫描后数据库状态

```
id | value        | name       | url              | method
---|--------------|------------|------------------|--------
1  | user:create  | 创建用户    | /users           | POST
4  | user:update  | 更新用户    | /users/{id}      | PUT

// user:delete (id=2) 和 old:api (id=3) 已被物理删除
// role_permission 表中关联记录也一并删除
```
