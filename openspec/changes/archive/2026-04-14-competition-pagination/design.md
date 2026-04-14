## Context

竞赛模块当前仅有 `GET /api/v1/competitions?limit=N` 接口，通过固定数量限制返回列表数据。项目中 Achievement 和 Member 模块已建立成熟的分页模式：

- Mapper 层使用 MyBatis-Plus 的 `IPage<T>` / `Page<T>` 进行 SQL 级分页
- Repository 实现层将 MyBatis-Plus 的 `IPage` 转换为 Spring Data 的 `Page<T>`
- Application Service 使用 `PageDTO.from()` 将 `Page<DTO>` 转为响应
- Controller 接收 `page` / `size` 参数

## Goals / Non-Goals

**Goals:**
- 新增独立分页接口，遵循现有项目分页模式
- 保持现有 `GET /api/v1/competitions` 接口不变
- 支持标准的分页参数（page、size）和分页元数据

**Non-Goals:**
- 不修改现有非分页接口
- 不增加筛选条件（如按级别、月份筛选），后续可扩展
- 不涉及数据库 schema 变更

## Decisions

### 1. 使用 MyBatis-Plus IPage 分页（与 Achievement 模块一致）

**选择**：Mapper 方法接收 `Page<CompetitionVO>` 参数，返回 `IPage<CompetitionVO>`

**理由**：项目中 Achievement 模块已验证此方案可行。MyBatis-Plus 分页插件自动拼接 `LIMIT/OFFSET`，无需手动编写分页 SQL。

**替代方案**：手动在 XML 中编写 `LIMIT #{offset}, #{size}` + COUNT 查询 — 增加维护成本，与项目风格不一致。

### 2. 接口路径 `GET /api/v1/competitions/page`

**选择**：在现有资源路径下添加 `/page` 子路径

**理由**：RESTful 风格，区分分页和非分页查询。保留现有接口的兼容性。

### 3. CompetitionConverter 新增 `convertToDTOPage` 方法

**选择**：使用 Spring Data 的 `Page.map()` 方法在 Service 层完成 `Page<CompetitionVO>` → `Page<CompetitionResponseDTO>` 转换

**理由**：与 AchievementConverter 的模式一致，保持 Converter 职责单一。

### 4. 分页参数约束

**选择**：`page` 默认 0（首页），`size` 默认 10，最大 50。超出范围时 clamp 而非报错。

**理由**：与项目 Member/Achievement 模块行为一致，对客户端友好。

## Risks / Trade-offs

- **[Mapper XML 复用]** → 复用现有 `CompetitionVOResultMap`，新增分页 SQL 时直接复用，不重复定义字段映射
- **[接口路径冲突]** → `/page` 作为固定子路径不会与 `/{id}` 冲突，Spring MVC 按精确匹配优先
