## Why

管理端侧边栏已配置"报名管理"菜单项（`/admin/enroll`），后端 API 也已完整实现（列表、详情、审批、统计），但前端页面尚未开发。管理员目前无法通过 Web 界面查看报名数据或执行审核操作，需要补齐这个管理界面。

## What Changes

- 新增管理端"报名管理"页面（`/admin/enroll`），包含：
  - 报名统计数字卡片（按状态、按方向的概览数据）
  - 支持按状态、方向筛选和关键词搜索（姓名/学号）的报名卡片列表
  - 点击卡片从右侧弹出 Drawer 查看报名详细信息
  - 行内操作：通过（直接生效）、拒绝（弹出 Modal 填写原因）
  - 分页器（PC 端 3 列卡片、移动端单列）
- 新增前端 Admin 报名 API Service（对接后端已有的 5 个管理端接口）
- 补充前端类型定义（EnrollmentDetailDTO、EnrollmentStatisticsDTO 等管理端专用类型）

## Capabilities

### New Capabilities
- `admin-enrollment-ui`: 管理端报名管理页面，包含统计卡片、筛选搜索、卡片列表、详情 Drawer、审批操作的完整 UI

### Modified Capabilities

（无，后端接口无需修改）

## Impact

- **前端新增文件**：
  - `src/frontend/src/app/admin/enroll/page.tsx` — 报名管理页面
  - `src/frontend/src/apis/services/admin-enroll.service.ts` — 管理 API Service
  - `src/frontend/src/apis/schema/` 中补充管理端类型定义
- **后端无改动**：所有 API 已就绪
- **依赖**：Ant Design 6 组件（Card、Drawer、Modal、Pagination、Select、Input、Tag）、现有 admin layout
