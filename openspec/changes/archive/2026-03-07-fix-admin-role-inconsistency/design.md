## Context

系统基于 RBAC 规范定义了四个标准角色：SUPER_ADMIN、DIRECTION_ADMIN、MEMBER、CANDIDATE。然而在代码实现中，多处出现硬编码的 "ADMIN" 角色字符串，导致权限检查不一致。主要问题包括：

1. **FileDownloadServiceImpl** 中的 `hasRoleAtLeast` 方法使用硬编码 "ADMIN" 和 "MEMBER" 字符串进行权限检查
2. **多个集成测试** 创建错误的 "ADMIN" 角色进行测试
3. **@WithUserVO 注解使用** 可能使用不正确的角色名称

**约束条件：**
- 必须保持与现有 RBAC 规范完全一致
- 不能改变 API 接口行为，仅优化内部权限检查逻辑
- 必须同步更新所有测试用例，确保测试环境与生产环境一致
- 遵循项目 DDD 四层架构，在正确层级进行权限检查

## Goals / Non-Goals

**Goals:**
- 消除代码中所有硬编码角色字符串，统一使用 RoleType 枚举
- 重构 FileDownloadServiceImpl 权限检查，使用 RoleHierarchy 工具类
- 修复测试代码中的错误角色创建，确保测试环境准确
- 更新所有 @WithUserVO 注解使用，使用正确的角色名称
- 确保权限检查逻辑严格遵循 RBAC 层级关系

**Non-Goals:**
- 不修改 RBAC 规范定义的角色名称或层级
- 不改变现有 API 接口签名或行为
- 不引入新的角色或权限体系
- 不修改前端代码或认证流程

## Decisions

### 1. FileDownloadServiceImpl 重构策略

**决定：** 将 `hasRoleAtLeast(UserVO user, String minRole)` 重构为 `hasRoleAtLeast(UserVO user, RoleType minRole)`，使用 RoleHierarchy 工具类进行权限检查。

**实现方案：**
```java
private boolean hasRoleAtLeast(UserVO user, RoleType minRole) {
    if (user == null || user.getRoleName() == null) {
        return false;
    }
    RoleType userRole = RoleType.fromName(user.getRoleName());
    return RoleHierarchy.hasRoleLevel(userRole, minRole);
}
```

**调用方式更新：**
```java
// 原代码
if (!hasRoleAtLeast(currentUser, "MEMBER")) { ... }

// 新代码
if (!hasRoleAtLeast(currentUser, RoleType.MEMBER)) { ... }
```

**理由：**
- 类型安全，避免拼写错误
- 与现有 RBAC 工具类集成，利用角色层级关系
- 消除硬编码字符串，便于维护

**替代方案：**
- 保留字符串参数但添加验证：仍需字符串到枚举的转换，不如直接使用枚举
- 创建新的权限检查服务：过度设计，当前场景简单明确

### 2. 测试角色创建统一策略

**决定：** 所有测试中创建的角色必须使用规范定义的角色名称：SUPER_ADMIN、DIRECTION_ADMIN、MEMBER、CANDIDATE。

**实现方案：**
```java
// 错误示例 - 必须修复
adminRole.setName("ADMIN");

// 正确示例 - 根据测试需求选择
adminRole.setName("SUPER_ADMIN");  // 超级管理员权限测试
// 或
adminRole.setName("DIRECTION_ADMIN"); // 方向管理员权限测试
```

**理由：**
- 确保测试环境与生产环境一致
- 避免测试通过但生产环境权限检查失败
- 遵循单一真实来源原则

### 3. @WithUserVO 注解更新策略

**决定：** 系统扫描所有使用 @WithUserVO 注解的测试，将角色名称更新为规范定义的值。

**更新范围：**
- 搜索所有 `@WithUserVO(roleName = "ADMIN")` 注解
- 根据测试场景替换为 `"SUPER_ADMIN"` 或 `"DIRECTION_ADMIN"`
- 确保成员和考生角色使用正确的 `"MEMBER"` 和 `"CANDIDATE"`

**理由：**
- 用户特别强调必须同步修改 @WithUserVO 注解使用
- 测试用户角色必须反映真实系统角色
- 避免权限测试产生误导性结果

### 4. 代码审查和清理策略

**决定：** 使用正则表达式搜索全代码库中的硬编码角色字符串，进行系统性清理。

**搜索模式：**
- `"ADMIN"`、`"MEMBER"`、`"DIRECTION_ADMIN"`、`"SUPER_ADMIN"`、`"CANDIDATE"`
- 排除 `RoleType` 枚举定义和数据库迁移脚本
- 重点关注业务逻辑代码中的字符串比较

**清理优先级：**
1. 权限检查逻辑（高优先级）
2. 业务规则判断（中优先级）
3. 日志和异常消息（低优先级）

## Risks / Trade-offs

| 风险 | 影响程度 | 缓解措施 |
|------|----------|----------|
| 重构不完整，遗留硬编码字符串 | 中 | 使用 grep 工具进行全代码库扫描，建立检查清单 |
| 测试覆盖率不足导致回归 | 中 | 运行完整测试套件，特别关注文件下载和权限相关功能 |
| @WithUserVO 注解更新遗漏 | 高 | 使用 IDE 全局搜索功能，逐一验证所有测试文件 |
| 权限逻辑变更影响现有功能 | 高 | 重点测试核心业务场景：文件下载、报名审核、成员管理 |
| 开发与测试环境不一致 | 中 | 在 CI/CD 流程中添加角色一致性检查 |

**权衡考虑：**
- **彻底性 vs 影响范围**：全面清理硬编码字符串可能涉及较多文件，但能确保长期一致性
- **立即修复 vs 分阶段**：立即修复所有问题风险较高，但问题严重性要求尽快解决
- **自动化 vs 手动检查**：结合使用自动化工具扫描和人工代码审查，确保质量

## Migration Plan

### 阶段一：准备（1天）
1. 创建完整的代码扫描报告，识别所有硬编码角色字符串
2. 建立修复清单，明确每个文件的修改内容
3. 备份现有测试数据，准备回滚方案

### 阶段二：实施（2天）
1. 重构 FileDownloadServiceImpl 权限检查方法
2. 修复测试代码中的错误角色创建
3. 更新所有 @WithUserVO 注解使用
4. 清理其他业务代码中的硬编码字符串

### 阶段三：验证（1天）
1. 运行完整单元测试和集成测试套件
2. 特别验证文件下载权限功能
3. 执行端到端测试，确保核心业务流程正常
4. 代码审查和合并

### 阶段四：部署
1. 滚动更新后端服务，监控错误日志
2. 验证生产环境权限检查功能
3. 如有问题，立即回滚到上一版本

**回滚策略：**
- 直接回滚代码更改
- 恢复测试数据备份
- 由于不改变 API 接口，回滚对用户无感知

## Open Questions

1. **是否需要更新数据库中的现有测试数据？**
   - 现有测试数据中可能包含 "ADMIN" 角色记录，需要考虑清理或迁移

2. **是否需要在 CI/CD 流程中添加角色一致性检查？**
   - 可考虑添加静态分析规则，禁止硬编码角色字符串

3. **其他团队是否依赖当前的 "ADMIN" 角色行为？**
   - 需要确认是否有外部系统或脚本依赖当前的错误实现