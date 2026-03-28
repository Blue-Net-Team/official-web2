## Context

数据库实体层已完成，包含：
- 8个枚举类使用 @EnumValue 注解
- 18个实体类使用 MyBatis-Plus
- QuestionContent 多态继承体系使用 Jackson

需要验证这些组件与数据库的交互是否正确。

## Goals / Non-Goals

**Goals:**
- 验证枚举与数据库的映射（@EnumValue 工作原理）
- 验证 QuestionContent JSON 多态序列化/反序列化
- 验证实体 CRUD 基本功能
- 为后续开发提供可运行的测试基线

**Non-Goals:**
- 不测试业务逻辑（Service 层）
- 不测试复杂的查询场景
- 不追求 100% 代码覆盖率

## Decisions

### Decision 1: 使用 @SpringBootTest 还是纯单元测试
**选择**：使用 @SpringBootTest 集成测试
**理由**：
- 需要验证与真实数据库的交互
- MyBatis-Plus 的枚举处理需要 Spring 上下文
- 使用 H2 内存数据库进行测试

**替代方案**：纯单元测试 + Mock - 无法验证数据库映射

### Decision 2: 测试数据库选择
**选择**：H2 内存数据库
**理由**：
- 测试运行快，无需外部依赖
- Spring Boot 原生支持
- 每次测试独立，数据隔离

**配置**：使用 PostgreSQL 兼容模式

### Decision 3: 测试类组织方式
**选择**：按功能分组
```
EnumMappingTest - 测试所有枚举映射
QuestionContentJsonTest - 测试 JSON 序列化
EntityCrudTest - 测试实体 CRUD
```

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| H2 与 PostgreSQL 行为差异 | 使用 PostgreSQL 兼容模式，关键测试在真实环境再验证 |
| 测试运行时间较长 | 仅在关键实体上做完整 CRUD，其他做简单验证 |

## Migration Plan

1. 创建测试基类提供通用工具方法
2. 实现 EnumMappingTest
3. 实现 QuestionContentJsonTest
4. 实现 EntityCrudTest（重点验证 User 和 EvaluationQuestion）
5. 运行所有测试验证通过

## Open Questions

1. 是否需要为所有 18 个实体都写完整测试？
2. JSON 测试是否需要测试边界情况（空值、超大 JSON）？
