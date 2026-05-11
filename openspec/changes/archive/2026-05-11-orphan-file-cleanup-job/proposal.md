## Why

目前项目中文件上传采用预签名直传模式，用户在 `prepare-upload` 后若未调用 `confirm-upload`，或上传校验失败，`tb_file` 表中会残留 `PENDING` 和 `REJECTED` 状态的记录。此外，业务数据更新（如更换头像）可能导致旧的 `ACTIVE` 文件不再被任何业务表引用，形成孤儿文件。这些孤儿记录长期堆积，占用数据库和对象存储空间，需要自动清理机制。

## What Changes

- 为 `tb_file` 表新增 `created_at` 字段（Flyway 迁移），并在 `FileDO`、`File` 领域实体中同步添加
- 新增 `OrphanFileCleanupJob` 定时任务类，使用 Spring `@Scheduled` 每天凌晨 2:00 执行
- 清理三类孤儿文件：
  - `PENDING` 超时：`created_at` 超过预签名有效期（15 分钟）+ 缓冲后仍未确认的文件
  - `REJECTED` 残留：上传校验失败后残留的记录
  - `ACTIVE` 无引用：未被任何业务表引用的已激活文件
- 引用检查覆盖 12 个业务表字段（`tb_user`、`tb_enroll`、`tb_achievement`、`tb_assessment_question`、`tb_assessment_answer`、`tb_qrcode`、`tb_competition`、`tb_venue`、`tb_equipment`、`tb_bug_report_image`）
- 删除顺序：先删除数据库记录，再删除 OSS 对象；单条异常隔离，不影响整体任务
- 新增 `job.orphan-file-cleanup.cron` 配置项（默认 `0 0 2 * * *`）

## Capabilities

### New Capabilities
- `orphan-file-cleanup`: 定期扫描并清理 `tb_file` 中无业务引用的孤儿文件记录及对应 OSS 对象

### Modified Capabilities
- 无现有能力的需求变更

## Impact

- **新增文件**：`OrphanFileCleanupJob.java`
- **修改文件**：`FileDO.java`、`File.java`（领域实体）、`FileRepository.java`、`FileRepositoryImpl.java`、`FileMapper.java`、`FileMapper.xml`、`application.yml`
- **新增迁移**：`V11__add_file_created_at.sql`（为 `tb_file` 添加 `created_at` 字段）
- **系统影响**：对象存储（MinIO / 阿里云 OSS）、PostgreSQL `tb_file` 表
- **无 API 变更**：纯后端离线任务，不影响前端接口
