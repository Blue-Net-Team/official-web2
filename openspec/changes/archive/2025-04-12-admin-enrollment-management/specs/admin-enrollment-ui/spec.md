## ADDED Requirements

### Requirement: 报名统计卡片展示
系统 SHALL 在报名管理页面顶部展示统计卡片，包含报名总数、各状态（待审核/已通过/已拒绝）人数。卡片使用数字展示，PC 端横向排列，移动端 2 列网格。

#### Scenario: 页面初始加载展示统计数据
- **WHEN** 管理员访问 `/admin/enroll` 页面
- **THEN** 系统调用 `GET /api/v1/admin/enrollments/statistics` 获取统计数据，并在页面顶部以数字卡片形式展示总数和各状态人数

### Requirement: 报名列表卡片展示
系统 SHALL 以卡片网格形式展示报名列表。PC 端 3 列网格，移动端单列。每张卡片 SHALL 展示：头像（有 avatarFileId 则显示图片，否则显示 Ant Design UserOutlined 图标）、姓名、学号、学院、方向、状态标签。仅待审核状态的卡片 SHALL 显示操作按钮。

#### Scenario: 加载报名列表
- **WHEN** 管理员访问页面
- **THEN** 系统调用 `GET /api/v1/admin/enrollments` 获取分页数据，以卡片网格展示

#### Scenario: 卡片头像显示
- **WHEN** 报名记录有 avatarFileId
- **THEN** 卡片通过 `GET /api/v1/file/download/{fileId}` 加载并显示头像图片
- **WHEN** 报名记录无 avatarFileId
- **THEN** 卡片显示 Ant Design UserOutlined 默认图标

### Requirement: 搜索与筛选
系统 SHALL 提供单个搜索框和两个下拉筛选器（状态、方向）。搜索框输入关键词后 SHALL 同时匹配姓名和学号。筛选变更后 SHALL 立即刷新列表和统计数据。

#### Scenario: 按关键词搜索
- **WHEN** 管理员在搜索框输入关键词并触发搜索
- **THEN** 系统将关键词作为 `keyword` 参数调用列表接口，返回匹配的报名记录

#### Scenario: 按状态筛选
- **WHEN** 管理员选择某个状态筛选值
- **THEN** 系统将状态作为 `status` 参数调用列表接口

#### Scenario: 按方向筛选
- **WHEN** 管理员选择某个方向筛选值
- **THEN** 系统将方向作为 `direction` 参数调用列表接口

### Requirement: 分页浏览
系统 SHALL 在卡片列表底部展示分页器，支持翻页浏览。默认每页 12 条（适配 3×4 网格）。

#### Scenario: 翻页操作
- **WHEN** 管理员点击分页器的某一页
- **THEN** 系统以对应页码调用列表接口并刷新卡片展示

### Requirement: 查看报名详情（Drawer）
系统 SHALL 支持点击卡片后从右侧弹出 Drawer 展示完整报名信息。Drawer 内容 SHALL 包含：头像大图、姓名、学号、邮箱、学院、专业、年级、方向、推荐人姓名、自我介绍。仅待审核状态的 Drawer SHALL 显示操作按钮。

#### Scenario: 打开详情 Drawer
- **WHEN** 管理员点击某张报名卡片
- **THEN** 系统调用 `GET /api/v1/admin/enrollments/{id}` 获取详情，并在右侧 Drawer 中展示完整信息

### Requirement: 通过报名
系统 SHALL 支持管理员对待审核状态的报名执行通过操作。通过操作 SHALL 在卡片行内和 Drawer 内均可触发，点击直接生效无需二次确认。通过后 SHALL 自动刷新列表和统计数据。

#### Scenario: 通过报名成功
- **WHEN** 管理员对待审核报名点击"通过"按钮
- **THEN** 系统调用 `PUT /api/v1/admin/enrollments/{id}/approve`，成功后刷新列表和统计数据，该报名状态变为已通过

#### Scenario: 对非待审核报名执行通过
- **WHEN** 管理员尝试对已通过或已拒绝的报名执行通过
- **THEN** 通过按钮不可见（UI 层面禁止）

### Requirement: 拒绝报名
系统 SHALL 支持管理员对待审核状态的报名执行拒绝操作。点击拒绝按钮 SHALL 弹出 Modal 对话框，包含原因文本框（可选，最多 200 字）和确认按钮。拒绝后 SHALL 自动刷新列表和统计数据。

#### Scenario: 拒绝报名并填写原因
- **WHEN** 管理员点击"拒绝"按钮
- **THEN** 弹出 Modal 对话框，包含原因文本框
- **WHEN** 管理员填写原因并点击确认
- **THEN** 系统调用 `PUT /api/v1/admin/enrollments/{id}/reject` 并传 reason，成功后刷新列表和统计数据

#### Scenario: 拒绝报名不填原因
- **WHEN** 管理员不填写原因直接点击确认
- **THEN** 系统调用拒绝接口（reason 为空），成功后刷新列表和统计数据

### Requirement: 权限控制
报名管理页面 SHALL 仅对 MEMBER 及以上角色（roleLevel >= 1）可见。已在 admin layout 层面控制，页面无需重复校验。

#### Scenario: 角色权限验证
- **WHEN** CANDIDATE 或未登录用户尝试访问 `/admin/enroll`
- **THEN** admin layout 拦截并显示 403 页面
