## Why

管理平台缺少考核时间管理页面，管理员无法通过界面进行考核时间的增删改查操作，只能通过数据库直接操作。同时后端存在权限缺口：DIRECTION_ADMIN 可以修改任意方向的考核时间，缺少方向隔离校验。

## What Changes

- 新增考核时间管理前端页面（`/admin/assessment/time`），提供分页表格、方向/年级筛选、新增/编辑/删除功能
- 新增管理端 API service（`admin-assessment-time.service.ts`），封装已有后端 CRUD 接口
- 新增考核时间抽屉组件（AssessmentTimeDrawer），支持查看/编辑/创建三种模式
- 修复后端权限缺口：DIRECTION_ADMIN 的 create/update/delete 操作增加方向校验，只能操作自己方向的考核时间
- 前端根据用户角色控制操作按钮可见性：DIRECTION_ADMIN 看到其他方向时不显示编辑/删除按钮

## Capabilities

### New Capabilities
- `assessment-time-admin-ui`: 考核时间管理前端页面，包含表格列表、筛选、CRUD 操作和角色权限控制

### Modified Capabilities
- `assessment-time-management`: 后端增加 DIRECTION_ADMIN 方向隔离校验，限制方向管理员只能操作自己方向的考核时间

## Impact

- **前端**：新增 3 个文件（页面、Drawer 组件、API service），修改导航配置
- **后端**：修改 `AssessmentTimeServiceImpl` 的 create/update/delete 方法，增加方向权限校验
- **API**：无新增接口，仅修改已有接口的行为（增加权限校验）
- **导航**：侧边栏已有 `assessment > assessmentTime` 菜单项，无需修改
