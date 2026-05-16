## Context

当前考核系统中，考核时间按 `(direction, epoch, grade)` 组织，答案按 `user_id + question_id` 关联。所有题目均为个人作答，没有组队机制。

蓝网团队的最终考核涉及跨方向项目（如智能小车），需要 CV、电控、机械结构三个方向协作完成一个作品。现有系统无法支持：
- 不同方向考生参与同一个考核
- 多人协作提交同一份作品
- 同一份作品对不同队员独立评分

## Goals / Non-Goals

**Goals:**
- 考核时间支持配置"允许组队"，仅限 FILE_UPLOAD 题可组队
- 跨方向考核通过 `direction = null` 实现，所有方向考生可见
- 队伍绑定考核轮次，考核结束后自动解散
- 邀请码加入流程：输入邀请码 → 预览队伍信息 → 确认加入
- 仅队长可提交/更新组队题答案，队员可查看
- 评委对同一份作品给每个队员独立评分、独立做录用决策
- 接口复用现有 `UserInfo` DTO，避免重复定义用户字段

**Non-Goals:**
- 不实现邀请链接功能（通过 URL 参数自动加入），本次仅支持邀请码
- 不实现队伍实时通信（聊天、协作编辑）
- 不限制队伍人数上限（初期不设限，可后续增加）
- 不实现算法题/选择题的组队支持
- 不支持部分题目组队、部分题目个人作答的混合模式（一个考核要么全组队要么全个人）

## Decisions

### Decision: 队伍绑定考核轮次而非全局存在
**选择**：队伍数据模型包含 `assessment_time_id`，每次考核单独创建队伍。  
**理由**：
- 用户明确表示"队伍跟着考核轮次，考核结束后自动解散"
- 不同考核可以不同组队，上一轮被淘汰的人下一轮可以和别人组
- 避免全局队伍的权限管理和生命周期复杂度

**替代方案**：全局队伍 + 考核内报名。放弃原因：需要两层关系（全局队伍 + 考核实例），模型复杂度高，与需求不符。

### Decision: `direction = null` 表示跨方向共享
**选择**：考核时间的 `direction` 字段为 `null` 时，所有方向考生可见。  
**理由**：
- 复用现有字段语义，无需新增"是否跨方向"字段
- 与现有最终考核的语义一致
- `grade` 仍然有效，用于限制年级

**影响**：需要修改 `AssessmentTimeMapper.xml` 的 `selectPageByUserParticipation`，增加 `OR (t.direction IS NULL AND t.grade = #{enrollmentYear})` 分支。

### Decision: 答案表加 `team_id` 而非独立表存储组队答案
**选择**：在 `tb_assessment_answer` 增加 `team_id` 字段，队长提交时记录 `team_id`，队员查询时通过 `team_id` 关联到队长的答案。  
**理由**：
- 最小化数据模型改动
- 非组队题 `team_id = null`，完全兼容现有逻辑
- 评分表 `tb_assessment_judgement` 已有 `user_id` 字段，可以独立给每个队员评分

**替代方案**：新建 `tb_team_answer` 表。放弃原因：增加一层关联查询复杂度，收益不大。

### Decision: 邀请码预览接口独立于加入接口
**选择**：
- `POST /api/v1/assessment-teams/preview` — 输入邀请码，返回队伍信息（不修改数据）
- `POST /api/v1/assessment-teams/join` — 确认加入，真正写入数据  
**理由**：
- 用户明确要求"先告诉用户他现在会被邀请到哪一组，有谁，然后确认加入才能加入"
- 预览接口无副作用，可以安全地多次调用
- 前端可以在输入邀请码后实时展示预览，用户确认后再调用 join

### Decision: 复用 `UserInfo` 展示队伍成员
**选择**：队伍成员列表接口返回 `List<UserInfo>` 或类似结构，复用现有的用户信息字段（`id`, `username`, `direction`, `avatar` 等）。  
**理由**：
- 用户明确要求"接口应该尽可能复用现有DTO，如UserInfo等"
- 避免新建 `TeamMemberDTO` 重复定义用户字段
- 前端可直接使用已有的用户展示组件

### Decision: 评分保持 `user_id` 粒度，不按 `team_id`
**选择**：`tb_assessment_judgement` 的 `user_id` 记录被评分人，`answer_id` 关联到队长的答案。每个队员一条独立 judgement 记录。  
**理由**：
- 用户明确要求"每个人具有独立的得分，一个小队可以只录用其中某个人"
- 复用现有评分模型，无需改动 judgement 表结构
- 评委在评分时选择具体的人进行评分

## Risks / Trade-offs

**[Risk] `direction = null` 的考核时间可能被管理员误创建为跨方向**  
→ **Mitigation**: 管理端创建页面增加明显提示："方向为空表示跨方向共享，所有方向考生可见"

**[Risk] 队长退出或账号异常导致队伍无法提交**  
→ **Mitigation**: 队长退出前必须先转让队长身份；不允许队长直接退出。如果队长账号被删除，由管理员手动转让队长（管理端接口）。

**[Risk] 队员看到"队长已提交"后，队长又修改了答案，队员看到的是旧版本**  
→ **Mitigation**: 队员查询答案时实时查询，不做缓存；前端加入轮询或提交后通知机制。

**[Risk] 同一个考核中，用户先个人提交了 FILE_UPLOAD 题，后来又组队**  
→ **Mitigation**: 用户在该考核中已有答案记录时，禁止其创建/加入队伍。或者：加入队伍后，个人答案自动归并到队伍答案（取最新）。**当前方案**：已有答案的用户不能创建/加入队伍，需先删除个人答案。

**[Risk] 非 FILE_UPLOAD 题在 allow_team=true 的考核中如何处理**  
→ **Mitigation**: 非 FILE_UPLOAD 题不受组队影响，仍按个人答题处理。考题目录页中 FILE_UPLOAD 题显示组队相关UI，其他题正常显示。

## Migration Plan

1. **数据库迁移**：Flyway 脚本新增 `tb_assessment_team`、`tb_assessment_team_member`；修改 `tb_assessment_time`（加 `allow_team`）、`tb_assessment_answer`（加 `team_id`）
2. **后端部署**：新增表和接口后，旧代码兼容（`allow_team` 默认 `false`，`team_id` 默认 `null`）
3. **前端部署**：管理端新增"允许组队"开关；用户端组队相关UI按需加载
4. **回滚**：关闭所有考核的 `allow_team` 即可，已有队伍数据保留但不影响逻辑

## Open Questions

1. 队伍名称是否必填？是否有默认生成规则？
2. 一个考核中是否允许存在多个 FILE_UPLOAD 题？如果允许多道组队题，队伍是否需要按题细分？
3. 队长修改答案后，已出的评分是否需要重新评？
