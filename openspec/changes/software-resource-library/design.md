## Context

BlueNet 现有方向体系（计算机视觉、结构设计、嵌入式开发）已有一个 `Direction` 枚举和按方向筛选的公开页面模式。文件服务仅用于实际文件上传/下载，而本次需求明确只要外部链接清单，因此无需复用 `FileAppService` 或对象存储。现有 RBAC 权限模型（`@RequiresPermission` + `RoleType`）已经支持按角色控制管理接口。

## Goals / Non-Goals

**Goals:**
- 提供一个公开页面 `/resources`，按方向展示可维护的软件资源链接。
- 提供后台管理页面 `/admin/resources`，`MEMBER` 及以上角色可管理资源。
- 保持实现轻量，不引入文件存储、下载统计或版本控制。
- 遵循项目 DDD 分层、权限扫描、分页响应等既有约束。

**Non-Goals:**
- 不托管软件安装包，不走对象存储上传/下载。
- 不做下载量统计、用户收藏、评分评论。
- 不做版本管理、多语言、富文本详情。
- 不添加 `created_at` / `updated_at` 字段。

## Decisions

### 1. 使用独立聚合 `SoftwareResource`，不复用 `File` 聚合
**Rationale**: `File` 聚合围绕二进制文件上传/下载、对象存储、状态流转设计；本次需求是外部链接元数据，两者生命周期和校验规则不同。独立聚合更清晰，也避免污染文件权限逻辑。

### 2. 方向字段复用现有 `Direction` 枚举，并新增 `GENERAL`
**Rationale**: 与现有方向体系保持一致，便于前端复用 slug 和显示名称。`GENERAL` 表示不绑定任何方向的通用软件。

### 3. 公开列表直接查询数据库，不启用 Redis 缓存
**Rationale**: 资源数据量小、更新频率低，首次请求即可快速返回；后续如访问量增大，可低成本增加缓存，当前不增加复杂度。

### 4. 后台管理接口权限值采用 `software-resource:*` 命名空间
**Rationale**: 命名空间化便于避免与其他模块权限冲突，同时 `PermissionScanner` 启动时会校验全局唯一性。

### 5. 前端 Tab 与分页
**Rationale**: 公开页采用 Tab + 分页，和项目现有竞赛、成就等列表页风格一致；管理页使用 Table + Modal 表单。

### 6. `status` 使用 `ACTIVE` / `DISABLED` 枚举
**Rationale**: 明确区分启用与禁用，禁用资源对公众隐藏但仍可后台编辑/重新启用。不使用软删除字段（符合项目不软删除的约定），删除即物理删除。

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| 外部链接失效无法自动感知 | 在管理页显示链接，成员定期人工巡检；后续可扩展定时健康检查。 |
| 权限值命名冲突导致启动失败 | 统一使用 `software-resource:` 前缀，创建前全局搜索确认无重复。 |
| 资源增多后公开页渲染变慢 | 当前数据量小；必要时对数据库查询加索引并启用 ISR 缓存。 |
| 方向枚举新增 `GENERAL` 影响现有逻辑 | `GENERAL` 仅用于资源库，其他模块查询时默认忽略该值，不影响既有方向页面。 |

## Migration Plan

1. 创建 `tb_software_resource` 表（Flyway / 手动 SQL，根据项目实际迁移方式）。
2. 启动应用，`PermissionScanner` 校验新权限值。
3. 部署前后端代码。
4. 管理员登录后台，手动录入首批资源。
5. 公开页 `/resources` 自动生效。

## Open Questions

- 是否需要首批数据脚本（种子数据）？
- 管理页是否需要在删除前进行二次确认？
- 是否需要对 `external_url` 做格式校验（如必须以 `http://` 或 `https://` 开头）？
