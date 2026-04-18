## ADDED Requirements

### Requirement: 报名审核通过初始凭据邮件通知
消息通知子系统 SHALL 支持为新建用户发送“报名审核通过 + 初始凭据”邮件。

#### Scenario: 凭据邮件内容构成
- **WHEN** 系统发送报名审核通过凭据邮件
- **THEN** 邮件 SHALL 以 HTML 形式渲染
- **THEN** 邮件 SHALL 包含可识别申请人的上下文信息与明文初始密码
- **THEN** 邮件 SHALL 包含首次登录后修改密码的安全提示

#### Scenario: 异步发送行为
- **WHEN** 审核通过触发凭据通知
- **THEN** 邮件 SHALL 通过现有邮件发送抽象异步发送
- **THEN** 发送失败 SHALL 在日志中可观测，便于运营跟进
