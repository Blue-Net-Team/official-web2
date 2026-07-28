## Context

目前系统存在两条独立的“成就/经历”数据链路：

- **团队成就**（`tb_achievement`）：由管理员在 `/admin/achievement` 维护，面向 `/achievements` 公开页展示，目前无法关联到具体成员。
- **竞赛经历**（`tb_user_experience` 中 `type='COMPETITION'`）：由用户在个人中心自行维护，用于成员主页展示。

`tb_user_achievement` 中间表在初始化脚本中已存在，但主代码中没有任何写入路径，仅有用户删除时的级联清理和统计读取。本次变更旨在以“团队成就”作为唯一权威来源，替代掉用户自维护的“竞赛经历”，同时让成就可以关联到系统内成员和外部协作者。

## Goals / Non-Goals

**Goals：**
- 管理员创建/更新成就时，可以指定系统内成员（`tb_user_achievement`）和外部协作者（`tb_achievement_external_member`）。
- 删除成就时级联清理成员关联和外部协作者。
- 公开接口按用户 ID 查询其关联成就，供成员主页和个人中心展示。
- 移除 `tb_user_experience` 中的竞赛数据及前端所有竞赛经历入口。
- 成员主页和个人中心将“竞赛经历”Tab 替换为“个人成就”Tab，数据来自成就系统。

**Non-Goals：**
- 不将历史竞赛经历数据迁移到成就系统（项目初期，数据可丢）。
- 不支持用户自己申请、编辑或删除成就。
- 不支持外部协作者注册后自动关联到已有姓名记录。
- 不修改 `PermissionScanner` 逻辑，仅新增/复用权限字符串。
- 不将项目经历和实习经历纳入成就管理。

## Decisions

### 1. 外部协作者使用独立表，而非 JSON 列

**选择**：新建 `tb_achievement_external_member(id, achievement_id, name, display_order)`。

**理由**：
- 结构清晰，未来可扩展学校、角色等字段。
- 便于按成就查询、排序和级联删除。
- 避免 JSON 列在条件查询和类型安全上的劣势。

**替代方案**：`tb_achievement.external_members JSONB`。已被否决，不利于后续扩展和查询。

### 2. 系统内成员与外部协作者前端分两个输入框

**选择**：`Mentions` 仅用于系统内成员，`Select mode="tags"` 用于外部协作者。

**理由**：
- Mentions 的值是字符串，无法可靠区分“已选系统用户”和“手动输入的外部名字”。
- 分开输入使数据模型和校验逻辑更简单，避免正则解析昵称带来的重名、空格等问题。

### 3. 按用户查成就采用应用层拼装

**选择**：先查询用户关联的 `achievement_id` 列表，再批量查询成就详情和成员，在应用层组装 `List<AchievementDTO>`。

**理由**：
- 避免 SQL 中复杂 join 导致分页/去重问题。
- `tb_user_achievement` 数据量可控，批量查询不会成为瓶颈。
- 与现有 `AchievementRepositoryImpl` 中按名称匹配竞赛 Logo 的逻辑兼容。

### 4. 直接删除竞赛经历，不做并行保留

**选择**：通过 Flyway 迁移脚本 `DELETE FROM tb_user_experience WHERE type='COMPETITION'`，并移除枚举值和前端入口。

**理由**：
- 项目初期，无大规模历史数据。
- 避免新旧两条链路同时维护，减少技术债务。

### 5. 成就聚合根负责维护关联关系

**选择**：`tb_user_achievement` 的写入逻辑从 `UserRepositoryImpl` 转移到 `AchievementRepositoryImpl` / `AchievementAppService`。

**理由**：
- 关联是成就的属性（“这个奖有哪些人”），不是用户的属性。
- 删除成就时需要在成就 Repository 中完成级联清理。

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| 历史竞赛经历数据丢失后，用户个人页短期内容变少 | 管理员在变更后主动补录近期重要奖项 |
| 外部协作者姓名重复或拼写不统一 | 仅做展示，不建立唯一身份；未来如需可通过新增字段扩展 |
| 成就成员更新时全量替换，并发编辑可能丢失中间状态 | 管理员单人操作场景，采用先删后插；必要时在前端提示覆盖 |
| `ExperienceType.COMPETITION` 移除后相关测试/类型引用编译失败 | 变更范围内同步清理相关枚举校验、前端类型和测试 |
| 成员主页新增成就查询增加一次数据库往返 | 应用层批量查询，单次请求内完成；后续如成为瓶颈可加缓存 |

## Migration Plan

1. **数据库迁移**：新增 Flyway 脚本
   - 创建 `tb_achievement_external_member` 表。
   - 删除 `tb_user_experience` 中 `type='COMPETITION'` 的记录。
   - 为 `tb_user_achievement` 的 `achievement_id` 添加索引（如不存在）。
2. **后端部署**：
   - 更新 `Achievement` 聚合、Command、DTO、Repository、AppService、Controller。
   - 新增 `AchievementExternalMember` 实体、DO、Mapper、Repository 方法。
   - 新增公开接口 `GET /api/v1/members/{memberId}/achievements`。
   - 移除 `ExperienceType.COMPETITION` 及相关校验。
3. **前端部署**：
   - 更新 `AchievementDrawer` 增加成员输入。
   - 更新成就卡片展示成员。
   - 改造成员主页和个人中心 Tab，移除竞赛经历。
4. **回归验证**：
   - 管理员创建/编辑/删除成就（含成员和外部协作者）。
   - 成员主页展示个人成就。
   - 公开成就页正常展示。
   - 项目经历和实习经历不受影响。

## Open Questions

- 是否需要为 `GET /api/v1/members/{memberId}/achievements` 提供分页？初期成就数量较少，可先返回列表，后续按需改为分页。
- 个人中心 `/profile` 是否同步增加“个人成就”只读 Tab？当前方案建议增加，以保持一致性。
- 外部协作者是否需要限制长度、去重规则？建议单条不超过 100 字符，保存前 trim 并去重。
