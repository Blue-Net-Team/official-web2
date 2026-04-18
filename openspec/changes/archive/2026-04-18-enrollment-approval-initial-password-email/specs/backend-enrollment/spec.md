## MODIFIED Requirements

### Requirement: 报名状态生命周期
系统 SHALL 支持报名状态从待审核变更为通过或拒绝。

#### Scenario: 状态转换规则
- **WHEN** 创建报名记录
- **THEN** 状态 SHALL 默认为待审核
- **WHEN** 状态变更为通过
- **THEN** 系统 MAY 基于报名数据创建用户账号
- **THEN** 若审核通过时创建了新用户，系统 SHALL 持久化报名邮箱与首登凭据
- **THEN** 若同学号用户已存在，系统 SHALL NOT 在审核通过时轮转既有凭据
