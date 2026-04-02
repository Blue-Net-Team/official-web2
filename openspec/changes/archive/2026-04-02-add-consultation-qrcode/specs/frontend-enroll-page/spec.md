## ADDED Requirements

### Requirement: 咨询群二维码展示
报名页面 SHALL 展示咨询群二维码，方便新生扫码加入咨询。

#### Scenario: 咨询群组件展示
- **WHEN** 用户访问报名页面
- **THEN** 页面显示咨询群展示区域
- **AND** 区域标题为"加入咨询群"

#### Scenario: 咨询群列表展示
- **WHEN** 页面加载完成
- **THEN** 组件调用 `GET /api/v1/qrcodes/consultation` 获取数据
- **AND** 显示咨询群列表（如"咨询群1"、"咨询群2"）
- **AND** 每个列表项显示群名称

#### Scenario: 二维码悬浮预览
- **WHEN** 用户鼠标悬浮在咨询群列表项上
- **THEN** 右侧弹出二维码图片
- **AND** 图片通过 `GET /api/v1/file/download/{fileId}` 加载

#### Scenario: 无咨询群时隐藏组件
- **WHEN** API 返回空列表
- **THEN** 咨询群展示区域不显示

#### Scenario: 响应式布局
- **WHEN** 用户在移动端访问页面
- **THEN** 咨询群组件以适合移动端的方式展示
- **AND** 二维码预览不超出屏幕范围

### Requirement: 咨询群 API 集成
报名页面 SHALL 通过 API 获取咨询群数据。

#### Scenario: 获取咨询群列表
- **WHEN** 组件挂载时
- **THEN** 调用 `GET /api/v1/qrcodes/consultation`
- **AND** 请求不需要认证头

#### Scenario: 处理加载状态
- **WHEN** API 请求进行中
- **THEN** 显示加载状态（如骨架屏或 Spinner）

#### Scenario: 处理请求失败
- **WHEN** API 请求失败
- **THEN** 静默失败，不影响表单正常使用
- **AND** 控制台输出错误日志
