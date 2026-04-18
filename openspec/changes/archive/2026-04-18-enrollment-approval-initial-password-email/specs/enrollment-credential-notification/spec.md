## ADDED Requirements

### Requirement: 审核通过为新建用户发放初始凭据
当管理员审核通过报名且系统未找到同学号用户时，系统 SHALL 生成并持久化首登凭据。

#### Scenario: 新用户审核通过时发放初始密码
- **WHEN** 管理员审核通过一条 `PENDING` 报名，且该 `studentId` 在用户表中不存在
- **THEN** 系统 SHALL 生成 8 位随机明文初始密码
- **THEN** 系统 SHALL 对该明文初始密码计算 SHA-256
- **THEN** 系统 SHALL 将 `BCrypt(SHA-256(明文))` 存入 `tb_user.password`
- **THEN** 系统 SHALL 将报名邮箱写入 `tb_user.email`

### Requirement: 初始凭据邮件通知
当审核通过创建新用户时，系统 SHALL 向报名邮箱异步发送包含明文初始密码的通知邮件。

#### Scenario: 新建用户发送初始凭据邮件
- **WHEN** 审核通过流程创建了新用户
- **THEN** 系统 SHALL 向报名邮箱异步发送 HTML 邮件
- **THEN** 邮件 SHALL 包含哈希前明文初始密码
- **THEN** 邮件 SHALL 包含首次登录后尽快修改密码的安全提示

#### Scenario: 已存在用户不重发初始凭据
- **WHEN** 管理员审核通过一条 `PENDING` 报名，且该 `studentId` 已存在用户
- **THEN** 系统 SHALL NOT 轮转或重置该用户密码
- **THEN** 系统 SHALL NOT 发送新的初始密码邮件
