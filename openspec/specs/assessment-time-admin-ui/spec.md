## Requirements

### Requirement: 考核时间管理页面
系统 SHALL 在 `/admin/assessment/time` 路径提供考核时间管理页面，包含分页表格、方向/年级筛选和 CRUD 操作。页面使用 Ant Design 组件（Table、Pagination、Button、Form、Drawer、Tag、DatePicker），布局使用 Tailwind CSS。

#### Scenario: 页面正常加载
- **WHEN** SUPER_ADMIN 或 DIRECTION_ADMIN 访问 `/admin/assessment/time`
- **THEN** 系统 SHALL 显示考核时间分页表格，包含列：方向、轮次、年级、开始时间、结束时间、限时、操作

#### Scenario: 方向筛选
- **WHEN** 用户在筛选栏选择特定方向
- **THEN** 表格 SHALL 仅显示该方向的考核时间记录

#### Scenario: 年级筛选
- **WHEN** 用户在筛选栏选择特定年级
- **THEN** 表格 SHALL 仅显示该年级的考核时间记录

### Requirement: 新增考核时间（支持全局选项）
系统 SHALL 提供「新增考核时间」按钮，点击后打开 Drawer 表单，包含字段：方向（Select，SUPER_ADMIN 可选"全局"选项）、轮次（InputNumber）、年级（InputNumber，SUPER_ADMIN 可选"不限年级"）、开始时间（DatePicker）、结束时间（DatePicker）、限时开关（Switch）、限时分钟数（InputNumber，限时开启时显示）。

方向选择器 SHALL 对 SUPER_ADMIN 增加"全局"选项（value = `"GLOBAL"`，提交时转换为 null）。SUPER_ADMIN 选择"全局"后，年级字段 SHALL 自动切换为"不限年级"或允许清空。

#### Scenario: 成功新增
- **WHEN** 管理员填写完整有效数据并提交
- **THEN** 系统 SHALL 调用 POST `/api/v1/admin/assessment-times` 创建记录，刷新列表，显示成功提示

#### Scenario: SUPER_ADMIN 新增全局考核
- **WHEN** SUPER_ADMIN 在方向选择器中选择"全局"，年级自动变为"不限年级"，填写其他字段后提交
- **THEN** 系统 SHALL 调用 POST `/api/v1/admin/assessment-times`，direction 和 grade 为 null，创建成功

#### Scenario: SUPER_ADMIN 新增指定方向的考核
- **WHEN** SUPER_ADMIN 在方向选择器中选择具体方向（如"计算机视觉"）
- **THEN** 年级字段保持可编辑，行为与原有逻辑一致

#### Scenario: DIRECTION_ADMIN 新增考核
- **WHEN** DIRECTION_ADMIN 点击新增按钮
- **THEN** 方向选择器 SHALL 默认选中自己方向且不可更改，不显示"全局"选项

#### Scenario: 表单校验失败
- **WHEN** 管理员提交时必填字段为空或时间逻辑无效（开始时间 >= 结束时间）
- **THEN** 表单 SHALL 显示对应的校验错误提示，不发送请求

#### Scenario: 后端返回唯一约束冲突
- **WHEN** 管理员提交的 direction+epoch+grade 组合已存在
- **THEN** 页面 SHALL 显示后端返回的错误提示（如"该方向轮次年级的考核时间已存在"）

### Requirement: 编辑考核时间
系统 SHALL 在表格操作列或 Drawer 中提供编辑功能。点击编辑后，Drawer 切换为编辑模式，预填充当前数据。

#### Scenario: 成功编辑
- **WHEN** 管理员修改有效字段并提交
- **THEN** 系统 SHALL 调用 PUT `/api/v1/admin/assessment-times/{id}` 更新记录，刷新列表，显示成功提示

#### Scenario: DIRECTION_ADMIN 编辑其他方向被前端禁止
- **WHEN** DIRECTION_ADMIN 用户查看不属于自己方向的考核时间
- **THEN** 操作列的编辑按钮 SHALL 不显示或禁用

### Requirement: 删除考核时间
系统 SHALL 提供删除功能，点击后弹出确认对话框。

#### Scenario: 成功删除
- **WHEN** 管理员确认删除一个无关联题目的考核时间
- **THEN** 系统 SHALL 调用 DELETE `/api/v1/admin/assessment-times/{id}`，刷新列表，显示成功提示

#### Scenario: 删除有关联题目的考核时间
- **WHEN** 管理员确认删除一个存在关联题目的考核时间
- **THEN** 页面 SHALL 显示后端返回的错误提示（如"存在关联的考核题目，需先删除相关题目"）

#### Scenario: DIRECTION_ADMIN 删除其他方向被前端禁止
- **WHEN** DIRECTION_ADMIN 用户查看不属于自己方向的考核时间
- **THEN** 操作列的删除按钮 SHALL 不显示或禁用

### Requirement: 查看考核时间详情
系统 SHALL 支持点击表格行打开 Drawer 查看详情（只读模式），Drawer 内提供「编辑」按钮切换到编辑模式。

