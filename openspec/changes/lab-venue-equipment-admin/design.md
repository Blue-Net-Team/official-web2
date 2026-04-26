## Context

后端已完整实现 Venue 和 Equipment 的 Domain、Application、Infrastructure 和 API 层：
- 公开 API：`GET /api/v1/venues`、`GET /api/v1/equipments`
- 管理 API：`POST/PUT/DELETE /api/v1/admin/venues`、`POST/PUT/DELETE /api/v1/admin/equipments`
- 数据库表 `tb_venue`、`tb_equipment` 已就绪

前端公共展示页面 `/lab-environment` 已存在，通过 `venue.service.ts` 和 `equipment.service.ts` 拉取数据。

缺失的是管理员后台的可视化 CRUD 界面。

## Goals / Non-Goals

**Goals:**
- 为管理员提供场地和设备的后台管理页面
- 支持完整的 CRUD 操作（新建、查看、编辑、删除）
- 支持图片上传与替换
- 支持排序调整（拖拽 + 按钮上移/下移）
- 复用现有竞赛管理的 UI 模式，保持一致性

**Non-Goals:**
- 后端 API 改动（已完整）
- 公共展示页面改动（已完整）
- 批量导入/导出功能
- 富文本编辑器（description 使用纯文本 TextArea）

## Decisions

### 1. 复用 Competition Admin 的 UI 模式
- **理由**：竞赛管理页面已实现成熟的 CRUD + 排序模式（Table + Drawer + DnD），直接复用可减少设计不一致和维护成本。
- **替代方案**：独立设计新布局，但会增加学习成本和 review 负担。

### 2. 场地与设备分两个独立页面
- **理由**：虽然两者结构高度相似（都是 name/brand/subtitle/description/image/sortOrder），但属于不同业务概念，且后端已经是独立的 Controller 和 Service。分开展示更直观，权限控制也更灵活。
- **替代方案**：抽象为通用配置页面，但会增加前端复杂度且不符合现有模式。

### 3. 图片使用 FileService 上传 NORMAL_IMG
- **理由**：与现有竞赛封面/Logo 上传方式保持一致，统一走 `POST /api/v1/file/upload` 获取 fileId，再更新到场地/设备。

### 4. 排序通过独立的 `batchUpdateSortOrder` + 单条 `moveUp/moveDown` 实现
- **理由**：竞赛管理已验证此模式。后端 Admin API 已支持 `PUT /api/v1/admin/venues/{id}/sort-order`（equipment 同理），无需额外接口。

## Risks / Trade-offs

- **[Risk]** 两个页面代码高度重复（Table、Drawer 逻辑几乎一致）
  - **Mitigation**：目前保持独立，若未来出现更多同类配置项（如方向介绍），再考虑抽象为通用 ConfigAdmin 组件。
- **[Risk]** Admin 导航菜单入口增加导致菜单项过多
  - **Mitigation**：归入“内容管理”分组下，与竞赛管理等并列。

## Migration Plan

无需数据迁移。本次变更纯前端新增文件，不影响现有数据或 API。

## Open Questions

- 无
