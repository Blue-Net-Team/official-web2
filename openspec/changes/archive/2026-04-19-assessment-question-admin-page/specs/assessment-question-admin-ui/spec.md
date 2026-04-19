## ADDED Requirements

### Requirement: Steps 引导筛选考题范围
管理页面 SHALL 使用 AntD Steps 组件引导管理员依次选择方向和考核时间，选定后展示考题管理表格。

#### Scenario: 初始进入页面
- **WHEN** 管理员访问 `/admin/assessment/question`
- **THEN** 显示 Steps 组件，当前步骤为 Step 1（选择方向），展示方向选择列表

#### Scenario: 选择方向后进入 Step 2
- **WHEN** 管理员在 Step 1 选择一个方向
- **THEN** Steps 前进到 Step 2（选择考核时间），加载该方向下的考核时间列表

#### Scenario: 选择考核时间后进入 Step 3
- **WHEN** 管理员在 Step 2 选择一个考核时间
- **THEN** Steps 前进到 Step 3（管理考题），加载该考核时间下的考题表格

#### Scenario: Steps 返回上一步
- **WHEN** 管理员点击 Steps 的上一步或点击已完成的 Step
- **THEN** 返回对应步骤，保留之前的选择状态

#### Scenario: DIRECTION_ADMIN 权限限制
- **WHEN** DIRECTION_ADMIN 访问页面
- **THEN** Step 1 自动选中其方向并跳到 Step 2，不允许选择其他方向

### Requirement: 考题列表展示
Step 3 SHALL 展示考题表格，包含题号、题型、标题、分值、附件、操作列。

#### Scenario: 考题表格展示
- **WHEN** 进入 Step 3
- **THEN** 表格展示当前考核时间下的所有考题，按题号排序，列包含：题号、题型（Tag）、标题、分值、附件标识、操作按钮

#### Scenario: 无考题时的空状态
- **WHEN** 当前考核时间下无考题
- **THEN** 表格显示空状态提示"暂无考题，点击新增添加"

#### Scenario: DIRECTION_ADMIN 操作限制
- **WHEN** DIRECTION_ADMIN 查看非自己方向的考核时间下的考题
- **THEN** 仅显示查看权限，不显示新增/编辑/删除按钮

### Requirement: 新增考题
管理员 SHALL 能通过 Drawer 表单新增考题，表单根据题型动态渲染不同的内容编辑区域。

#### Scenario: 打开新增抽屉
- **WHEN** 管理员点击"新增考题"按钮
- **THEN** 打开 Drawer，表单包含：题号、题型选择、标题、分值、附件上传、题型对应的内容编辑区

#### Scenario: 文件上传题型表单
- **WHEN** 管理员选择题型为"文件上传"
- **THEN** 内容编辑区仅显示一个 TextArea 用于输入题干

#### Scenario: 单选题题型表单
- **WHEN** 管理员选择题型为"单选题"
- **THEN** 内容编辑区显示：题干 TextArea、选项列表（Form.List 动态增删）、每个选项前有 Radio 按钮选择正确答案

#### Scenario: 多选题题型表单
- **WHEN** 管理员选择题型为"多选题"
- **THEN** 内容编辑区显示：题干 TextArea、选项列表（Form.List 动态增删）、每个选项前有 Checkbox 选择正确答案

#### Scenario: 算法题型表单
- **WHEN** 管理员选择题型为"算法题"
- **THEN** 内容编辑区显示：题干 TextArea、时间限制 InputNumber、内存限制 InputNumber、测试用例列表（Form.List 动态增删，每条含输入和期望输出）

#### Scenario: 提交新增考题
- **WHEN** 管理员填写完表单并点击"创建"
- **THEN** 系统调用 `POST /admin/assessment-questions`，成功后关闭 Drawer 并刷新列表

#### Scenario: 表单校验失败
- **WHEN** 管理员提交时缺少必填字段
- **THEN** 表单显示校验错误提示，不发送请求

### Requirement: 编辑考题
管理员 SHALL 能通过 Drawer 表单编辑已有考题，表单预填充现有数据。

#### Scenario: 打开编辑抽屉
- **WHEN** 管理员点击考题的编辑按钮
- **THEN** 打开 Drawer，表单预填充该考题的现有数据，内容编辑区根据题型自动渲染

#### Scenario: 提交编辑考题
- **WHEN** 管理员修改字段后点击"保存"
- **THEN** 系统调用 `PUT /admin/assessment-questions/{id}`，成功后关闭 Drawer 并刷新列表

#### Scenario: 切换题型时清空内容
- **WHEN** 管理员在编辑模式下切换题型
- **THEN** 内容区域清空并重新渲染为新题型对应的表单

### Requirement: 删除考题
管理员 SHALL 能删除考题，删除前需确认。

#### Scenario: 删除确认弹窗
- **WHEN** 管理员点击考题的删除按钮
- **THEN** 弹出确认 Modal，显示"确认删除考题「{标题}」？此操作不可撤销"

#### Scenario: 确认删除
- **WHEN** 管理员在确认弹窗中点击"确认删除"
- **THEN** 系统调用 `DELETE /admin/assessment-questions/{id}`，成功后刷新列表

#### Scenario: 删除失败
- **WHEN** 后端返回删除失败
- **THEN** 显示错误提示信息

### Requirement: 查看考题详情
管理员 SHALL 能通过 Drawer 查看考题详情（只读模式）。

#### Scenario: 点击行查看详情
- **WHEN** 管理员点击表格中的一行
- **THEN** 打开 Drawer 只读模式，展示考题所有信息，底部有"编辑"和"删除"按钮

### Requirement: 考题附件管理
管理员 SHALL 能为考题上传/更换附件。

#### Scenario: 上传附件
- **WHEN** 管理员在表单中上传文件
- **THEN** 文件通过 `POST /file/upload?type=ASSESSMENT_ATTACHMENT` 上传，返回的 fileId 作为 attachmentId

#### Scenario: 更换附件
- **WHEN** 管理员在编辑模式下重新上传文件
- **THEN** 新的 fileId 替换原有的 attachmentId

### Requirement: 权限控制
管理页面 SHALL 根据用户角色限制操作权限。

#### Scenario: SUPER_ADMIN 全部权限
- **WHEN** SUPER_ADMIN 访问考题管理页面
- **THEN** 可以选择所有方向、管理所有考核时间的考题

#### Scenario: DIRECTION_ADMIN 方向限制
- **WHEN** DIRECTION_ADMIN 访问考题管理页面
- **THEN** Step 1 自动选中其方向，只能管理该方向考核时间下的考题
