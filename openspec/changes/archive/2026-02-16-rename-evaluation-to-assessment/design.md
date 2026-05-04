## Context

当前考核功能使用表前缀 `tb_evaluation_*` 与实体名 `Evaluation*`，权限为 `evaluation-*`。产品与文档已统一为「考核 / assessment」。需在不改变业务逻辑的前提下，通过数据库迁移与代码重命名完成术语统一。后端为单体应用，使用 Flyway、MyBatis，权限存库并在代码中通过常量/注解引用。

## Goals / Non-Goals

**Goals:**
- 表、列、索引、约束及注释中的 evaluation 命名统一为 assessment。
- 权限标识从 evaluation-* 改为 assessment-*，并在迁移中更新角色-权限关联。
- Java 实体、Mapper、Service、枚举等命名与引用统一为 assessment。
- 通过单一 Flyway 迁移可逆地完成库表与权限重命名，便于回滚。

**Non-Goals:**
- 不改变 API 路径或请求/响应字段语义（若仅重命名 JSON 字段则属实现细节，可在此变更或后续单独做）。
- 不在此变更中修改前端代码；前端适配可作为后续任务或由前端仓库同步。

## Decisions

1. **迁移方式：单条 Flyway 迁移脚本**
   - 新建一条迁移（如 `V6__rename_evaluation_to_assessment.sql`），在其中依次：重命名表（`ALTER TABLE ... RENAME TO ...`）、重命名列（如 `evaluation_time_id` → `assessment_time_id`）、重命名索引、更新 `tb_comment.answer_id` 等外键注释、更新权限表中等。
   - 理由：可追溯、可回滚，且不修改历史迁移文件，符合 Flyway 规范。备选「多条小迁移」增加版本数且无必要。

2. **权限数据迁移**
   - 在同一迁移或紧随其后的迁移中，对存储权限 value 的表执行 UPDATE，将 `evaluation-time:*`、`evaluation-answer:*`、`evaluation-question:*`、`evaluation-result:*` 替换为 `assessment-*` 对应值。
   - 理由：保证数据库与代码中的权限标识一致，避免权限校验失败。

3. **Java 包与类重命名**
   - 实体：`EvaluationTime` → `AssessmentTime`，`EvaluationQuestion` → `AssessmentQuestion`，`EvaluationAnswer` → `AssessmentAnswer`；表名注解同步为 `tb_assessment_*`。
   - Mapper 接口与 XML：重命名类与 namespace，XML 内表名、列名、resultMap 等全部改为 assessment 命名。
   - Service/Controller：类名与内部引用统一改为 Assessment*；权限注解中的字符串改为 `assessment-*`。
   - 枚举与常量：如文件类型 `evaluation_attachment` → `assessment_attachment`，以及所有 evaluation 相关常量。
   - 理由：一次性统一命名，避免遗留别名造成混淆。

4. **外键与关联**
   - 仅做表名列名重命名，不改变外键逻辑。`tb_comment.answer_id` 仍指向原表（重命名后为 `tb_assessment_answer.id`），迁移中不新增或删除外键，仅更新注释（若有）。
   - 理由：最小化迁移风险，不触发外键重建。

## Risks / Trade-offs

- **[Risk]** 迁移执行期间表被重命名，若应用未及时重启或仍使用旧实体名，会报错。
  **Mitigation:** 部署顺序为先执行 Flyway 迁移，再部署新版本应用；或维护窗口内停机部署。

- **[Risk]** 权限 value 的 UPDATE 若漏改或与代码不一致，会导致部分接口 403。
  **Mitigation:** 迁移脚本中显式列出所有需替换的 permission value，并与代码中常量/注解做一次对照；考虑在迁移后加一条校验 SQL 或测试断言。

- **[Trade-off]** 不在此变更中改 API 路径（如 `/evaluation/` → `/assessment/`）可减少前端与调用方改动，但若后续要改则需再发一版。
  **Mitigation:** 在 proposal/impact 中说明；若产品要求 URL 也统一，可在 tasks 中加「可选：API 路径重命名」子任务。

## Migration Plan

1. **开发阶段**：编写 Flyway 迁移脚本（表/列/索引/注释/权限 value 更新），并在本地或测试库验证。
2. **部署**：先执行 Flyway（应用启动时或单独执行），再部署新代码；或约定维护窗口，停机部署。
3. **回滚**：保留一条「回滚」迁移脚本（或文档化逆向 SQL），将表名与权限 value 改回 evaluation；应用回滚到旧版本。若已对外暴露 assessment 权限，回滚后需同步恢复权限数据。

## Open Questions

- 是否需要同时将 API 路径由 `/evaluation*` 改为 `/assessment*`？若需要，可在 tasks 中增加前端与网关的对应项。
