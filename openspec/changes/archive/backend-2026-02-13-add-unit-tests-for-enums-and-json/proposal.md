## Why

数据库实体代码已生成并完成了 Flyway 迁移，但还需要验证：
1. 枚举类与数据库的映射是否正确（@EnumValue 配置）
2. QuestionContent 多态 JSON 的序列化和反序列化是否正常

这些验证需要通过单元测试来完成，确保数据持久化和读取的准确性。

## What Changes

- 创建枚举映射的单元测试（验证 Direction, FileType 等8个枚举）
- 创建 QuestionContent JSON 序列化/反序列化的单元测试
- 创建实体 CRUD 的基础测试（验证 MyBatis Mapper 正常工作）
- 修复发现的任何问题

## Capabilities

### New Capabilities

- `enum-mapping-test`: 枚举与数据库映射验证
- `json-serialization-test`: QuestionContent JSON 序列化验证
- `entity-crud-test`: 实体类 CRUD 操作验证

### Modified Capabilities

- 无

## Impact

- 在 `src/test/java` 添加单元测试类
- 确保所有枚举和实体能正确与数据库交互
- 为后续开发提供回归测试基础
