# frontend-enroll-page

## MODIFIED Requirements

### Requirement: 头像上传功能
报名页面 SHALL 支持用户上传个人头像。用户选择图片后 SHALL 先经过圆形裁剪弹窗（1:1，输出 512×512），确认后上传裁剪结果；上传区域的预览图 SHALL 为裁剪后的图片。文件校验 SHALL 仅允许 jpeg/png/webp，最大体积 5MB，GIF MUST NOT 进入裁剪弹窗。

#### Scenario: 头像上传区域展示
- **WHEN** 页面加载完成
- **THEN** 显示圆形头像上传区域
- **AND** 显示上传图标和提示文字

#### Scenario: 选择、裁剪和预览头像
- **WHEN** 用户点击头像上传区域
- **THEN** 打开文件选择对话框
- **WHEN** 用户选择通过校验的图片文件
- **THEN** 弹出圆形裁剪弹窗，不立即上传
- **WHEN** 用户确认裁剪
- **THEN** 上传区域预览裁剪后的图片
- **AND** 开始上传并显示进度
- **AND** 显示删除按钮

#### Scenario: 删除已上传头像
- **WHEN** 用户 hover 已上传头像
- **THEN** 显示删除按钮
- **WHEN** 用户点击删除按钮
- **THEN** 清除已上传的头像
- **AND** 恢复默认上传状态

#### Scenario: 头像验证
- **WHEN** 用户选择的文件不是 jpeg/png/webp 类型（含 GIF）
- **THEN** 显示"请选择图片文件（JPG/PNG/WEBP）"错误提示
- **AND** 不打开裁剪弹窗，不发起上传请求
- **WHEN** 用户选择的图片超过 5MB
- **THEN** 显示"图片大小不能超过 5MB"错误提示
- **AND** 不打开裁剪弹窗，不发起上传请求

#### Scenario: 取消裁剪
- **WHEN** 用户在裁剪弹窗中点击取消或关闭弹窗
- **THEN** 不发起上传，头像上传区域保持原状
