## Why

实验室场地与设备管理功能的后端 API 和公共展示页面已就绪，但管理员缺少可视化的后台管理界面。管理员目前无法通过前端界面增删改查场地和设备信息，也无法调整展示顺序，导致内容维护完全依赖数据库操作。

## What Changes

- 新增场地管理后台页面 (`/admin/venue`)，支持：
  - 分页查看场地列表
  - 新建、编辑、删除场地
  - 上传/更新场地图片
  - 拖拽/按钮调整排序
- 新增设备管理后台页面 (`/admin/equipment`)，支持：
  - 分页查看设备列表
  - 新建、编辑、删除设备
  - 上传/更新设备图片
  - 拖拽/按钮调整排序
- 新增 Admin API Service：`admin-venue.service.ts`、`admin-equipment.service.ts`
- 在 admin 导航菜单中注册两个新入口

## Capabilities

### New Capabilities
- `venue-admin`: 场地后台管理（CRUD + 排序 + 图片上传）
- `equipment-admin`: 设备后台管理（CRUD + 排序 + 图片上传）

### Modified Capabilities
- 无现有规范变更需求

## Impact

- **前端代码**：新增 4 个页面/组件文件 + 2 个 Service 文件
- **导航菜单**：`src/frontend/src/components/admin/AdminLayout.tsx` 需新增菜单项
- **依赖**：复用现有 `@dnd-kit/core`、`@dnd-kit/sortable` 拖拽排序能力
- **后端**：无需改动，已提供完整 Admin API