#### Scenario: 查看详情
- **WHEN** 用户点击表格某一行
- **THEN** 系统 SHALL 打开 Drawer 显示该考核时间的完整信息（只读模式）

#### Scenario: 从查看切换到编辑
- **WHEN** 用户在查看模式的 Drawer 中点击「编辑」按钮
- **THEN** Drawer SHALL 切换为编辑模式，字段变为可编辑状态

### Requirement: 角色权限控制（前端，全局考核）
前端 SHALL 根据用户角色控制操作按钮的可见性：
- SUPER_ADMIN：所有方向及全局考核的新增、编辑、删除按钮均可用
- DIRECTION_ADMIN：新增时方向选择器限制为自己方向；全局考核记录的编辑/删除按钮不显示

#### Scenario: SUPER_ADMIN 看到完整操作
- **WHEN** SUPER_ADMIN 查看任意方向的考核时间
- **THEN** 编辑和删除按钮 SHALL 均显示且可用

#### Scenario: SUPER_ADMIN 可操作全局考核
- **WHEN** SUPER_ADMIN 查看全局考核记录
- **THEN** 编辑和删除按钮 SHALL 均显示且可用

#### Scenario: DIRECTION_ADMIN 只能操作自己方向
- **WHEN** DIRECTION_ADMIN（direction=COMPUTER_VISION）查看方向为 STRUCTURAL_DESIGN 的考核时间
- **THEN** 该行的编辑和删除按钮 SHALL 不显示

#### Scenario: DIRECTION_ADMIN 不可编辑全局考核
- **WHEN** DIRECTION_ADMIN 查看 direction=null 的全局考核记录
- **THEN** 该行的编辑和删除按钮 SHALL 不显示

#### Scenario: DIRECTION_ADMIN 新增时方向受限
- **WHEN** DIRECTION_ADMIN（direction=COMPUTER_VISION）点击新增按钮
- **THEN** 方向选择器 SHALL 默认选中 COMPUTER_VISION 且不可更改

### Requirement: 表格数据展示格式（全局考核）
表格 SHALL 对以下字段进行格式化展示：
- 方向：显示中文标签（如"计算机视觉"），方向为 null 时显示"全局"标签（蓝色）
- 年级：grade 有值时显示"XX级"（如"2024级"），grade 为 null 时显示"不限"标签
- 时间：格式化为 `YYYY-MM-DD HH:mm`
- 限时：显示"XX 分钟"或"不限时"
- 考核状态：根据当前时间与 startTime/endTime 判断，显示"未开始"/"进行中"/"已结束"标签

#### Scenario: 方向显示中文
- **WHEN** 考核时间的 direction 为 COMPUTER_VISION
- **THEN** 表格该列 SHALL 显示 Tag 组件，内容为"计算机视觉"

#### Scenario: 全局考核的方向显示
- **WHEN** 考核时间的 direction 为 null
- **THEN** 表格该列 SHALL 显示蓝色 Tag 组件，内容为"全局"

#### Scenario: 全局考核的年级显示
- **WHEN** 考核时间的 grade 为 null
- **THEN** 表格该列 SHALL 显示 Tag 组件，内容为"不限"

#### Scenario: 限时显示格式
- **WHEN** 考核时间的 timeLimit 为 true 且 timeLimitMinutes 为 60
- **THEN** 表格该列 SHALL 显示"60 分钟"
- **WHEN** 考核时间的 timeLimit 为 false
- **THEN** 表格该列 SHALL 显示"不限时"

#### Scenario: 考核状态标签
- **WHEN** 当前时间早于 startTime
- **THEN** 状态列 SHALL 显示"未开始"标签（灰色）
- **WHEN** 当前时间在 startTime 和 endTime 之间
- **THEN** 状态列 SHALL 显示"进行中"标签（绿色）
- **WHEN** 当前时间晚于 endTime
- **THEN** 状态列 SHALL 显示"已结束"标签（红色）

### Requirement: 管理端 API Service
系统 SHALL 提供 `admin-assessment-time.service.ts`，封装以下后端接口调用：
- `getList(page, size)` → `GET /api/v1/admin/assessment-times`
- `create(data)` → `POST /api/v1/admin/assessment-times`
- `update(id, data)` → `PUT /api/v1/admin/assessment-times/{id}`
- `delete(id)` → `DELETE /api/v1/admin/assessment-times/{id}`

#### Scenario: 列表请求
- **WHEN** 调用 `getList(0, 20)`
- **THEN** SHALL 发送 GET 请求到 `/api/v1/admin/assessment-times?page=0&size=20`，返回 `ResponseMessage<PageDTO<AssessmentTimeDTO>>`

#### Scenario: 创建请求
- **WHEN** 调用 `create({direction: 'COMPUTER_VISION', epoch: 1, grade: 2025, ...})`
- **THEN** SHALL 发送 POST 请求到 `/api/v1/admin/assessment-times`，携带 CSRF Token
