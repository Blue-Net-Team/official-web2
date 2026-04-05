## Requirements

### Requirement: 管理端创建考题
系统 SHALL 允许管理员通过 `POST /api/v1/admin/assessment-questions` 创建考题。请求体包含 assessmentTimeId、questionNo、questionType、title、content、attachmentId（可选）、score。

#### Scenario: 成功创建考题
- **WHEN** 管理员提交合法的创建请求
- **THEN** 系统创建考题并返回 200 + AssessmentQuestionDTO

#### Scenario: 创建时考核时间不存在
- **WHEN** 管理员提交的 assessmentTimeId 对应的考核时间不存在
- **THEN** 系统返回 400 错误 "考核时间不存在"

#### Scenario: 创建时题号重复
- **WHEN** 管理员提交的 assessmentTimeId + questionNo 组合已存在
- **THEN** 系统返回 409 错误 "该考核时间下题号已存在"

#### Scenario: 创建时参数校验失败
- **WHEN** 管理员提交的请求缺少必填字段（questionType、title 等）
- **THEN** 系统返回 400 参数校验错误

### Requirement: 管理端更新考题
系统 SHALL 允许管理员通过 `PUT /api/v1/admin/assessment-questions/{id}` 更新考题信息。

#### Scenario: 成功更新考题
- **WHEN** 管理员提交合法的更新请求且考题存在
- **THEN** 系统更新考题并返回 200 + AssessmentQuestionDTO

#### Scenario: 更新不存在的考题
- **WHEN** 管理员提交更新请求但考题 ID 不存在
- **THEN** 系统返回 404 错误 "考题不存在"

#### Scenario: 方向管理员更新非本方向考题
- **WHEN** 方向管理员尝试更新非本方向的考题
- **THEN** 系统返回 403 错误 "无权操作该方向的考题"

### Requirement: 管理端删除考题
系统 SHALL 允许管理员通过 `DELETE /api/v1/admin/assessment-questions/{id}` 删除考题。

#### Scenario: 成功删除考题
- **WHEN** 管理员删除存在的考题且无关联答题记录
- **THEN** 系统删除考题并返回 200

#### Scenario: 删除不存在的考题
- **WHEN** 管理员删除不存在的考题
- **THEN** 系统返回 404 错误 "考题不存在"

#### Scenario: 删除有关联答题记录的考题
- **WHEN** 管理员删除存在关联答题记录的考题
- **THEN** 系统返回 409 错误 "该题目存在答题记录，无法删除"

### Requirement: 管理端分页查询考题
系统 SHALL 允许管理员通过 `GET /api/v1/admin/assessment-questions?assessmentTimeId={id}&page=0&size=10` 分页查询考题。

#### Scenario: 成功查询考题列表
- **WHEN** 管理员发起分页查询请求
- **THEN** 系统返回 200 + PageDTO<AssessmentQuestionDTO>，按 questionNo 升序排列

#### Scenario: 查询指定考核时间的考题
- **WHEN** 管理员提供 assessmentTimeId 参数
- **THEN** 系统仅返回该考核时间下的考题

#### Scenario: 超级管理员查询所有考题
- **WHEN** 超级管理员发起查询
- **THEN** 系统返回所有考题（不受方向限制）

#### Scenario: 方向管理员查询考题
- **WHEN** 方向管理员发起查询
- **THEN** 系统仅返回本方向考核时间下的考题

### Requirement: 用户端查询考题目录
系统 SHALL 允许已登录用户通过 `GET /api/v1/assessment-questions?assessmentTimeId={id}&page=0&size=10` 分页查询考题目录。

#### Scenario: 考生查询本方向考题
- **WHEN** 考生查询考核时间 ID 属于自己方向和年级的考题
- **THEN** 系统返回 200 + PageDTO<AssessmentQuestionDTO>，不包含 content 详细内容

#### Scenario: 考生查询非本方向考题
- **WHEN** 考生查询考核时间 ID 不属于自己方向或年级
- **THEN** 系统返回 403 错误 "无权查看该考核的题目"

#### Scenario: 团队成员查询任意考题
- **WHEN** 团队成员（ROLE_MEMBER 及以上）查询任意考核时间的考题
- **THEN** 系统返回该考核时间下的所有考题
