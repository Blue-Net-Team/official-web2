# 报名表管理与展示

## Why

招新流程要求报名者在面试时携带纸质报名表到实验室，但目前网站上没有分发报名表的渠道——管理员无法上传/更新报名表，报名者也无法在报名页下载。需要在 enroll 报名页提供报名表下载入口，并在 admin 后台提供报名表的管理能力。

## What Changes

- **enroll 报名页**：左侧栏新增「报名表」卡片，内含下载按钮（跳转 `/file/download/{fileId}`）与提示文案「填写完成后请下载打印本报名表，并在面试时带到实验室」；移动端同步展示；无报名表时卡片不渲染。
- **admin 后台**：新增 `/admin/enroll-form` 报名表管理页，展示当前报名表状态（是否已上传、上传时间），提供【更新】（重新上传替换）与【删除】按钮；侧边栏 `menuConfig` 新增「报名表管理」入口（minLevel 2）。
- **后端文件模块**：`FileType` 新增 `ENROLL_FORM("enroll-form", "报名表")` 枚举；下载权限校验将其归入公开分支；孤儿文件清理 SQL 排除该类型（否则当前报名表会被定时任务误删）。
- **后端接口**：新增 3 个端点——公开查询当前报名表（返回 fileId 与更新时间，前端经 `/file/download/{fileId}` 下载）、管理端设置/更新报名表（传已上传确认的 fileId，校验后替换并删除旧文件）、管理端删除报名表。
- **格式约束**：报名表仅允许 pdf / doc / docx（前端 accept 拦截 + 管理端接口领域校验 + 现有魔数检查兜底）。
- **数据模型**：不新增表、不修改表结构、无 migration。「当前报名表」由 `tb_file` 中 `type='enroll-form'` 且 `status='active'` 的最新一条记录表达。

## Capabilities

### New Capabilities

- `enroll-form-management`: 报名表的全生命周期管理——文件类型定义、公开下载权限、当前报名表查询语义、管理端设置/更新/删除接口、上传格式校验，以及 enroll 页下载卡片与 admin 管理页的前端行为。

### Modified Capabilities

- `orphan-file-cleanup`: ACTIVE 文件的孤儿判定需排除 `type='enroll-form'` 的记录——该类型文件不被任何业务表引用（无引用表设计），其生命周期由管理端接口显式维护，不纳入自动清理。

## Impact

- **后端代码**：`FileType` 枚举、`FileDomainServiceImpl.checkDownloadPermission()` 公开分支、`FileRepository`/`FileMapper` 新增 `findLatestByType` 查询、`FileMapper.xml` 的 `selectOrphanFiles` 增加类型排除、新增 `EnrollFormAppService` 与公开/管理两个 Controller。
- **前端代码**：新增 enroll-form API service、`EnrollFormDownloadCard` 组件（挂载于 enroll 页左侧栏及移动端）、`/admin/enroll-form` 管理页、`AdminNav` 菜单配置。
- **API**：新增 `GET /api/v1/enroll-form`（PUBLIC）、`POST /api/v1/admin/enroll-form`（PROTECTED，`admin:enroll-form:update`）、`DELETE /api/v1/admin/enroll-form`（PROTECTED，`admin:enroll-form:delete`）；权限标识需全局唯一。
- **数据库**：无结构变更；仅 mapper XML 查询文本改动。
- **已知取舍**：`tb_file` 仅存生成文件名（`enroll_form-<uuid>.<ext>`），管理页不显示原始文件名，下载到本地的文件名亦为生成名；ENROLL_FORM 类型文件不再被孤儿清理任务兜底，若替换流程异常中断可能残留旧文件（可接受）。
