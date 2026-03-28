## Context

当前数据库包含18张业务表（tb_role, tb_permission, tb_role_permission, tb_college, tb_user, tb_user_experience, tb_achievement, tb_user_achievement, tb_file, tb_introduce_image, tb_enroll, tb_verify_code, tb_message_template, tb_evaluation_time, tb_evaluation_question, tb_evaluation_answer, tb_comment, tb_audit），但所有表、列和索引都缺少注释说明。

开发手册《官网功能与开发手册.md》中已详细定义了各表的字段含义和业务规则，需要将这些信息同步到数据库元数据中。

## Goals / Non-Goals

**Goals:**
- 为所有18张表添加中文表注释，说明表的用途
- 为所有列添加中文列注释，说明字段含义和业务规则
- 为所有索引添加注释，说明索引用途
- 使用 Flyway 迁移脚本实现，确保可重复执行和环境一致性
- 与开发手册定义保持一致

**Non-Goals:**
- 不修改表结构或字段类型
- 不添加新的业务字段
- 不修改现有数据
- 不引入外键约束（保持现有设计）

## Decisions

### 1. 迁移脚本命名规范
**决策**: 使用 `V5__add_table_column_comments.sql` 作为迁移文件名
**理由**:
- 遵循现有 Flyway 版本号序列（V1-V4 已存在）
- 语义清晰，表明这是注释添加迁移

### 2. 注释内容来源
**决策**: 以开发手册中的表设计定义为准，补充代码中的实际含义
**理由**:
- 开发手册是权威的业务文档来源
- 确保文档与实际表结构一致

### 3. 注释语言
**决策**: 使用中文注释
**理由**:
- 团队成员以中文为母语
- 与开发手册保持一致

### 4. 软删除字段处理
**决策**: 所有表都有 `deleted` 字段用于软删除，需要添加注释说明
**理由**:
- 根据开发手册规范，所有表都必须有 `deleted` 软删除字段
- V1__init_schema.sql 中未显示但规范要求存在

## Risks / Trade-offs

**风险**: 迁移脚本如果在生产环境执行失败，可能影响部署
**缓解**:
- 在本地和测试环境充分测试
- 使用事务包装所有 COMMENT 语句（PostgreSQL 支持）

**风险**: 注释内容与开发手册不一致
**缓解**:
- 仔细对照开发手册的表设计章节
- 执行后使用 `\d+` 命令验证注释已正确添加

**风险**: 后续表结构变更导致注释过时
**缓解**:
- 建立规范：新增表/字段时必须同时添加注释
- 代码审查时检查注释完整性

## Migration Plan

1. **开发环境**: 创建迁移脚本 → 执行 `flyway migrate` → 验证注释
2. **测试环境**: 部署脚本 → 验证所有注释正确
3. **生产环境**: 在维护窗口执行迁移 → 验证

**Rollback**: PostgreSQL 的 COMMENT 语句是幂等的，可以重新执行覆盖。如需要回滚，可执行 `COMMENT ON TABLE xxx IS NULL` 清除注释。

## Open Questions

- 是否需要为序列（SEQUENCE）也添加注释？（建议：是，为 tb_xx_id_seq 添加注释）
