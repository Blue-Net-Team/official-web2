## Context

管理平台已有报名管理、竞赛管理、权限管理等页面，采用 Ant Design 组件 + Tailwind 布局。后端考核时间 CRUD 接口已完整，但存在两个问题：

1. **前端缺失**：侧边栏已有考核时间菜单项（`/admin/assessment/time`，minLevel=2），但对应页面尚未实现
2. **权限缺口**：DIRECTION_ADMIN 的 create/update/delete 操作没有方向隔离校验，可以操作任意方向的考核时间

### 现有模式参考

- **竞赛管理页**（`/admin/competition`）：Table + Drawer 模式，支持排序、分页、CRUD
- **报名管理页**（`/admin/enroll`）：Card + Drawer 模式
- 考核时间数据结构较规整（表格友好），适合采用竞赛管理页的 Table + Drawer 模式

## Goals / Non-Goals

**Goals:**
- 提供考核时间的完整管理界面（列表、筛选、新增、编辑、删除）
- 修复后端 DIRECTION_ADMIN 方向隔离权限
- 前端根据用户角色控制操作按钮可见性
- 遵循现有管理页面的组件模式和样式风格

**Non-Goals:**
- 不涉及考题管理、答案评分等其他考核功能
- 不修改后端 DTO 结构或新增 API 接口
- 不处理考核会话（session）管理
- 不实现考核时间的批量导入/导出

## Decisions

### 1. 页面布局：Table + Drawer 模式

**选择**：使用 Ant Design Table（分页）+ Drawer（新增/编辑/查看），参考竞赛管理页模式

**原因**：
- 考核时间数据结构规整（方向、轮次、年级、时间、限时），表格展示最清晰
- Drawer 模式与现有管理页面一致，用户体验统一
- 相比 Modal，Drawer 在表单字段较多时空间更充裕

**替代方案**：
- Card 布局：适合信息密度低的场景（如报名管理），考核时间数据适合表格
- 全新页面跳转：增加路由复杂度，当前字段数量不需要单独页面

### 2. 前端筛选：客户端筛选 vs 后端筛选

**选择**：后端不支持 direction/grade 筛选参数，前端获取全量数据后客户端筛选

**原因**：
- 后端 admin list API 只接受 page/size 参数，无 direction/grade 过滤
- DIRECTION_ADMIN 及以上看到的是全量数据，数量可控
- 客户端筛选实现简单，避免修改后端 API

### 3. 权限控制策略：前后端双重校验

**选择**：
- 后端：在 Application 层 create/update/delete 方法中增加 DIRECTION_ADMIN 方向校验
- 前端：根据 `userInfo.direction` 和当前行的 `direction` 判断是否显示编辑/删除按钮

**原因**：
- 后端校验是安全底线，防止绕过前端直接调 API
- 前端隐藏按钮是用户体验优化，避免用户点击后才看到权限错误

### 4. 组件技术选型

**选择**：
- 表格、分页、按钮、表单、Drawer、Tag、DatePicker → Ant Design
- 布局（flex、padding、margin）→ Tailwind CSS（不使用 Ant Design Layout/Flex）

**原因**：与用户要求一致，Ant Design 用于功能性组件，布局保持 Tailwind

## Risks / Trade-offs

- **[客户端筛选性能]** → 考核时间数据量有限（每方向每年级几轮），全量加载后客户端筛选性能可接受
- **[方向校验时机]** → 后端校验放在 Application 层而非 Domain 层，因为需要访问 UserCTX 获取当前用户方向；这是合理的分层选择，权限属于应用层关注点
- **[导航菜单无需修改]** → 侧边栏已有 `assessment > assessmentTime` 菜单项（minLevel=2），只需实现对应页面
