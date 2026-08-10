## Why

目前系统同时存在两条相似的数据链路：管理员维护的“团队成就”（`tb_achievement`）和用户自己维护的“竞赛经历”（`tb_user_experience`）。由于历史原因，`tb_user_achievement` 中间表已建好却未启用，导致成就无法关联到具体成员。为了统一官方荣誉展示、减少重复录入，并让外部访客在成员主页看到该成员参与/获得的成就，需要将“团队成就”作为权威来源，直接替换掉个人中心的“竞赛经历”。

## What Changes

- **移除竞赛经历**：删除 `tb_user_experience` 中 `type = 'COMPETITION'` 的数据，移除 `ExperienceType.COMPETITION` 枚举，下线前端所有“竞赛经历”Tab 和表单。
- **成就关联成员**：管理员创建/更新成就时，可通过 `Mentions` 选择系统内成员，并通过标签输入外部协作者姓名；后端分别写入 `tb_user_achievement` 和新建的 `tb_achievement_external_member`。
- **删除成就级联**：删除成就时同步清理成员关联与外部协作者记录。
- **公开成员成就查询**：新增 `GET /api/v1/members/{memberId}/achievements` 接口，供成员主页展示该用户的官方成就。
- **前端展示改造**：成员主页和个人中心将“竞赛经历”Tab 替换为“个人成就”Tab，只读展示由管理员维护的成就列表。
- **成就卡片展示增强**：成就列表/详情展示系统内成员（头像+昵称，可跳转主页）和外部协作者（纯文本标签）。

## Capabilities

### New Capabilities

- `achievement-member-association`：成就与系统内成员及外部协作者的关联管理。
- `member-achievements-public-view`：公开按成员查询成就并在成员主页展示。

### Modified Capabilities

- `achievement-management`：创建/更新/删除成就需同步维护成员关联与外部协作者；请求和返回字段扩展。
- `team-achievements`：成就列表/卡片需展示关联成员和外部协作者。
- `backend-user-experience`：移除 `COMPETITION` 类型及对应数据，仅保留 `PROJECT` 和 `INTERNSHIP`。
- `frontend-user-experience`：移除“竞赛经历”相关 Tab、表单和组件。
- `frontend-member-profile-view`：将“竞赛经历”Tab 替换为“个人成就”Tab，数据来源改为成就接口。
- `frontend-user-profile`：将个人中心“竞赛经历”Tab 替换为只读的“个人成就”Tab。

## Impact

- **后端**：`Achievement` 聚合、`AchievementRepositoryImpl`、`AchievementAppService`、管理端 Controller、公开成员 Controller；新增 `AchievementExternalMember` 实体与表；Flyway 迁移脚本清理旧数据。
- **前端**：`AchievementDrawer`、成就卡片、成员主页、个人中心、侧边栏统计、`ExperienceSection` 中竞赛相关逻辑。
- **数据**：`tb_user_experience` 中竞赛记录将被物理删除，不可恢复；`tb_user_achievement` 正式启用。
- **权限**：成就管理保持管理员权限；成员成就查询为公开权限。
