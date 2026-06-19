## 1. 数据库与领域层（TDD 第一步）

- [x] 1.1 在 `Direction` 枚举中新增 `GENERAL`（通用）。
- [x] 1.2 创建 `tb_software_resource` 建表 SQL（无 `created_at` / `updated_at`）。
- [x] 1.3 创建 `SoftwareResource` 领域实体，包含 `create` / `reconstruct` 工厂方法及基础校验。
- [x] 1.4 创建 `SoftwareResourceRepository` 接口（`findById`、`listActiveByDirection`、`save`、`update`、`delete`、`listAllForAdmin`）。
- [x] 1.5 编写 `SoftwareResource` 单元测试：创建校验、状态切换、字段更新。

## 2. 应用层与基础设施

- [x] 2.1 创建 `SoftwareResourceDO` 与 `SoftwareResourceMapper`。
- [x] 2.2 实现 `SoftwareResourceRepositoryImpl` 并完成 DO/实体转换。
- [x] 2.3 创建 `SoftwareResourceAppService` / `AdminSoftwareResourceAppService` 接口及实现。
- [x] 2.4 编写应用层单元测试：列表查询、创建、更新、删除、排序。
- [x] 2.5 集成测试：Mapper CRUD、Repository 持久化。

## 3. API 层

- [x] 3.1 创建公开 DTO（`SoftwareResourceDTO`、`SoftwareResourceListRequestDTO`）。
- [x] 3.2 创建管理 DTO（`CreateSoftwareResourceRequestDTO`、`UpdateSoftwareResourceRequestDTO`）。
- [x] 3.3 创建 Request/Response Converter。
- [x] 3.4 实现公开 `SoftwareResourceController`：
  - `GET /api/v1/software-resources?direction=...`（分页，公开权限）。
- [x] 3.5 实现管理 `AdminSoftwareResourceController`：
  - `POST /api/v1/admin/software-resources`（`software-resource:create`）。
  - `PUT /api/v1/admin/software-resources/{id}`（`software-resource:update`）。
  - `DELETE /api/v1/admin/software-resources/{id}`（`software-resource:delete`）。
  - `GET /api/v1/admin/software-resources`（`software-resource:admin-list`，分页）。
- [x] 3.6 确保所有 `@RequiresPermission` 值全局唯一，启动 `PermissionScanner` 不报错。
- [x] 3.7 编写 Controller 集成测试。

## 4. 前端公开页

- [x] 4.1 创建 `/resources` 页面，导出 `revalidate = 3600`。
- [x] 4.2 实现 Tab 切换：全部 / 通用 / 计算机视觉 / 结构设计 / 嵌入式开发。
- [x] 4.3 对接 `softwareResourceService.list(direction, page, size)` API。
- [x] 4.4 资源卡片展示：名称、分类、描述、点击跳转外部链接。
- [x] 4.5 空状态与加载态处理。

## 5. 前端管理页

- [x] 5.1 在 `AdminNav` 中新增「资源库管理」菜单项（`minLevel: 2`，对应 `MEMBER`）。
- [x] 5.2 创建 `/admin/resources` 页面。
- [x] 5.3 实现资源列表 Table（展示名称、方向、分类、状态、排序）。
- [x] 5.4 实现新增/编辑 Modal 表单。
- [x] 5.5 实现删除确认与启用/禁用切换。
- [x] 5.6 对接创建、更新、删除、列表 API。

## 6. 验证与收尾

- [x] 6.1 后端编译打包通过（`mvnw clean compile package`）。
- [x] 6.2 Docker 镜像构建并启动（`bluenet-api-service:latest`）。
- [x] 6.3 启动基础设施（PostgreSQL、Redis 等）。
- [x] 6.4 Playwright 端到端验证：公开页 Tab 切换、后台 CRUD 流程。
- [x] 6.5 更新相关文档（数据库设计、接口总览）。
- [x] 6.6 提交代码并按规范填写 commit message。
