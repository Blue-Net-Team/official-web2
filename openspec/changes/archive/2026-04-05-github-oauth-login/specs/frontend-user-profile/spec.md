## ADDED Requirements

### Requirement: GitHub 账号绑定展示与管理

系统 SHALL 在个人信息 Tab 的邮箱绑定区域下方展示 GitHub 绑定状态，并提供绑定/解绑操作。

#### Scenario: 展示未绑定状态
- **WHEN** 用户查看个人信息且未绑定 GitHub
- **THEN** 系统 SHALL 显示"未绑定"状态
- **AND** 系统 SHALL 显示"绑定 GitHub"按钮

#### Scenario: 展示已绑定状态
- **WHEN** 用户查看个人信息且已绑定 GitHub
- **THEN** 系统 SHALL 显示 GitHub 用户名
- **AND** 系统 SHALL 显示"已绑定"状态
- **AND** 系统 SHALL 显示"解绑"按钮

#### Scenario: 发起绑定
- **WHEN** 用户点击"绑定 GitHub"按钮
- **THEN** 系统 SHALL 调用 `GET /api/v1/auth/github/bind` 获取授权 URL
- **THEN** 系统 SHALL 将浏览器重定向到该授权 URL

#### Scenario: 绑定回调成功
- **WHEN** 页面加载时 URL 包含 `?github=binding_success` 参数
- **THEN** 系统 SHALL 显示"GitHub 账号绑定成功"提示
- **AND** 系统 SHALL 刷新绑定状态

#### Scenario: 绑定回调失败 - 已被其他用户绑定
- **WHEN** 页面加载时 URL 包含 `?github=already_bound` 参数
- **THEN** 系统 SHALL 显示"该 GitHub 账号已被其他用户绑定"错误提示

#### Scenario: 解绑确认
- **WHEN** 用户点击"解绑"按钮
- **THEN** 系统 SHALL 显示确认弹窗"确定要解绑 GitHub 账号吗？解绑后将无法使用 GitHub 登录"

#### Scenario: 解绑成功
- **WHEN** 用户确认解绑且 API 返回成功
- **THEN** 系统 SHALL 显示"已解绑 GitHub 账号"提示
- **AND** 系统 SHALL 刷新绑定状态为未绑定

#### Scenario: 解绑失败
- **WHEN** 用户确认解绑但 API 返回失败
- **THEN** 系统 SHALL 显示错误提示
