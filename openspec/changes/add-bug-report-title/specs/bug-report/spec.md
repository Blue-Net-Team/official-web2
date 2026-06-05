## MODIFIED Requirements

### Requirement: 用户可以通过 FloatButton 提交 Bug 报告
系统 SHALL 在所有页面的右下角提供一个悬浮按钮（FloatButton），点击后弹出 Modal 表单，允许用户填写并提交 Bug 报告。

#### Scenario: 访客成功提交 Bug 报告
- **WHEN** 用户点击 FloatButton 并填写标题、描述、上传 2 张截图、输入联系邮箱
- **THEN** 系统成功创建 Bug 报告记录，返回成功提示，Modal 自动关闭

#### Scenario: 提交时仅填写必填项
- **WHEN** 用户仅填写 Bug 标题和描述（不填邮箱、不上传图片）
- **THEN** 系统仍成功创建报告，联系邮箱和图片列表为空

#### Scenario: 描述字段为空
- **WHEN** 用户未填写 Bug 描述直接点击提交
- **THEN** 系统拒绝提交，提示"请填写问题描述"

#### Scenario: 标题字段为空
- **WHEN** 用户未填写 Bug 标题直接点击提交
- **THEN** 系统拒绝提交，提示"请填写 Bug 标题"

#### Scenario: 标题超过 100 字符
- **WHEN** 用户填写的 Bug 标题超过 100 字符
- **THEN** 系统拒绝提交，提示"Bug 标题最多 100 字符"

### Requirement: 后端提供公开 Bug 报告提交接口
系统 SHALL 提供 `POST /api/v1/bug-reports` 接口，允许任何用户（含未登录访客）提交 Bug 报告。

#### Scenario: 访客调用提交接口
- **WHEN** 未登录用户调用提交接口并传入有效标题和描述
- **THEN** 系统返回 200 和创建成功的报告 ID

#### Scenario: 提交接口参数校验失败
- **WHEN** 调用提交接口时 description 为空字符串或长度超过 2000 字符
- **THEN** 系统返回 400 和参数校验错误信息

#### Scenario: 提交接口 title 为空
- **WHEN** 调用提交接口时 title 为空字符串或 null
- **THEN** 系统返回 400 和"Bug 标题不能为空"错误

#### Scenario: 提交接口 title 超长
- **WHEN** 调用提交接口时 title 长度超过 100 字符
- **THEN** 系统返回 400 和"Bug 标题最多 100 字符"错误
