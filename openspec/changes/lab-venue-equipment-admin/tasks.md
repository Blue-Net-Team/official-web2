## 1. Admin API Services

- [x] 1.1 创建 `src/frontend/src/apis/services/admin-venue.service.ts`（CRUD + 图片更新）
- [x] 1.2 创建 `src/frontend/src/apis/services/admin-equipment.service.ts`（CRUD + 图片更新）

## 2. Venue 管理页面

- [x] 2.1 创建 `src/frontend/src/app/admin/venue/page.tsx`（列表 + 删除）
- [x] 2.2 创建 `src/frontend/src/app/admin/venue/VenueDrawer.tsx`（新建/编辑/查看 + 图片上传 + sortOrder）

## 3. Equipment 管理页面

- [x] 3.1 创建 `src/frontend/src/app/admin/equipment/page.tsx`（列表 + 删除）
- [x] 3.2 创建 `src/frontend/src/app/admin/equipment/EquipmentDrawer.tsx`（新建/编辑/查看 + 图片上传 + sortOrder）

## 4. 导航与类型

- [x] 4.1 在 `AdminLayout` 导航菜单中添加"场地管理"和"设备管理"入口
- [x] 4.2 确认 TypeScript 类型定义完整（添加 `CreateVenueRequestDTO`、`UpdateVenueRequestDTO`、`CreateEquipmentRequestDTO`、`UpdateEquipmentRequestDTO`）

## 5. 验证与收尾

- [x] 5.1 运行 `pnpm lint` 检查前端代码规范（通过，无新增报错）
- [x] 5.2 本地代码审查完成，结构与现有竞赛管理模式一致
