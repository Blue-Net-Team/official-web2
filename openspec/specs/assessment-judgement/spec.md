## ADDED Requirements

### Requirement: Team member judgement auto-creation
The system SHALL create judgements for all team members when an automatic or finalized judgement is recorded for a team leader's answer in a team-enabled assessment.

#### Scenario: Automatic objective judgement propagates to team members
- **WHEN** the system creates an automatic judgement (AUTO source) for a team leader's single choice, multiple choice, or algorithm answer
- **AND** the assessment allows teams
- **THEN** the system SHALL create an identical automatic judgement for each team member with the same score, result code, and max score

#### Scenario: Admin finalized judgement propagates to team members
- **WHEN** a direction administrator confirms a final score for a team leader's FILE_UPLOAD answer
- **THEN** the system SHALL create an `ADMIN_FINALIZED` judgement for the leader
- **AND** the system SHALL create an identical `ADMIN_FINALIZED` judgement for each team member

## Requirements

### Requirement: Manual review for file upload answers
The system SHALL allow users with team member or higher permission to score file upload answers, with support for team-wide or individual member scoring.

#### Scenario: Member scores file upload answer
- **WHEN** a team member submits a valid score for a file upload answer
- **THEN** the system SHALL save a manual judgement and expose it in the answer review view
- **AND** the system SHALL also save a comment record in `tb_comment` for multi-reviewer visibility

#### Scenario: Member scores team member individually
- **WHEN** a team member submits a score for a specific team member's FILE_UPLOAD answer
- **THEN** the system SHALL save a manual judgement for that member only
- **AND** other team members' judgements SHALL remain unchanged

#### Scenario: Candidate cannot score file upload answer
- **WHEN** a candidate attempts to score any file upload answer
- **THEN** the system SHALL reject the operation with a forbidden response

### Requirement: Judgement result visibility
The system SHALL allow candidates to view their own judgement results and allow team members or higher roles to view candidate judgement results within their authorized scope.

For global assessments (`direction = null`):
- DIRECTION_ADMIN SHALL be able to view candidate judgement results regardless of the candidate's direction
- Team members SHALL be able to view candidate judgement results

#### Scenario: Direction admin views global assessment results
- **WHEN** a DIRECTION_ADMIN (direction=COMPUTER_VISION) requests judgement results for a global assessment (`direction = null`)
- **THEN** the system SHALL return candidate judgement results across all directions

#### Scenario: Member views global assessment results
- **WHEN** a team member requests judgement results for a global assessment within their authorized role scope
- **THEN** the system SHALL return the matching candidate judgement results

### Requirement: Assessment pass decision
The system SHALL allow direction administrators or higher roles to set the final pass decision for a candidate assessment based on the question judgement results, supporting global assessments and team members independently.

For global assessments (`direction = null`), any DIRECTION_ADMIN SHALL be able to set pass decisions for any candidate regardless of the candidate's direction.

#### Scenario: Direction administrator marks candidate as passed
- **WHEN** a direction administrator sets a candidate assessment decision to passed
- **THEN** the system SHALL save the decision, decision maker, decision time, and optional decision comment
- **AND** if the assessment time does not yet have `results_published_at` set, the system SHALL set it to the current time

#### Scenario: Direction administrator marks team member as passed independently
- **WHEN** a direction administrator sets a pass decision for a team member in a global assessment
- **THEN** the system SHALL save the decision for that member only
- **AND** other team members' decisions SHALL remain unchanged

#### Scenario: Direction admin marks candidate from another direction as passed in global assessment
- **WHEN** a COMPUTER_VISION DIRECTION_ADMIN sets a pass decision for a STRUCTURAL_DESIGN candidate in a global assessment
- **THEN** the system SHALL save the decision and return success

#### Scenario: Member cannot set final decision
- **WHEN** a team member attempts to set the final pass decision
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Eliminated candidate from prior epoch is excluded from decision workspace
- **WHEN** a direction administrator queries the decision workspace for an assessment time
- **AND** a candidate has a `passed = false` decision for a prior epoch of the same direction and grade combination, or for any prior epoch when the current assessment is global (direction is null)
- **THEN** the system SHALL exclude that candidate from the workspace candidate list
- **AND** the system SHALL exclude that candidate from the workspace statistics
- **AND** the system SHALL still include candidates with a `passed = false` decision for the current epoch

### Requirement: 题目评分提交列表移动端卡片化
题目评分页（`/admin/assessment/judge/score`）的提交列表（含题目视图与考生视图中的提交列表、队伍头行与成员行）SHALL 在移动端（`isMobile = !screens.md`）由内嵌 Table 改为卡片列表，解决学号等窄列在手机上每行仅显示一个数字的问题。

卡片 SHALL 按以下结构渲染：
- 队伍头行渲染为通栏卡片（队名 + 人数 + 队长标签，图标使用 `TeamOutlined`）
- 成员行渲染为缩进卡片：主行姓名（含队长 `CrownOutlined` 标签/队伍标签）+ 学号独立一行（等宽数字），右侧得分 `x / max` 与评分状态标签（已评分/待评分）
- 操作区：评判时间 + 评分/改分主按钮

#### Scenario: 移动端成员卡片展示学号
- **WHEN** 评分人在移动端视口查看某题目的提交列表
- **THEN** 每名考生渲染一张卡片，学号在独立行完整展示
- **AND** 得分、结果、状态与操作按钮在卡片内不挤压、不折行

#### Scenario: 队伍卡片与成员卡片层级
- **WHEN** 提交以队伍为单位展示
- **THEN** 队伍头行渲染为通栏卡片，其成员卡片相对队伍卡片缩进显示
- **AND** 队长卡片带皇冠图标标签，与桌面端标识一致

#### Scenario: 移动端触发评分弹窗
- **WHEN** 用户点击卡片上的评分 / 改分按钮
- **THEN** SHALL 打开与桌面端一致的评分 Modal（宽度在移动端为全屏）

#### Scenario: 桌面端保持内嵌表格
- **WHEN** 视口大于等于 md 断点
- **THEN** 提交列表 SHALL 以原内嵌 Table 渲染，含队伍头行合并行逻辑，行为与改动前一致

### Requirement: 移动端范围限定
本次卡片化范围 SHALL 限定为提交列表（submissionColumns / submissionMemberColumns 及其渲染处）；排行榜（questionColumns / candidateColumns / decisionColumns）、筛选区与详情 Drawer 仅在既有 `screens.md` 自适应基础上保证不溢出，不改为卡片。

#### Scenario: 排行榜在移动端不溢出
- **WHEN** 用户在移动端查看题目/考生排行榜
- **THEN** 排行榜区域 SHALL 可横向滚动查看完整内容，不发生内容截断或布局错乱
