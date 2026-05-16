## ADDED Requirements

### Requirement: 组队题评分按队员独立记录
对于 FILE_UPLOAD 类型的组队题，系统 SHALL 允许评委对同一份答案的每个队员独立评分、独立评论。每个队员 SHALL 有独立的 `tb_assessment_judgement` 记录，共享同一个 `answer_id` 但具有不同的 `user_id`。

#### Scenario: 评委给队长评分
- **WHEN** 评委对 FILE_UPLOAD 组队题的队长进行评分
- **THEN** 系统 SHALL 创建一条 judgement 记录，`answer_id` 为队长的答案，`user_id` 为队长 ID

#### Scenario: 评委给队员评分
- **WHEN** 评委对同一 FILE_UPLOAD 组队题的队员进行评分
- **THEN** 系统 SHALL 创建另一条 judgement 记录，`answer_id` 仍为队长的答案（同一份作品），但 `user_id` 为该队员 ID

#### Scenario: 不同队员得分不同
- **WHEN** 评委给队长打 85 分，给队员 A 打 82 分，给队员 B 打 78 分
- **THEN** 系统 SHALL 保存三条独立的 judgement 记录，各自具有不同的分数

### Requirement: 组队题答案查询返回队长作品
评委查询 FILE_UPLOAD 组队题的答案时，系统 SHALL 返回队长提交的作品（`answer_id` 关联到队长）。评委评分时 SHALL 明确指定被评分人（`user_id`）。

#### Scenario: 评委查看组队题答案
- **WHEN** 评委查询某 FILE_UPLOAD 组队题的答案
- **THEN** 系统 SHALL 返回队长提交的答案及文件信息

#### Scenario: 评委选择队员进行评分
- **WHEN** 评委在评分界面选择某个队员进行评分
- **THEN** 系统 SHALL 将该评分关联到该队员的 `user_id`

## MODIFIED Requirements

### Requirement: Manual review for file upload answers
系统 SHALL 允许具有 team member 或更高权限的用户对文件上传答案进行评分和评论。对于组队题，评分 SHALL 针对具体队员，每个队员有独立的评分记录。

#### Scenario: Member scores file upload answer
- **WHEN** team member 为文件上传答案提交有效分数和评论
- **THEN** 系统 SHALL 保存人工评分，并在答案评审视图中展示
- **AND** 系统 SHALL 同时在 `tb_comment` 中保存评论记录以便多评审者可见

#### Scenario: Member scores team member individually
- **WHEN** team member 为 FILE_UPLOAD 组队题的某个队员进行评分
- **THEN** 系统 SHALL 保存该评分并关联到该队员的 `user_id`，不影响其他队员的评分

#### Scenario: Candidate cannot score file upload answer
- **WHEN** candidate 尝试为任何文件上传答案评分
- **THEN** 系统 SHALL 拒绝该操作并返回 forbidden 响应

### Requirement: Judgement result visibility
系统 SHALL 允许考生查看自己的评分结果，允许 team member 或更高角色在其授权范围内查看考生的评分结果。对于组队题，考生 SHALL 只能看到自己的评分，不能看到队友的评分。

#### Scenario: Candidate views own result
- **WHEN** candidate 请求查看自己提交的答案的评分结果
- **THEN** 系统 SHALL 返回该 candidate 的评分结果

#### Scenario: Team member views candidate result
- **WHEN** team member 在其授权的考核范围内请求查看 candidate 的评分结果
- **THEN** 系统 SHALL 返回匹配的 candidate 评分结果

#### Scenario: Team member cannot view teammate's result
- **WHEN** candidate（队员）尝试查看队友的评分结果
- **THEN** 系统 SHALL 返回 403 错误，提示只能查看自己的评分
