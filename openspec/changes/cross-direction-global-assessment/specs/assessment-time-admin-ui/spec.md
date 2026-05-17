## MODIFIED Requirements

### Requirement: 新增考核时间（支持全局选项）

系统 SHALL 提供「新增考核时间」按钮，点击后打开 Drawer 表单，包含字段：方向（Select，SUPER_ADMIN 可选"全局"选项）、轮次（InputNumber）、年级（InputNumber，SUPER_ADMIN 可选"不限年级"）、开始时间（DatePicker）、结束时间（DatePicker）、限时开关（Switch）、限时分钟数（InputNumber，限时开启时显示）。

方向选择器 SHALL 对 SUPER_ADMIN 增加"全局"选项（value = `"GLOBAL"`，提交时转换为 null）。SUPER_ADMIN 选择"全局"后，年级字段 SHALL 自动切换为"不限年级"或允许清空。

#### Scenario: SUPER_ADMIN 新增全局考核
- **WHEN** SUPER_ADMIN 在方向选择器中选择"全局"，年级自动变为"不限年级"，填写其他字段后提交
- **THEN** 系统 SHALL 调用 POST `/api/v1/admin/assessment-times`，direction 和 grade 为 null，创建成功

#### Scenario: SUPER_ADMIN 新增指定方向的考核
- **WHEN** SUPER_ADMIN 在方向选择器中选择具体方向（如"计算机视觉"）
- **THEN** 年级字段保持可编辑，行为与原有逻辑一致

#### Scenario: DIRECTION_ADMIN 新增考核
- **WHEN** DIRECTION_ADMIN 点击新增按钮
- **THEN** 方向选择器 SHALL 默认选中自己方向且不可更改，不显示"全局"选项

### Requirement: 表格数据展示格式（全局考核）

表格 SHALL 对以下字段进行格式化展示：
- 方向：显示中文标签（如"计算机视觉"），方向为 null 时显示"全局"标签（蓝色）
- 年级：grade 有值时显示"XX级"（如"2024级"），grade 为 null 时显示"不限"标签
- 时间：格式化为 `YYYY-MM-DD HH:mm`
- 限时：显示"XX 分钟"或"不限时"
- 考核状态：根据当前时间与 startTime/endTime 判断，显示"未开始"/"进行中"/"已结束"标签

#### Scenario: 全局考核的方向显示
- **WHEN** 考核时间的 direction 为 null
- **THEN** 表格该列 SHALL 显示蓝色 Tag 组件，内容为"全局"

#### Scenario: 全局考核的年级显示
- **WHEN** 考核时间的 grade 为 null
- **THEN** 表格该列 SHALL 显示 Tag 组件，内容为"不限"

### Requirement: 角色权限控制（前端，全局考核）

前端 SHALL 根据用户角色控制操作按钮的可见性：
- SUPER_ADMIN：所有方向及全局考核的新增、编辑、删除按钮均可用
- DIRECTION_ADMIN：新增时方向选择器限制为自己方向；全局考核记录的编辑/删除按钮不显示

#### Scenario: SUPER_ADMIN 可操作全局考核
- **WHEN** SUPER_ADMIN 查看全局考核记录
- **THEN** 编辑和删除按钮 SHALL 均显示且可用

#### Scenario: DIRECTION_ADMIN 不可编辑全局考核
- **WHEN** DIRECTION_ADMIN 查看 direction=null 的全局考核记录
- **THEN** 该行的编辑和删除按钮 SHALL 不显示
