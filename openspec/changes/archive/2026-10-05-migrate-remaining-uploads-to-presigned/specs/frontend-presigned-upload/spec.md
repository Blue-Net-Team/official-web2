# frontend-presigned-upload

## ADDED Requirements

### Requirement: 前端文件上传全面使用预签名直传通道
前端所有文件上传场景 MUST 使用预签名直传通道（`usePresignedUpload`：prepare-upload → PUT 直传 OSS → confirm-upload），MUST NOT 存在经后端中转的上传调用（`fileService.upload` / `POST /api/v1/file/upload`）。以下消费方 SHALL 全部走预签名通道：报名页头像、个人主页头像、竞赛 Logo、竞赛封面、考核题目（QuestionDetail / QuestionDrawer）、BugReport、报名表单管理页、场地图片（VenueDrawer）、设备图片（EquipmentDrawer）、管理端二维码（QrcodeDrawer）、个人微信二维码（ProfileInfo）、成就附件（AchievementDrawer）。

#### Scenario: 场地图片预签名上传
- **WHEN** 管理员在场地表单中选择图片上传
- **THEN** 浏览器直接 PUT 到预签名 URL，不经后端中转
- **AND** 上传成功后 fileId 回填场地表单

#### Scenario: 设备图片预签名上传
- **WHEN** 管理员在设备表单中选择图片上传
- **THEN** 走预签名直传通道
- **AND** 上传成功后 fileId 回填设备表单

#### Scenario: 竞赛封面预签名上传
- **WHEN** 管理员在竞赛表单中选择封面图片
- **THEN** 走预签名直传通道且不经过裁剪弹窗
- **AND** 上传成功后 coverFileId 回填表单

#### Scenario: 二维码预签名上传
- **WHEN** 管理员在二维码管理或个人资料页上传二维码图片
- **THEN** 走预签名直传通道（QRCODE 类型）
- **AND** 上传成功后 fileId 回填对应表单

#### Scenario: 成就附件预签名上传
- **WHEN** 管理员在成就表单中上传附件文件
- **THEN** 走预签名直传通道，文件类型保持不变（NORMAL_IMG）
- **AND** 上传成功后 fileId 回填成就表单

#### Scenario: 前端无旧通道调用残留
- **WHEN** 全局搜索 `fileService.upload` 或 `POST /file/upload` 的调用
- **THEN** 前端代码中不存在任何匹配
- **AND** `file.service.ts` 中不再定义 `upload` 方法
