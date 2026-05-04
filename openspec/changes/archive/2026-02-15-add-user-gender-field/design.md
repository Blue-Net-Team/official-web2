## Context

### 当前状态
当前用户表（`tb_user`）缺少性别字段，用户实体类 `User.java` 和相关DTO/VO均未包含性别信息。根据proposal中的需求，需要为用户系统添加完整的性别字段支持。

### 现有结构分析
- **Entity**: `User` 实体类位于 `domain/model/entity/User.java`，使用 MyBatis-Plus 注解，当前包含 id, studentId, email, roleId, password, username, nickname, collegeId, major, direction, job, avatarId, disable 等字段
- **VO**: `UserVO` 位于 `domain/model/vo/user/UserVO.java`，用于返回用户视图数据
- **DTO**: `UserInfo` 位于 `api/dto/user/UserInfo.java`，用于用户信息传输
- **枚举模式**: 项目使用 `@EnumValue` 注解的枚举，数据库中存储小写下划线格式的字符串（如 `computer_vision`）

### 约束
- 遵循现有枚举命名规范：小写下划线格式存储
- 保持与现有 User 实体字段风格一致
- 使用 MyBatis-Plus 的枚举映射机制
- 不引入新的依赖

## Goals / Non-Goals

**Goals:**
- 在数据库 `tb_user` 表中添加 `gender` 字段，类型为 VARCHAR，默认值为 "unknown"
- 创建 `Gender` 枚举类，支持 male/female/unknown 三种值，使用 `@EnumValue` 注解
- 更新 `User` 实体类，添加 `gender` 字段，类型为 `Gender` 枚举
- 更新 `UserVO`，添加 `gender` 字段以便前端展示
- 更新 `UserInfo`，添加 `gender` 字段以便数据传输
- 确保现有代码兼容性，默认值为 unknown

**Non-Goals:**
- 不修改用户注册接口（前端可选择不传gender，后端使用默认值）
- 不添加复杂的性别校验逻辑（仅需校验枚举值有效性）
- 不修改前端代码（不在本次变更范围内）
- 不添加性别相关的业务逻辑（如权限控制、功能开关等）

## Decisions

### 1. 性别枚举设计
**决策**: 创建新的 `Gender` 枚举类，包含 MALE("male", "男")、FEMALE("female", "女")、UNKNOWN("unknown", "未知") 三个值。

**理由**:
- 遵循项目现有枚举模式（参考 `Direction` 枚举）
- 使用 `@EnumValue` 注解确保 MyBatis-Plus 正确映射
- 小写下划线格式与现有规范保持一致
- 提供中文描述便于后续扩展（如展示用途）

**替代方案考虑**:
- 使用数据库原生枚举类型：MySQL枚举类型迁移不便，且与 MyBatis-Plus 集成复杂
- 使用 Integer 类型存储：可读性差，需要额外的映射层

### 2. 数据库字段类型
**决策**: 使用 `VARCHAR(20)` 类型存储性别值，默认值为 'unknown'。

**理由**:
- 与现有枚举存储方式一致（`Direction` 也是 VARCHAR）
- 可读性好，便于数据库维护和查询
- 扩展性强，未来可支持更多性别选项

**替代方案考虑**:
- TINYINT：节省空间但可读性差
- ENUM：MySQL 特定，迁移不便

### 3. DTO/VO 更新策略
**决策**: 在所有相关 DTO 和 VO 中添加 gender 字段。

**涉及的文件**:
- `UserVO.java` - 添加 `private Gender gender;`
- `UserInfo.java` - 添加 `private Gender gender;`

**理由**:
- 保持数据传输完整性
- 前端可以根据需要展示性别信息
- 与现有字段风格保持一致

### 4. 默认值策略
**决策**: 性别字段默认值为 UNKNOWN。

**理由**:
- 确保现有数据迁移后不会报错
- 用户可以后续补充性别信息
- 符合隐私保护原则（不明确设置则为未知）

## Risks / Trade-offs

### [风险] 现有数据迁移
**风险**: 已有用户记录没有性别字段，迁移后可能出现问题。
**缓解**: 使用数据库默认值 'unknown'，确保迁移后所有记录都有有效值。

### [风险] API 兼容性
**风险**: 前端未更新时，传递的 JSON 可能不包含 gender 字段。
**缓解**: 后端接收时允许 gender 为空，使用默认值 UNKNOWN。

### [风险] 枚举值变更
**风险**: 未来可能需要支持更多性别选项。
**缓解**: 使用 VARCHAR 存储，扩展性好；枚举类易于添加新值。

### [权衡] 数据完整性 vs 灵活性
使用 VARCHAR 而非 ENUM 会牺牲一定的数据完整性约束（数据库层面不会校验值的有效性），但获得更好的灵活性和可读性。数据校验由应用层（Java枚举）保证。

## Migration Plan

### 数据库迁移
```sql
-- 添加 gender 字段到 tb_user 表
ALTER TABLE tb_user
ADD COLUMN gender VARCHAR(20) NOT NULL DEFAULT 'unknown'
COMMENT '性别：male-男, female-女, unknown-未知';
```

### 代码部署步骤
1. **创建枚举类**: 创建 `Gender.java` 枚举
2. **更新实体**: 修改 `User.java`，添加 gender 字段
3. **更新 DTO/VO**: 修改 `UserVO.java` 和 `UserInfo.java`
4. **数据库迁移**: 执行 SQL 脚本添加字段
5. **测试**: 验证用户查询、更新接口正常返回 gender 字段

### 回滚策略
- 数据库回滚：`ALTER TABLE tb_user DROP COLUMN gender;`
- 代码回滚：Git 回退相关提交

## Open Questions

1. **是否需要更新 UserAuthResponseDTO？**
   - 该 DTO 用于登录响应，是否需要在登录时返回性别信息？
   - 建议：暂时不添加，保持登录响应精简，用户详情接口已包含 UserInfo

2. **是否需要添加性别相关的查询条件？**
   - 如按性别筛选用户列表
   - 建议：暂时不需要，如有需求在后续迭代中添加

3. **是否需要更新 StudentIdLoginRequestDTO？**
   - 登录请求一般不需要性别字段
   - 确认：不需要更新
