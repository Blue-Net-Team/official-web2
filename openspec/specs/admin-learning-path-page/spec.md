# admin-learning-path-page Specification

## Purpose

为学习路径功能提供后台管理页面，使方向管理员无需直接调用 API 即可维护三个方向的学习步骤（序号、标题、相关链接）。

## Requirements

### Requirement: Learning path admin page with direction tabs

系统 SHALL 在 `/admin/learning-path` 提供学习路径管理页面，页面以 Tab 切换三个方向（cv/embed/struct），每个 Tab 内以表格展示该方向的学习步骤列表。

#### Scenario: Page displays three direction tabs

- **WHEN** 管理员访问 `/admin/learning-path`
- **THEN** 页面展示"计算机视觉"、"嵌入式开发"、"结构设计"三个 Tab，默认选中第一个

#### Scenario: Table shows learning steps of selected direction

- **WHEN** 管理员切换到某个方向 Tab
- **THEN** 表格展示该方向全部学习步骤，列为：步骤序号、标题、相关链接、操作

---

### Requirement: Create and edit learning step via right-side drawer

系统 SHALL 通过右侧 Drawer 提供学习步骤的新增与编辑表单，表单字段为：步骤序号（必填，≥1）、标题（必填）、相关链接（可选，URL 格式）。

#### Scenario: Create step successfully

- **WHEN** 管理员点击"新增步骤"，在 Drawer 中填写合法表单并提交
- **THEN** 系统调用创建接口，成功后关闭 Drawer 并刷新当前方向的步骤列表

#### Scenario: Edit step successfully

- **WHEN** 管理员点击某行的编辑按钮，在 Drawer 中修改并提交
- **THEN** 系统调用更新接口，成功后关闭 Drawer 并刷新列表

#### Scenario: Duplicate step number rejected

- **WHEN** 管理员提交的步骤序号在该方向下已存在
- **THEN** 系统展示后端返回的错误提示（如"该方向的步骤序号已存在"），Drawer 保持打开

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

系统 SHALL 依赖后端 `direction-learning-path:create/update/delete` 权限注解做真实鉴权，前端不做细粒度权限判断。

#### Scenario: Unauthorized request rejected

- **WHEN** 未授权用户的请求到达管理接口
- **THEN** 后端返回 401/403，前端展示错误提示
