## ADDED Requirements

### Requirement: 创建考核队伍
系统 SHALL 允许已登录用户在允许组队的考核中创建队伍。创建时 MUST 校验该用户当前在该考核中未加入任何队伍，且该考核 `allow_team = true`。

创建成功后，系统 SHALL 自动生成唯一邀请码（6位字母数字），并将创建者设为队长。

#### Scenario: 成功创建队伍
- **WHEN** 用户在 `allow_team = true` 的考核中请求创建队伍
- **THEN** 系统 SHALL 创建队伍记录，设置 `leader_id` 为该用户，`invite_code` 为自动生成的唯一码，并返回队伍信息（含邀请码）

#### Scenario: 考核不允许组队
- **WHEN** 用户请求在 `allow_team = false` 的考核中创建队伍
- **THEN** 系统 SHALL 返回 400 错误，提示该考核不支持组队

#### Scenario: 用户已在该考核中有队伍
- **WHEN** 用户请求创建队伍，但该用户已在该考核中属于某个队伍
- **THEN** 系统 SHALL 返回 409 Conflict 错误，提示用户已有队伍

#### Scenario: 用户已有个人答案
- **WHEN** 用户请求创建队伍，但该用户已在该考核的 FILE_UPLOAD 题上提交了个人答案
- **THEN** 系统 SHALL 返回 409 Conflict 错误，提示已有个人答案，无法组队

### Requirement: 邀请码预览队伍信息
系统 SHALL 提供无副作用的预览接口，用户输入邀请码后可查看队伍信息（队伍名称、队长、成员列表），但不加入队伍。

预览接口返回的成员列表 SHALL 复用现有的 `UserInfo` 结构，包含用户基本信息（`id`, `username`, `direction`, `avatar` 等）。

#### Scenario: 有效邀请码预览
- **WHEN** 用户提交有效的邀请码
- **THEN** 系统 SHALL 返回对应队伍的名称、队长信息、当前成员列表，且 SHALL NOT 修改任何数据

#### Scenario: 无效邀请码预览
- **WHEN** 用户提交不存在的邀请码
- **THEN** 系统 SHALL 返回 404 错误，提示邀请码无效

#### Scenario: 邀请码已过期
- **WHEN** 用户提交对应考核已结束的邀请码
- **THEN** 系统 SHALL 返回 400 错误，提示该邀请码已过期

### Requirement: 通过邀请码加入队伍
系统 SHALL 允许已登录用户通过邀请码加入队伍。加入前用户 MUST 在预览后显式确认。

加入时 SHALL 校验：
- 邀请码存在且对应考核未结束
- 用户未在该考核中加入其他队伍
- 用户在该考核的 FILE_UPLOAD 题上无个人答案记录

#### Scenario: 成功加入队伍
- **WHEN** 用户提交有效邀请码并确认加入
- **THEN** 系统 SHALL 将用户加入队伍成员列表，并返回更新后的队伍信息

#### Scenario: 用户已在其他队伍
- **WHEN** 用户尝试加入队伍，但该用户已在同一考核的其他队伍中
- **THEN** 系统 SHALL 返回 409 Conflict 错误，提示用户已有队伍

#### Scenario: 用户已有个人答案
- **WHEN** 用户尝试加入队伍，但该用户已在该考核的 FILE_UPLOAD 题上提交了个人答案
- **THEN** 系统 SHALL 返回 409 Conflict 错误，提示已有个人答案，无法加入队伍

### Requirement: 查询当前用户的队伍
系统 SHALL 提供接口查询当前用户在指定考核中的队伍信息，包括队伍名称、队长、成员列表、邀请码。

成员列表 SHALL 复用现有的 `UserInfo` 结构。

#### Scenario: 用户已组队
- **WHEN** 用户查询自己在某考核中的队伍
- **THEN** 系统 SHALL 返回队伍详情及成员列表

#### Scenario: 用户未组队
- **WHEN** 用户查询自己在某考核中的队伍，但该用户未加入任何队伍
- **THEN** 系统 SHALL 返回 404 或空数据，表示未组队

### Requirement: 退出队伍
系统 SHALL 允许队员退出队伍。队长 MUST NOT 直接退出，必须先转让队长身份。

退出后，该用户 SHALL 恢复为未组队状态，可重新创建或加入其他队伍（考核未结束前提下）。

#### Scenario: 队员成功退出
- **WHEN** 队员请求退出队伍
- **THEN** 系统 SHALL 从队伍成员列表中移除该用户

#### Scenario: 队长直接退出被拒绝
- **WHEN** 队长请求退出队伍但未转让队长身份
- **THEN** 系统 SHALL 返回 403 错误，提示队长需先转让队长身份

### Requirement: 转让队长身份
系统 SHALL 允许队长将队长身份转让给其他队员。转让后原队长变为普通队员。

#### Scenario: 队长成功转让
- **WHEN** 队长将队长身份转让给另一名队员
- **THEN** 系统 SHALL 更新 `leader_id` 为目标用户，并返回更新后的队伍信息

#### Scenario: 非队长尝试转让
- **WHEN** 非队长用户尝试转让队长身份
- **THEN** 系统 SHALL 返回 403 错误

#### Scenario: 转让给非队员
- **WHEN** 队长尝试将队长身份转让给不在该队伍中的用户
- **THEN** 系统 SHALL 返回 400 错误

### Requirement: 队长提交组队题答案
在允许组队的考核中，FILE_UPLOAD 类型的题目仅队长有权提交和更新答案。队员 SHALL 只能查看队长提交的文件，不可修改。

提交时，答案记录的 `team_id` SHALL 设为当前队伍 ID，`user_id` SHALL 设为队长 ID。

#### Scenario: 队长提交组队题答案
- **WHEN** 队长提交 FILE_UPLOAD 题答案
- **THEN** 系统 SHALL 保存答案，设置 `team_id` 为队伍 ID，返回成功

#### Scenario: 队员尝试提交组队题答案
- **WHEN** 队员尝试提交 FILE_UPLOAD 题答案
- **THEN** 系统 SHALL 返回 403 错误，提示仅队长可提交

#### Scenario: 非组队题不受限制
- **WHEN** 用户在允许组队的考核中提交非 FILE_UPLOAD 题答案
- **THEN** 系统 SHALL 按个人答题正常处理，不校验队伍和队长权限

### Requirement: 队员查看组队题答案
队员 SHALL 通过队伍关联查看队长提交的 FILE_UPLOAD 题答案。查询接口 SHALL 根据当前用户的 `team_id` 返回队长的答案。

#### Scenario: 队员查看组队题答案
- **WHEN** 队员查询自己在某 FILE_UPLOAD 题的答案
- **THEN** 系统 SHALL 返回队长提交的答案（file_id、content 等）

#### Scenario: 未组队用户查看组队题答案
- **WHEN** 未组队的用户查询 FILE_UPLOAD 题答案且该考核允许组队
- **THEN** 系统 SHALL 返回 404 或提示需要先组队

### Requirement: 考核结束后队伍自动解散
系统 SHALL 在考核结束后将相关队伍状态置为 `DISBANDED`，禁止后续加入、退出、提交等操作。

#### Scenario: 考核结束后提交被拒绝
- **WHEN** 用户在已结束的考核中尝试提交答案
- **THEN** 系统 SHALL 返回 400 错误，提示考核已结束

#### Scenario: 考核结束后加入队伍被拒绝
- **WHEN** 用户尝试加入已结束考核的队伍
- **THEN** 系统 SHALL 返回 400 错误，提示考核已结束
