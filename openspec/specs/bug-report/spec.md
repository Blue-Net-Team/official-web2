# bug-report Specification

## Purpose
TBD - created by archiving change add-bug-report-feature. Update Purpose after archive.
## Requirements
### Requirement: 用户可以通过 FloatButton 提交 Bug 报告
系统 SHALL 在所有页面的右下角提供一个悬浮按钮（FloatButton），点击后弹出 Modal 表单，允许用户填写并提交 Bug 报告。

#### Scenario: 访客成功提交 Bug 报告
- **WHEN** 用户点击 FloatButton 并填写描述、上传 2 张截图、输入联系邮箱
- **THEN** 系统成功创建 Bug 报告记录，返回成功提示，Modal 自动关闭

#### Scenario: 提交时仅填写必填项
- **WHEN** 用户仅填写 Bug 描述（不填邮箱、不上传图片）
- **THEN** 系统仍成功创建报告，联系邮箱和图片列表为空

#### Scenario: 描述字段为空
- **WHEN** 用户未填写 Bug 描述直接点击提交
- **THEN** 系统拒绝提交，提示"请填写问题描述"

### Requirement: Bug 报告自动捕获页面与环境信息
系统 SHALL 在提交 Bug 报告时自动记录当前页面 URL 和浏览器环境信息。

#### Scenario: 自动附加环境信息
- **WHEN** 用户在任何页面提交 Bug 报告
- **THEN** 系统记录当前 `window.location.href`、`navigator.userAgent`、屏幕分辨率、视口大小

### Requirement: Bug 报告支持上传截图（最多 3 张）
系统 SHALL 允许用户在提交 Bug 报告时上传截图，最多 3 张，截图通过现有 MinIO 文件系统存储。

#### Scenario: 上传多张截图（上限内）
- **WHEN** 用户在表单中上传 3 张截图
- **THEN** 每张截图通过 `POST /api/v1/file/upload` 上传后获得 fileId，提交时携带 fileId 列表，后端建立关联

#### Scenario: 不上传截图
- **WHEN** 用户不上传任何截图
- **THEN** 系统创建无图片关联的 Bug 报告

#### Scenario: 上传超过 3 张截图
- **WHEN** 用户尝试上传第 4 张截图
- **THEN** 系统阻止上传，提示"最多上传 3 张截图"

#### Scenario: 提交时携带超过 3 个 fileId
- **WHEN** 调用提交接口时 fileIds 数量超过 3 个
- **THEN** 系统返回 400 和"截图数量不能超过 3 张"错误

### Requirement: 提交后给出成功反馈
系统 SHALL 在用户成功提交 Bug 报告后，通过 Ant Design App 的 message 组件显示成功提示。

#### Scenario: 提交成功提示
- **WHEN** Bug 报告提交成功
- **THEN** 页面显示 "提交成功，感谢反馈！" 的 message 提示，持续 3 秒

### Requirement: 后端提供公开 Bug 报告提交接口
系统 SHALL 提供 `POST /api/v1/bug-reports` 接口，允许任何用户（含未登录访客）提交 Bug 报告。

#### Scenario: 访客调用提交接口
- **WHEN** 未登录用户调用提交接口并传入有效参数
- **THEN** 系统返回 200 和创建成功的报告 ID

#### Scenario: 提交接口参数校验失败
- **WHEN** 调用提交接口时 description 为空字符串或长度超过 2000 字符
- **THEN** 系统返回 400 和参数校验错误信息

