## Context

后端学院管理 API 已完整实现：
- `GET /api/v1/colleges` — 获取所有学院列表（公开接口）
- `POST /api/v1/admin/colleges` — 创建学院（需管理员权限）
- `PUT /api/v1/admin/colleges/{id}` — 更新学院（需管理员权限）
- `DELETE /api/v1/admin/colleges/{id}` — 删除学院（需管理员权限，有关联数据时拒绝删除）

现有技术约束：
- 暗色主题，AntD ConfigProvider 全局配置
- 布局用 Tailwind class
- 权限：SUPER_ADMIN 和 DIRECTION_ADMIN 可访问，参考设备/场地管理权限控制
- 已有类似页面：设备管理、场地管理，采用 Table + Drawer 模式

## Goals / Non-Goals

**Goals:**
- 管理员通过表格查看所有学院列表
- 支持学院的创建、编辑、删除操作
- 删除前检查关联数据，给出友好提示
- 与现有管理页保持一致的 UI/UX

**Non-Goals:**
- 后端 API 变更（API 已就绪，不修改）
- 学院批量操作
- 学院排序/分页（后端 API 未提供分页，一次性加载所有）

## Decisions

### D1: 复用 Table + Drawer 模式

**选择**: 参考设备管理页面实现，使用 Table 展示列表，Drawer 处理查看/编辑/创建

**理由**:
- 与现有管理页保持一致
- 学院数据结构简单（只有 name 字段），无需复杂表单
- Drawer 三种模式：view、edit、create

### D2: 权限控制策略

**选择**: 参考设备管理权限，菜单项 `minLevel` 设为 3（SUPER_ADMIN）

**理由**:
- 学院作为基础参考数据，应由高级管理员管理
- 与设备/场地管理权限保持一致

### D3: 删除确认逻辑

**选择**: 删除前显示确认弹窗，提示学院名称，后端返回错误时前端显示友好信息

**理由**:
- 后端已有校验（有关联用户/报名时拒绝删除）
- 前端需要捕获并显示具体错误原因

## Risks / Trade-offs

- **[无分页性能]** → 学院数量通常较少（几十个），一次性加载影响不大
- **[删除被拒体验]** → 后端返回 400 错误时，前端显示 message.error 提示具体原因
