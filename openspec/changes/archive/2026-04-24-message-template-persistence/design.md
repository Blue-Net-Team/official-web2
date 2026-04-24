## Context

`MessageTemplateRegistry` 维护系统中所有消息模板的元数据、内容覆盖和启禁用状态。当前版本为纯内存实现，通过 `LinkedHashMap` 和 `HashSet` 在运行时缓存覆盖数据。管理后台已提供完整的模板编辑、启禁用切换和预览功能，但编辑结果在应用重启后全部恢复为代码硬编码的默认值。

数据库表 `tb_message_template`（`code`, `name`, `subject`, `content`, `description`, `enabled`）及对应 MyBatis-Plus 的 `MessageTemplateDO` / `MessageTemplateMapper` 已存在于代码库中，但从未被业务代码引用。

## Goals / Non-Goals

**Goals:**
- 模板内容编辑后持久化到数据库，重启自动恢复
- 启禁用状态编辑后持久化到数据库，重启自动恢复
- `subject` 编辑后同样持久化（前端已支持，后端当前忽略）
- 读取时优先使用数据库覆盖值，无覆盖则回退到代码默认值
- 全量现有测试继续通过

**Non-Goals:**
- 不改数据库表结构（现有字段已满足需求）
- 不改前端 UI 或 API 接口
- 不引入 Redis 等额外缓存层（单表数据量极小）
- 不将模板元数据（变量列表、默认 HTML、描述）持久化到数据库（这些是代码契约，不应由用户修改）

## Decisions

**1. 内存元数据 + 数据库覆盖层（混合架构）**

- **Rationale**: 模板元数据（变量列表、默认 HTML）是代码级别的渲染契约，不应暴露给用户修改。如果全部存入数据库，新增模板需要 migration，且变量列表无法以结构化形式存储（当前表无此字段）。保留内存注册表 + 数据库覆盖层，只需持久化运行时可能被修改的字段（content、subject、enabled），改动最小且语义清晰。
- **Alternative considered**: 将所有模板数据完整存入数据库。Rejected，因为会增加表结构复杂度且没有必要。

**2. 同步写库**

- **Rationale**: 模板编辑是低频管理操作，并发极低。同步写库保证强一致性，无需引入异步队列或分布式事务的复杂度。
- **Alternative considered**: 异步写库或事件驱动。Rejected，过度设计。

**3. 构造方法中调用 `init()` + `@PostConstruct` 防护**

- **Rationale**: `MessageTemplateRegistry` 需要 `MessageTemplateMapper` 注入。在构造方法中调用 `init()` 可以支持测试中的 `new MessageTemplateRegistry(mockMapper)`；`@PostConstruct` 的 `templates.isEmpty()` 检查防止 Spring 容器启动时的重复初始化。

## Risks / Trade-offs

- **[Risk] 多实例部署时的短暂不一致** → `Mitigation`: 模板编辑频率极低，且各实例在启动时都会从同一份数据库加载，短暂不一致窗口可接受。如需强一致可引入分布式缓存，但当前数据量下过度设计。
- **[Risk] 单元测试直接 `new MessageTemplateRegistry()` 失效** → `Mitigation`: 现有测试（`EmailVerificationCodeTemplateTest`、`EnrollmentRejectionTemplateTest`）需要改为注入 mock `MessageTemplateMapper`。
