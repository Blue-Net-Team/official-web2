## Context

当前 Bug 报告模型只有 `description` 一个文本字段，同步到 GitHub Issue 时 `GitHubIssueSyncService.buildTitle()` 直接截取 description 前 100 字符作为标题。issue #24 要求增加独立的 `title` 字段，使用户在反馈时可以输入一个简洁的问题简述。

已有数据：生产环境 `tb_bug_report` 表可能存在历史记录，这些记录没有 `title`。需要通过迁移脚本将 `description` 直接回填到 `title`（字段长度限制为 200，超长部分由数据库自然截断），并设置 `NOT NULL` 约束。

## Goals / Non-Goals

**Goals:**
- 新增 `title` 字段并作为必填项贯穿前后端。
- 历史数据无缝迁移，`title` 非空。
- GitHub Issue 标题使用用户输入的 `title`，更简洁准确。
- 管理端列表以 `title` 作为主要可读列。

**Non-Goals:**
- 不改变 GitHub Issue Body 的内容和格式。
- 不提供管理端编辑 title 的功能（标题在提交时确定）。
- 不改变截图上传、状态流转等其他逻辑。

## Decisions

1. **数据库字段类型与约束**
   - `title VARCHAR(200) NOT NULL`：兼容 100 字符的业务上限并留有余量。
   - 迁移脚本：`UPDATE tb_bug_report SET title = description WHERE title IS NULL;`，然后添加 `NOT NULL` 约束（具体语法取决于项目使用的迁移工具）。由于 `title` 字段长度为 200，超长 description 会被数据库截断。

2. **业务长度上限**
   - `title` 最大 100 字符（前端、DTO、领域层统一）。
   - `description` 仍保持 2000 字符，作为详细描述。

3. **GitHub Issue 标题 fallback 策略**
   - 优先使用 `bugReport.getTitle()`。
   - 若为空（历史异常数据或兼容场景），降级使用 `description` 前 100 字符。
   - 仍遵循 `MAX_TITLE_LENGTH = 100` 截断保护。

4. **前后端字段命名**
   - 统一使用 `title`，前端表单 label 为"Bug 标题"，placeholder 为"请简要描述问题..."。

5. **管理端列表展示**
   - 表格"描述"列改为"标题"列，详情页在"问题描述"上方新增"标题"区块。

## Risks / Trade-offs

- **[Risk] 旧客户端直接调用 API 可能因缺少 title 失败** → 这是预期行为，Bug 报告接口目前仅由站内前端调用，无第三方集成。
- **[Risk] 迁移脚本在 description 为空的历史数据上可能产生空 title** → 当前业务层已要求 description 必填，历史数据理论上不存在空 description；如出现，迁移脚本可设置默认标题 `"Bug Report"`。
- **[Risk] 截断的 description 作为 title 仍可能冗长** → 属于一次性迁移数据，后续新提交会使用用户输入的简洁标题。

## Migration Plan

1. 编写数据库迁移脚本（Flyway/Liquibase/手动 SQL，取决于项目现有方案），新增 `title` 字段并回填历史数据。
2. 部署后端：新增字段的 DTO/Entity/Converter 同步上线。
3. 部署前端：用户端弹窗和管理端页面更新。
4. 回滚：如出现问题，可回滚代码；数据库字段新增为向前兼容的 schema 变更，回滚时不删除字段即可。
