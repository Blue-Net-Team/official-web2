## MODIFIED Requirements

### Requirement: 用户信息卡片展示
系统 SHALL 在个人主页左侧展示用户信息卡片，包含头像、姓名、角色、简介、基本信息、微信二维码和统计数据。头像区域 SHALL 支持已登录用户点击上传新头像，hover 时显示编辑遮罩。

#### Scenario: 展示已设置微信二维码的用户信息卡片
- **WHEN** 用户访问个人主页且已设置微信二维码
- **THEN** 系统 SHALL 在侧边栏展示微信二维码图片
- **AND** 二维码图片 SHALL 可通过点击放大预览

#### Scenario: 展示未设置微信二维码的用户信息卡片
- **WHEN** 用户访问个人主页且未设置微信二维码
- **THEN** 系统 SHALL 在侧边栏展示占位提示，如"暂无微信二维码"

### Requirement: 个人信息编辑
系统 SHALL 允许用户编辑个人信息，包括姓名、昵称、年级、学院、专业、报名方向、GitHub 链接、个人简介和微信二维码。

#### Scenario: 编辑个人信息包含微信二维码
- **WHEN** 用户在"个人信息"Tab 修改表单并点击保存
- **AND** 表单中包含微信二维码文件标识
- **THEN** 系统调用 `updateProfile()` API
- **AND** 更新成功后显示保存成功提示
- **AND** 页面二维码区域 SHALL 刷新为新上传的二维码

#### Scenario: 上传微信二维码
- **WHEN** 用户在编辑模式下点击二维码上传区域
- **THEN** 系统 SHALL 打开文件选择器，支持选择图片文件（JPG/PNG）
- **AND** 选择文件后 SHALL 调用预签名上传接口，文件类型为 `QRCODE`
- **AND** 上传成功后 SHALL 在表单中显示二维码预览

#### Scenario: 删除微信二维码
- **WHEN** 用户在编辑模式下点击已上传二维码的删除按钮
- **THEN** 系统 SHALL 移除表单中的二维码文件标识
- **AND** 保存后用户二维码 SHALL 被清空

### Requirement: 前端可获取当前用户信息
前端 SHALL 通过 `GET /api/v1/user/info` 获取当前登录用户的基本信息，包含微信二维码。

#### Scenario: 成功获取包含二维码的用户信息
- **WHEN** 已登录用户访问个人主页
- **THEN** 系统调用 `getUserInfo()` API 获取用户信息
- **AND** 响应 SHALL 包含 `wechatQrcode` 字段
- **AND** 页面 SHALL 展示真实的用户数据及二维码

### Requirement: 前端可更新用户信息
前端 SHALL 通过 `PUT /api/v1/user/info` 更新用户信息，包含微信二维码文件标识，根据用户角色控制可修改字段（二维码不受角色限制）。

#### Scenario: 更新个人信息包含二维码
- **WHEN** 用户修改昵称、个人简介或微信二维码并点击保存
- **THEN** 系统调用 `updateProfile()` API，请求体包含 `qrcodeFileId`
- **AND** 更新成功后显示成功提示
