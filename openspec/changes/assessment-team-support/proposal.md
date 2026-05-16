## Why

当前考核系统仅支持个人答题，但团队项目（如智能小车）需要计算机视觉、电控、机械结构三个方向协作完成。缺乏组队机制导致跨方向考核无法有效组织，不同方向考生无法共享同一份作品提交和评分。

## What Changes

- **新增考核组队能力**：考核时间支持开启"允许组队"，仅限 FILE_UPLOAD 类型题目可组队作答。
- **新增队伍生命周期管理**：创建队伍、邀请码加入、退出/转让/解散队伍，队伍绑定考核轮次，考核结束后自动解散。
- **新增邀请码预览确认流程**：输入邀请码后先展示队伍信息（名称、成员列表），用户确认后才正式加入。
- **跨方向考核共享**：`direction` 为 `null` 的考核时间对所有方向可见，支持跨方向组队。
- **队长唯一提交权限**：组队题仅队长可上传作品，队员可查看但不可修改。
- **独立评分与录用**：同一份作品，评委对每个队员独立评分、独立决定录用/淘汰。
- **修改考核时间查询逻辑**：用户端查询支持返回 `direction = null` 的跨方向考核。
- **修改答题校验逻辑**：FILE_UPLOAD 题在允许组队的考核中，校验队长权限后才可提交/更新。

## Capabilities

### New Capabilities
- `assessment-team-support`: 考核组队全生命周期管理，包括创建队伍、邀请码加入、预览确认、退出/转让/解散、队长提交权限控制。

### Modified Capabilities
- `assessment-time-management`: 新增 `allow_team` 字段；创建/编辑考核时支持设置是否允许组队；查询逻辑支持 `direction = null`。
- `frontend-assessment-question-page`: FILE_UPLOAD 题在组队考核中增加组队前置流程；题目页根据队长/队员角色展示不同操作区。
- `assessment-judgement`: 评分和评论按 `user_id` 独立记录，同一队伍成员共享同一份 `answer_id` 但各自有独立的 `judgement` 记录。

## Impact

- **后端**：新增 `tb_assessment_team`、`tb_assessment_team_member` 表；修改 `tb_assessment_time`（加 `allow_team`）、`tb_assessment_answer`（加 `team_id`）；新增 `AssessmentTeamController`、`AssessmentTeamAppService`；修改 `AssessmentTimeAppServiceImpl`（查询逻辑）、`AssessmentAnswerAppServiceImpl`（提交校验）。
- **前端**：考核时间管理页面增加"允许组队"开关；考题目录页增加组队状态判断；FILE_UPLOAD 题页面增加队伍信息面板、邀请码输入/展示、队长上传区/队员只读区。
- **数据库**：新增两张表，修改两张表，需编写 Flyway 迁移脚本。
- **接口复用**：队伍成员信息复用现有的 `UserInfo` DTO 结构，避免重复定义用户字段。
