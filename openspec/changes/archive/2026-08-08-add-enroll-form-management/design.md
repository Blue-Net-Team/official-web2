# 报名表管理与展示 — 技术设计

## Context

招新季需要向报名者分发纸质报名表：管理员上传/替换报名表文件，报名者在 enroll 报名页下载、打印并带到面试现场。

现有基础设施：

- **预签名直传链路**（`prepare-upload` → 前端直传 MinIO → `confirm-upload`）：confirm 时做 MD5 + 大小 + 魔数三重校验，文件状态机为 `PENDING → ACTIVE / REJECTED`。
- **文件状态机与孤儿清理任务配套**：`OrphanFileCleanupJob` 的 `selectOrphanFiles` SQL 会删除「ACTIVE 且不被任何业务表引用」的文件（`FileMapper.xml:20`）。
- **下载权限分发**：`FileDomainServiceImpl.checkDownloadPermission()` 按 FileType switch 分发，`NORMAL_IMG, QRCODE` 为公开空分支，`default` 抛 `Forbidden`——新增枚举不显式归类则下载必 403。
- **同构先例**：咨询群二维码（`qrcode-management`）是"管理员上传文件 → 公开页展示"的完整先例，但其业务语义（多张、按类型区分）需要 `tb_qrcode` 引用表。

约束：不新增表、无 migration；权限标识全局唯一；管理端入口 minLevel 2；仅允许 pdf/doc/docx。

## Goals / Non-Goals

**Goals:**

- 公开接口返回当前报名表 fileId，enroll 页提供下载按钮与提示文案（桌面 + 移动端）。
- 管理端可设置/更新/删除报名表，admin 侧边栏新增入口。
- ENROLL_FORM 文件公开可下载，且不被孤儿清理任务误删。
- 报名表格式限定 pdf/doc/docx。

**Non-Goals:**

- 不新建业务表（如 `tb_enroll_form`），不修改任何表结构。
- 不保存/展示原始文件名（`tb_file.name` 为生成名；如需展示原始名，后续单独立项加字段）。
- 不支持多张报名表或按方向区分报名表（单张全局表）。
- 不改动预签名上传流程本身。

## Decisions

### D1: 无引用表，「当前报名表」由查询表达

**决策**：`tb_file` 中 `type='enroll-form' AND status='active'` 的最新一条（id 降序）即为当前报名表，通过新增的 `FileRepository.findLatestByType(FileType)` 查询获取。

**理由**：需求是单张全局表，引用表的唯一价值是表达"哪张是当前表"，而 id 最大天然表达这一点。省去 DDD 全套（entity/repository/DO/mapper/converter）。

**备选**（被否）：新建 `tb_enroll_form` 单行表存 fileId。优点：引用关系显式、孤儿清理无需改动、文件生命周期自动兜底。缺点：相对本需求过重。**若未来需要多表/按方向分表，应迁移到此方案**。

### D2: 孤儿清理 SQL 加类型排除

**决策**：`selectOrphanFiles` 的 ACTIVE 分支增加 `AND f.type != 'enroll-form'`。

**理由**：D1 的必然推论——该类型没有任何业务表引用，不排除则当前报名表会被定时任务删除（数据库记录 + MinIO 对象）。

**配套责任**：ENROLL_FORM 文件的删除完全由管理端接口负责（替换时删旧、删除接口删当前）。这是本设计最重要的隐性约定，必须有集成测试守护。

### D3: 管理端接口的替换顺序——先验新，后删旧

**决策**：`setEnrollForm(fileId)` 在事务内严格按序执行：

1. 取文件并校验：存在 → type=ENROLL_FORM → status=ACTIVE → 扩展名 ∈ {pdf, doc, docx}；任一失败即抛异常，旧表不动；
2. 查当前最新 ENROLL_FORM 文件，存在且 id 不同 → `fileRepository.deleteFileById()`（连记录带对象一起删）；
3. 同 id 重复设置 → 幂等成功，不删。

**理由**：反序（先删旧再验新）会在新文件校验失败时让网站处于无表状态。

### D4: 格式校验三层分配

