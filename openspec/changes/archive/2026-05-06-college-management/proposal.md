## Why

后端学院管理 CRUD API 已完整实现（`/api/v1/admin/colleges`），但前端缺少管理页面。管理员无法通过界面进行学院的创建、编辑、删除操作，而现有报名功能依赖学院数据。

## What Changes

- 新增管理端学院 API 服务（`admin-college.service.ts`），对接后端管理端接口
- 在 `type.ts` 中新增创建/更新学院请求类型
- 在管理后台导航菜单中添加学院管理入口
- 新增学院管理页面（`/admin/college`），使用 Table + Drawer 模式
- 新增学院编辑抽屉（`CollegeDrawer`），支持查看、创建、编辑学院

## Capabilities

### New Capabilities

- `college-admin-ui`: 管理端学院管理页面，包含学院列表表格、编辑抽屉、CRUD 操作

### Modified Capabilities

- `frontend-type-definitions`: 在 `type.ts` 中新增学院相关请求类型

## Impact

- **前端页面**：新增 1 个管理页面 + 1 个抽屉组件
- **前端 API**：新增 1 个管理端 API 服务文件
- **前端 DTO**：新增 2 个请求类型定义
- **后端**：无变更，API 已就绪
- **数据库**：无变更
