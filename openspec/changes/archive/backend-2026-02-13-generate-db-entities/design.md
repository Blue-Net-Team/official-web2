## Context

蓝网官方网站后端项目采用DDD分层架构，项目骨架已搭建完成但领域层实体为空。根据产品功能手册，需要生成18张表的实体类、枚举、MyBatis映射和Flyway迁移脚本。

**项目约束**：
- 单体架构，团队资金有限，暂不考虑分布式扩展
- 使用MyBatis Plus作为ORM框架
- PostgreSQL作为数据库
- 无通用审计字段（create_time/update_time/deleted）

## Goals / Non-Goals

**Goals:**
- 生成18个领域实体类，完整映射产品手册表设计
- 创建8个枚举类，使用@EnumValue注解实现数据库值映射
- 实现18个MyBatis Mapper接口及XML映射
- 编写Flyway初始迁移脚本
- 建立QuestionContent抽象继承体系支持多态JSON存储
- 确立自增主键和枚举映射规范

**Non-Goals:**
- 不实现业务逻辑代码（Service/Controller层）
- 不配置MyBatis Plus自动填充处理器
- 不实现自定义TypeHandler（使用默认EnumTypeHandler + @EnumValue）

## Decisions

### Decision 1: 自增主键策略
**选择**：使用数据库自增（AUTO_INCREMENT）而非雪花算法
**理由**：
- 单体架构下无需考虑分布式ID冲突
- 实现简单，性能优于雪花算法
- 团队规模小，数据量有限，ID位数不会成为问题
**替代方案**：雪花算法（ASSIGN_ID）- 为未来分布式预留，但当前增加复杂度

### Decision 2: 枚举值存储格式
**选择**：使用@EnumValue注解存储小写下划线格式
**示例**：
```java
public enum Direction {
    @EnumValue("computer_vision")
    COMPUTER_VISION,
    @EnumValue("structural_design")
    STRUCTURAL_DESIGN,
    @EnumValue("embedded")
    EMBEDDED
}
```
**理由**：
- 数据库可读性好，避免大写枚举名
- 符合PostgreSQL命名规范
- MyBatis Plus原生支持@EnumValue
**替代方案**：存储枚举名（默认）- 可读性差，不符合规范

### Decision 3: QuestionContent多态JSON设计
**选择**：使用Jackson @JsonTypeInfo实现多态反序列化
**类结构**：
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({...})
public abstract class QuestionContent {
    private String content; // 题干
}

public class SingleChoiceContent extends QuestionContent {
    private List<String> options;
    private String correctAnswer;
}

public class AlgorithmContent extends QuestionContent {
    private List<TestCase> testCases;
    private Integer timeLimit;
    private Integer memoryLimit;
}
```
**理由**：
- 单表设计避免JOIN，提升查询性能
- JSON结构灵活，支持不同题目类型的差异化字段
- Jackson原生支持多态序列化/反序列化
**替代方案**：分表存储（question_single_choice, question_algorithm等）- 增加表数量，查询复杂

### Decision 4: 无审计字段设计
**选择**：所有表（除Audit）均无create_time/update_time/deleted字段
**例外**：
- User表有disable字段用于软删除逻辑
- Audit表有action_time记录操作时间
**理由**：
- 符合产品手册设计
- 减少冗余字段，简化实体
- 业务逻辑不需要这些元数据
**风险**：无法追踪记录创建/修改时间，问题排查困难
**缓解**：通过Audit表记录关键操作

### Decision 5: 文件权限关联设计
**选择**：File表仅存储元数据，权限通过业务表关联判断
**权限矩阵**：
| 文件类型 | 控制表 | 判断逻辑 |
|---------|--------|----------|
| work | EvaluationAnswer | currentUser.id == answer.user_id OR role >= MEMBER |
| evaluation_attachment | EvaluationQuestion | currentUser.direction == question.direction |
| avatar(Enroll) | Enroll | role >= MEMBER |
| avatar(User) | User | currentUser.id == user.id OR has role |
| normal_img, qrcode | - | 公开访问 |
**理由**：
- 避免File表过度耦合业务逻辑
- 权限规则可灵活调整
- 减少冗余字段
**替代方案**：File表增加owner_id/permission字段 - 无法覆盖复杂业务规则

### Decision 6: 评分实时计算
**选择**：EvaluationAnswer不存储final_score，查询时计算平均分
**理由**：
- 避免数据不一致（评论增删改后需要同步更新）
- 实现简单，Comment表记录评分
- 查询频率低（仅考核结束后查看）
**替代方案**：存储final_score，通过触发器或事件同步 - 增加复杂度

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| **JSON字段无法建索引** | QuestionContent查询性能差 | 增加evaluation_time_id等外键索引，避免按content查询 |
| **枚举值变更困难** | 数据库已有数据与代码不一致 | 使用Flyway迁移脚本管理枚举值变更，避免直接修改 |
| **无审计字段排查难** | 无法确定记录创建时间 | 关键操作记录Audit日志，必要时补充时间字段 |
| **自增主键ID暴露** | 可预测ID存在安全风险 | ID仅用于内部关联，不对外暴露 |
| **多态JSON反序列化失败** | 数据结构变更导致历史数据无法解析 | 使用Jackson忽略未知字段，保持向后兼容 |

## Migration Plan

**部署步骤**：
1. 执行Flyway迁移脚本V1__init_schema.sql创建所有表
2. 验证表结构与实体类映射一致
3. 运行单元测试确保Mapper正常工作

**回滚策略**：
- Flyway迁移失败自动回滚
- 如已执行，手动执行DROP TABLE语句（开发环境）
- 生产环境需谨慎，建议先备份

## Open Questions

1. **QuestionContent JSON结构版本管理**：未来题目类型扩展时如何兼容旧数据？
2. **File表url字段格式**：是相对路径还是完整URL？是否包含域名？
3. **EvaluationAnswer.content字段长度**：算法代码可能很长，是否需要TEXT类型？