| 层 | 手段 | 现状/新增 |
|---|---|---|
| 前端 | `Upload` 组件 `accept=".pdf,.doc,.docx"` | 新增 |
| confirm-upload | 魔数检查（PDF `%PDF`、docx 即 ZIP `PK` 均已有规则；doc 走 default 放行） | 现有 |
| 设置接口 | 领域校验扩展名 ∈ {pdf, doc, docx} | 新增 |

**说明**：`prepare-upload` 不按 FileType 限制扩展名（现状），因此扩展名校验放在设置接口的领域逻辑中，作为最后一道闸。

### D5: 下载权限归入公开分支

**决策**：`checkDownloadPermission()` 的 `NORMAL_IMG, QRCODE` 空分支加入 `ENROLL_FORM`。

**理由**：报名表面向未登录访客分发；且 `default` 分支会抛 `Forbidden`，新枚举必须显式归类，这是编译器发现不了的必改点。

### D6: 接口形态

| 端点 | 权限 | 语义 |
|---|---|---|
| `GET /api/v1/enroll-form` | PUBLIC | 返回 `{fileId, createdAt}`（上传时间，取自 `tb_file.created_at`）；无表时 200 + `data: null`（不用 404，前端按"空态不渲染"处理，与 ConsultationQrcode 模式一致） |
| `POST /api/v1/admin/enroll-form?fileId=` | PROTECTED `admin:enroll-form:update` | 设置/替换 |
| `DELETE /api/v1/admin/enroll-form` | PROTECTED `admin:enroll-form:delete` | 删除当前表，无表时 404 |

- 管理端页面复用公开查询接口获取当前状态，不单开 admin 查询端点。
- 应用层新建轻量 `EnrollFormAppService`（不放 FileAppService——这是报名业务语义；不塞进 EnrollAppService——那是报名单审核）。
- 权限值 `admin:enroll-form:*` 需确认与现有值不重复（PermissionScanner 启动校验）。

### D7: 前端形态

- **enroll 页**：新组件 `EnrollFormDownloadCard`，桌面端挂左侧栏 `DirectionSidebar` 与 `ConsultationQrcode` 之间；移动端参照 `ConsultationQrcode` 的 `max-lg` 模式在表单下方单独渲染一份。接口返回 null → `return null` 不渲染。下载用 `<a href="/file/download/{fileId}">` 让后端 302 到预签名 URL。
- **admin 页**：`/admin/enroll-form`，状态区（是否已上传 + 上传时间）+【更新】（Upload.Dragger → 预签名三步 → set 接口）+【删除】（确认弹窗 → delete 接口）。上传复用二维码管理页的现成模式。
- **侧边栏**：`menuConfig` 加 `key: 'enrollForm'`，label「报名表管理」，path `/admin/enroll-form`，`minLevel: 2`，图标复用已导入的 `FileTextOutlined`，位置紧邻「报名管理」。

## Risks / Trade-offs

- **[孤儿清理不再兜底 ENROLL_FORM]**：替换流程若在「新文件生效前、旧文件删除后」之间异常中断（事务回滚则不会；进程崩溃于 MinIO 删除阶段则可能），残留文件永不清理。→ 缓解：D3 的顺序设计使窗口极小；残留文件不影响功能（查询只取最新），可人工清理。
- **[并发替换]**：两管理员同时替换，`id DESC LIMIT 1` 取最新生效，被顶掉的文件可能残留。→ 已接受，语义上"最后设置者生效"。
- **[下载文件名为生成名]**：用户下载到的是 `enroll_form-<uuid>.pdf` 而非「蓝网报名表.pdf」。→ 已接受；后续可加 `original_name` 字段优化。
- **[`.doc` 魔数放行]**：老格式 doc（OLE2）不在 FileMagicChecker 列表，改名伪装可绕过魔数但仍被扩展名校验限制为文档类。→ 风险可接受（管理端上传，操作者可信）。
- **[pending 泄漏]**：管理员上传后未调 set 接口的文件 75 分钟后被现有清理任务删除。→ 现有机制天然处理，无需额外工作。

## Migration Plan

无数据库结构变更。部署顺序无特殊要求：后端先行（前端无表时不渲染卡片，向后兼容）。回滚 = 还原代码；ENROLL_FORM 文件在回滚后会被旧版孤儿清理 SQL 删除，重新上传即可。

## Open Questions

- 无（需求边界、格式约束、入口等级、按钮形态均已与需求方确认）。
