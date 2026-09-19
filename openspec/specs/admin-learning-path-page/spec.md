# admin-learning-path-page Specification

## Purpose

为学习路径功能提供后台管理页面，使方向管理员无需直接调用 API 即可维护三个方向的学习步骤（序号、标题、相关链接）。
## Requirements
### Requirement: Learning path admin page with direction tabs

系统 SHALL 在 `/admin/learning-path` 提供学习路径管理页面，页面以 Tab 切换三个方向（cv/embed/struct），每个 Tab 内以表格展示该方向的学习步骤列表。表格列 SHALL 为：拖拽把手、步骤序号（由行位置派生）、标题、相关链接、操作。页面 SHALL NOT 提供手工填写步骤序号的入口。

#### Scenario: Page displays three direction tabs

- **WHEN** 管理员访问 `/admin/learning-path`
- **THEN** 页面展示"计算机视觉"、"嵌入式开发"、"结构设计"三个 Tab，默认选中第一个

#### Scenario: Table shows learning steps of selected direction

- **WHEN** 管理员切换到某个方向 Tab
- **THEN** 表格展示该方向全部学习步骤，列为：拖拽把手、步骤序号、标题、相关链接、操作

#### Scenario: Step number column is derived from row position

- **WHEN** 表格渲染某方向的学习步骤
- **THEN** 步骤序号列按行位置显示 `01`、`02`、`03`…（两位补零），取值来自前端行下标而非后端字段

#### Scenario: Step number stays contiguous after deletion

- **WHEN** 管理员删除中间某个步骤后列表刷新
- **THEN** 步骤序号列重新按新顺序显示为连续的 `01`、`02`…，不出现断档

#### Scenario: Drag handle is present on every row

- **WHEN** 表格渲染学习步骤行
- **THEN** 每行首列展示可抓取的拖拽把手图标，指针样式提示可拖拽

---

### Requirement: Create and edit learning step via right-side drawer

系统 SHALL 通过右侧 Drawer 提供学习步骤的新增与编辑表单，表单字段为：标题（必填）、相关链接（可选，URL 格式）。表单 SHALL NOT 包含步骤序号字段。

#### Scenario: Create step successfully

- **WHEN** 管理员点击"新增步骤"，在 Drawer 中填写合法表单并提交
- **THEN** 系统调用创建接口，成功后关闭 Drawer 并刷新当前方向的步骤列表

#### Scenario: Create form has no step number field

- **WHEN** 管理员打开"新增步骤"Drawer
- **THEN** 表单仅展示标题与相关链接两项，不展示步骤序号输入框

#### Scenario: Edit step successfully

- **WHEN** 管理员点击某行的编辑按钮，在 Drawer 中修改并提交
- **THEN** 系统调用更新接口，成功后关闭 Drawer 并刷新列表

#### Scenario: Edit form has no step number field

- **WHEN** 管理员打开某步骤的编辑 Drawer
- **THEN** 表单仅展示标题与相关链接，回填当前值，不展示步骤序号输入框

#### Scenario: Invalid link format rejected

- **WHEN** 管理员填写的相关链接不是合法 URL
- **THEN** 表单校验失败并提示，不发起请求

---

### Requirement: Delete learning step with confirmation

系统 SHALL 在删除学习步骤前要求管理员二次确认。

#### Scenario: Confirm deletion

- **WHEN** 管理员点击删除并在确认弹窗中确认
- **THEN** 系统调用删除接口，成功后刷新列表

#### Scenario: Cancel deletion

- **WHEN** 管理员点击删除但在确认弹窗中取消
- **THEN** 系统不发起任何请求，列表保持不变

---

### Requirement: Admin navigation menu entry

系统 SHALL 在 AdminNav 菜单中注册"学习路线管理"入口，仅对方向管理员（`DIRECTION_ADMIN`）及以上角色可见。

#### Scenario: Direction admin sees menu entry

- **WHEN** 角色为 `DIRECTION_ADMIN` 或 `SUPER_ADMIN` 的用户打开后台菜单
- **THEN** 菜单中显示"学习路线管理"项，点击跳转 `/admin/learning-path`

#### Scenario: Lower roles do not see menu entry

- **WHEN** 角色为 `MEMBER` 或 `CANDIDATE` 的用户打开后台菜单
- **THEN** 菜单中不显示"学习路线管理"项

---

### Requirement: Backend permission enforcement

系统 SHALL 依赖后端 `direction-learning-path:create/update/delete/sort` 权限注解做真实鉴权，前端不做细粒度权限判断。

#### Scenario: Unauthorized request rejected

- **WHEN** 未授权用户的请求到达管理接口
- **THEN** 后端返回 401/403，前端展示错误提示

### Requirement: Drag-and-drop reorder of learning steps

系统 SHALL 允许方向管理员通过拖拽表格行调整该方向学习步骤的展示顺序，并 SHALL 在拖拽结束后把新顺序持久化到后端。拖拽 SHALL 使用与竞赛、资源管理页一致的 `dnd-kit` 实现（`DndContext` + `SortableContext` + 可排序行组件 + `PointerSensor` 带激活距离），并 SHALL 采用乐观更新与失败回滚。

#### Scenario: Drag a row to a new position

- **WHEN** 管理员按住拖拽把手把某行拖到列表中的另一位置并释放
- **THEN** 表格立即按新顺序重排，且系统调用批量排序接口提交该方向全部步骤的新顺序

#### Scenario: Numbers renumber immediately after drop

- **WHEN** 拖拽释放导致行顺序变化
- **THEN** 步骤序号列立即按新位置重新显示连续编号，无需等待服务端响应

#### Scenario: Drop on the same position is a no-op

- **WHEN** 管理员把某行拖回原位置（或拖到自身）
- **THEN** 系统不发起任何请求，列表与序号保持不变

#### Scenario: Reorder failure rolls back

- **WHEN** 批量排序接口返回失败
- **THEN** 表格恢复为拖拽前的顺序与序号，并展示"排序更新失败"错误提示

#### Scenario: Drag does not hijack row interactions

- **WHEN** 管理员点击行内的编辑或删除按钮
- **THEN** 系统执行对应操作，不触发排序请求；拖拽监听只绑定在把手图标上，不绑定整行

#### Scenario: Reorder is scoped to the active direction

- **WHEN** 管理员在某个方向 Tab 内完成拖拽
- **THEN** 提交的排序请求只包含该方向的步骤，其它方向的顺序不受影响

