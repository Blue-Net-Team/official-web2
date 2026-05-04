## Context

系统需要一个内置的系统用户用于执行内部操作。当前系统缺少这样的内置用户，需要通过配置文件定义并在应用启动时自动初始化。

**约束条件：**
- 用户表 `tb_user` 的唯一字段：`student_id`（13位，系统用户使用12个0）
- 必填字段：`student_id`（NOT NULL UNIQUE）
- 密码需要使用 BCrypt 加密存储
- 项目使用 DDD 四层架构，初始化逻辑应放在基础设施层

## Goals / Non-Goals

**Goals:**
- 在 `application.yml` 中添加系统用户配置，支持环境变量覆盖
- 创建配置属性类 `SystemUserProperties`
- 创建启动初始化 Bean，自动插入系统用户
- 如果已存在相同学号的用户则跳过插入

**Non-Goals:**
- 不使用 Flyway 迁移实现（明确要求）
- 不修改现有用户管理逻辑
- 不创建系统用户的特殊权限机制

## Decisions

### 1. 配置属性类设计

**决定：** 创建 `SystemUserProperties` 类，放在 `infrastructure/config/properties/` 目录下，与现有 `MinioProperties` 保持一致。

**配置结构：**
```yaml
system-user:
  username: ${SYSTEM_USER_USERNAME:system}
  password: ${SYSTEM_USER_PASSWORD:admin123}
  student-id: ${SYSTEM_USER_STUDENT_ID:000000000000}
```

**理由：** 遵循项目现有的配置模式，使用 `@ConfigurationProperties` 注解，支持环境变量覆盖。

### 2. 初始化 Bean 设计

**决定：** 创建 `SystemUserInitializer` 类，实现 `CommandLineRunner` 接口。

**理由：**
- `CommandLineRunner` 在所有 Bean 初始化完成后执行，确保 `UserRepository` 和 `PasswordEncoder` 已就绪
- 可以访问完整的 Spring 上下文
- 执行顺序可控（可通过 `@Order` 调整）

**替代方案：**
- `@PostConstruct`：执行时机太早，可能依赖的 Bean 未完全初始化
- `ApplicationRunner`：功能类似，选择 `CommandLineRunner` 更简洁

### 3. 用户数据插入策略

**决定：** 直接使用 `UserRepository` 的 `save` 方法和 `findByStudentId` 方法。

**流程：**
1. 启动时检查学号 `000000000000` 是否已存在
2. 如果存在，跳过插入，输出日志
3. 如果不存在，创建 User 实体并保存

**密码处理：** 使用注入的 `PasswordEncoder` 对配置的密码进行 BCrypt 加密。

### 4. 层级位置

**决定：** 初始化 Bean 放在 `infrastructure/init/` 目录下。

**理由：**
- 属于基础设施层的初始化逻辑
- 不涉及领域业务规则
- 与 `config` 目录平级，职责清晰

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 默认密码 `admin123` 安全性较低 | 文档说明生产环境必须通过环境变量修改 |
| 启动时数据库不可用导致初始化失败 | 捕获异常，输出错误日志，不影响应用启动 |
| 多实例部署时可能重复插入 | 使用学号唯一约束 + 先查询后插入策略 |
