## Context

当前系统已实现用户经历管理的基础CRUD功能，但存在以下问题：

1. **权限控制缺陷**: 所有已登录用户（包括CANDIDATE角色）都能管理经历，违反业务规则"考生无法添加、修改、删除经历"
2. **缺少公开接口**: 团队成员的经历信息无法对外展示，不符合"团队成员的经历是完全公开的"需求
3. **代码缺陷**: `UserExperienceDomainServiceImpl.getExperienceById()` 第41行的权限校验逻辑存在bug

系统采用DDD四层架构，权限控制基于 `@RequiresPermission` 注解 + AOP拦截实现，支持三种访问级别：PUBLIC、AUTHENTICATED、PROTECTED。

## Goals / Non-Goals

**Goals:**
- 修复权限控制缺陷，确保只有MEMBER及以上角色才能管理经历
- 提供公开接口，允许未登录用户查看团队成员的经历
- 修复现有代码bug，提升代码质量
- 完善测试覆盖，确保功能稳定可靠

**Non-Goals:**
- 不修改经历数据模型或数据库表结构
- 不添加新的经历类型（仅支持现有的project/competition/internship）
- 不实现经历审核或管理功能
- 不添加隐私设置或可见性控制

## Decisions

### 1. 权限控制方案

**决策**: 使用现有权限系统，在数据库中添加权限记录并分配给相应角色

**理由**:
- 系统已有完善的权限管理机制，无需引入新的权限模型
- 权限数据存储在数据库中，便于动态调整
- 符合项目规范，权限注解统一使用 `AccessLevel.PROTECTED`

**替代方案**:
- ❌ 在代码中硬编码角色检查：违反项目规范，不够灵活
- ❌ 创建新的访问级别：增加系统复杂度，无必要

**实现细节**:
```
新增权限:
- user:experience:create (创建经历)
- user:experience:update (更新经历)
- user:experience:delete (删除经历)

分配给角色:
- MEMBER (level 2)
- DIRECTION_ADMIN (level 3)
- SUPER_ADMIN (level 4)
```

### 2. 公开接口设计

**决策**: 在 `MemberController` 中新增 `GET /api/v1/members/{memberId}/experiences` 接口

**理由**:
- 语义清晰：成员经历是成员信息的一部分
- 符合RESTful设计：资源路径 `/members/{id}/experiences` 表达层级关系
- 访问级别为PUBLIC，无需认证

**替代方案**:
- ❌ 在 `UserExperienceController` 中添加：语义不明确，用户经历是私密资源
- ❌ 集成到成员详情接口：可能导致响应数据过大，影响性能

**实现细节**:
```java
@RequiresPermission(name = "查看成员经历", value = "member:experience:view",
                    access = AccessLevel.PUBLIC)
@GetMapping("/{memberId}/experiences")
public ResponseMessage<List<ExperienceDTO>> getMemberExperiences(
    @PathVariable Long memberId,
    @RequestParam(required = false) String type)
```

### 3. Bug修复方案

**决策**: 修复 `UserExperienceDomainServiceImpl.getExperienceById()` 的权限校验逻辑

**问题**: 第41行检查 `!experience.get().getId().equals(experienceId)` 永远为false

**修复**:
```java
// 错误代码
if (experience.isPresent() && !experience.get().getId().equals(experienceId)) {
    return Optional.empty();
}

// 修复后
if (experience.isPresent()) {
    ExperienceVO exp = experience.get();
    if (!userExperienceRepository.checkOwner(experienceId, userId)) {
        log.warn("用户 {} 尝试访问不属于自己的经历 {}", userId, experienceId);
        return Optional.empty();
    }
}
```

### 4. 测试策略

**决策**: 采用分层测试策略，覆盖单元测试、集成测试和边界测试

**测试范围**:
- **单元测试**: 测试各层独立功能
  - Controller层: 测试权限控制、参数验证、异常处理
  - Service层: 测试业务逻辑、数据转换
  - Repository层: 测试数据访问、VO转换

- **集成测试**: 测试API接口的完整流程
  - 不同角色的权限测试
  - 公开接口访问测试
  - 数据一致性测试

- **边界测试**: 测试异常场景和边界条件
  - 权限边界: 考生尝试管理经历
  - 数据边界: 空数据、超长数据、非法数据
  - 并发边界: 并发创建/更新经历

## Risks / Trade-offs

### 风险1: 权限数据迁移

**风险**: 需要在生产环境数据库中添加权限记录，可能影响现有用户

**缓解措施**:
- 使用Flyway迁移脚本，确保幂等性
- 在测试环境充分验证后再部署生产
- 准备回滚脚本，出现问题可快速回滚

### 风险2: 现有考生数据

**风险**: 如果已有考生创建了经历，权限修改后他们将无法管理这些数据

**缓解措施**:
- 调研生产环境是否存在这种情况
- 如存在，提供数据迁移方案或保留这些经历
- 在文档中明确说明权限变更影响

### 风险3: 性能影响

**风险**: 公开接口可能导致大量未认证请求，增加服务器负载

**缓解措施**:
- 添加缓存机制，缓存成员经历数据
- 考虑添加访问频率限制
- 监控接口性能，必要时优化查询

### 权衡: 灵活性 vs 复杂度

**权衡**: 使用数据库权限系统增加了灵活性，但也增加了复杂度

**选择理由**: 项目已有完善的权限管理机制，保持一致性比简化更重要
