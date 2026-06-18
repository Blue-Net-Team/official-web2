## Why

新生入学后需要快速获取各方向常用的学习软件与工具链接。目前这些资源分散在群公告或口头传递中，容易遗漏且不便查找。一个公开、按方向分类、可维护的软件资源库能显著提升 onboarding 效率。

## What Changes

- 新增公开页面 `/resources`，以 Tab 形式展示「全部 / 通用 / 计算机视觉 / 结构设计 / 嵌入式开发」的软件资源。
- 新增后台管理页面 `/admin/resources`，支持成员及以上角色对资源进行增删改、排序和启用/禁用。
- 新增后端模块 `software-resource`：领域模型、数据库表 `tb_software_resource`、RESTful API。
- 资源仅存储外部下载链接，不走文件上传/下载服务，不产生 OSS 流量与存储成本。
- 新增全局唯一权限标识，用于管理后台接口的访问控制。

## Capabilities

### New Capabilities

- `software-resource-library`: 公开软件资源库，包含按方向筛选的列表展示、后台 CRUD、排序与状态管理。

### Modified Capabilities

- 无现有能力需求变更。

## Impact

- 后端：新增 `tb_software_resource` 表、`SoftwareResource` 聚合、`SoftwareResourceController`（公开）与 `AdminSoftwareResourceController`（管理）。
- 前端：新增 `/resources` 公开页面与 `/admin/resources` 管理页面，更新 `AdminNav` 菜单。
- 权限：新增 `software-resource:create/update/delete/list` 等权限值，需确保全局唯一。
- 存储：仅保存文本链接，不引入对象存储成本。
