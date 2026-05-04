## Why

业务与文档中已统一使用「考核」对应英文 `assessment`，而数据库表与实体仍使用 `evaluation`，造成术语不一致、维护与沟通成本增加。本次将考核相关表与命名统一为 `assessment`，便于与产品、文档及后续扩展保持一致。

## What Changes

- 数据库表重命名：`tb_evaluation_time` → `tb_assessment_time`，`tb_evaluation_question` → `tb_assessment_question`，`tb_evaluation_answer` → `tb_assessment_answer`；相关列名、索引、外键及注释同步更新。
- **BREAKING**：权限标识由 `evaluation-time:*`、`evaluation-answer:*`、`evaluation-question:*`、`evaluation-result:*` 改为 `assessment-time:*`、`assessment-answer:*`、`assessment-question:*`、`assessment-result:*`；依赖这些权限的配置与前端需同步修改。
- 实体与领域命名：Java 实体、Mapper、Service 等由 Evaluation* 改为 Assessment*；文件类型枚举等中的 `evaluation_attachment` 改为 `assessment_attachment`。
- 新增 Flyway 迁移脚本，仅做重命名与注释更新，不改变表结构或业务逻辑。

## Capabilities

### New Capabilities
（无新增能力，仅命名与表结构术语统一。）

### Modified Capabilities
- `evaluation-system`: 实体与表命名由 Evaluation* / tb_evaluation_* 改为 Assessment* / tb_assessment_*，权限标识由 evaluation-* 改为 assessment-*；功能需求与行为不变，仅术语与命名约定变更。

## Impact

- **数据库**：Flyway 新迁移文件（表/列/索引/注释重命名）；已有环境需执行迁移。
- **后端**：实体类、Mapper XML、Repository/Service、权限常量与注解、枚举（如文件类型）等所有引用 evaluation 的代码需改为 assessment。
- **前端/配置**：若通过权限标识或 API 文档引用 evaluation 相关名称，需同步更新。
- **文档与 Spec**：`openspec/specs/evaluation-system/` 通过 delta spec 描述命名变更；主 spec 可选同步术语。
