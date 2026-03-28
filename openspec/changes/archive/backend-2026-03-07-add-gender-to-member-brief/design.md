## Context

当前后端成员简要信息 DTO (`MemberBriefDTO`) 缺少性别字段，而：
- 数据库实体已有 `gender` 字段
- `MemberDetailDTO` 已包含 `gender` 字段
- `Gender` 枚举已定义：`MALE`、`FEMALE`、`UNKNOWN`
- `MemberVO` 已包含 `gender` 字段

只需在 DTO 和转换器中补充映射即可。

## Goals / Non-Goals

**Goals:**
- 为 `MemberBriefDTO` 添加 `gender` 字段
- 更新 `MemberConverter.toBriefDTO()` 映射 `gender` 字段
- 保持与现有代码风格一致

**Non-Goals:**
- 不修改数据库结构
- 不修改 `Gender` 枚举定义
- 不修改 `MemberDetailDTO`（已包含该字段）

## Decisions

### 1. 字段类型选择
**决定**: 使用现有的 `Gender` 枚举类型
**理由**:
- 与 `MemberDetailDTO` 保持一致
- 类型安全，避免字符串硬编码
- 已有 `@EnumValue` 注解支持 MyBatis-Plus 序列化

### 2. 字段可空性
**决定**: 字段可为空 (`Gender?`)
**理由**:
- 历史数据可能没有性别信息
- 保持向后兼容

## Risks / Trade-offs

- **前端兼容性**: 新增字段不会破坏现有前端，前端需要同步更新类型定义
