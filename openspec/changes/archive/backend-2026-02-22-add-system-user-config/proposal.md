## Why

系统需要一个内置的系统用户（system user）用于执行内部操作，如添加新用户、系统级任务等。当前系统缺少这样的内置用户，导致需要手动创建或使用普通用户账号执行系统级操作，存在安全隐患和管理不便。

## What Changes

- 在 `application.yml` 中新增系统用户配置项，支持通过环境变量配置用户名和密码
- 提供默认用户名 `system` 和默认密码 `admin123`
- 创建启动时自动初始化系统用户的 Bean，应用启动时自动插入系统用户到数据库
- 使用学号 `000000000000`（12个0）作为系统用户的唯一标识
- 如果已存在该学号的用户，则跳过插入

## Capabilities

### New Capabilities

- `system-user-initialization`: 系统用户自动初始化功能，包括配置定义和启动时自动创建系统用户

### Modified Capabilities

无现有能力需要修改。

## Impact

- **配置文件**: `application.yml` 新增 `system-user` 配置节
- **新增类**:
  - `SystemUserProperties` - 配置属性类
  - `SystemUserInitializer` - 启动初始化 Bean
- **数据库**: `tb_user` 表新增系统用户记录（首次启动时）
- **依赖**: 使用 Spring Boot 的 `@ConfigurationProperties` 和 `@PostConstruct` 或 `CommandLineRunner`
