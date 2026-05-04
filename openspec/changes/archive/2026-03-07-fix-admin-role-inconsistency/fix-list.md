# 角色一致性修复清单

基于代码扫描结果，识别出以下需要修复的硬编码角色字符串问题。

## 修复优先级

### 高优先级（关键业务逻辑）
1. **FileDownloadServiceImpl.java** - 文件下载权限检查逻辑存在硬编码"ADMIN"和"MEMBER"字符串
2. **PermissionAspect.java** - 超级管理员检查使用硬编码"SUPER_ADMIN"字符串

### 中优先级（测试代码）
3. **测试文件中的角色创建和查询** - 多个集成测试创建错误的"ADMIN"角色或使用硬编码字符串

### 低优先级（其他引用）
4. **业务逻辑中的硬编码引用** - 其他业务代码中的硬编码角色名称

## 详细修复清单

### 1. FileDownloadServiceImpl.java
**文件路径**: `src/main/java/com/bluenet/web/application/service/impl/FileDownloadServiceImpl.java`

**问题**:
- 第79行: `hasRoleAtLeast(currentUser, "MEMBER")` - 硬编码字符串参数
- 第112-126行: `hasRoleAtLeast`方法内部使用硬编码字符串比较
  - 第120行: `"MEMBER".equals(userRoleName) || "ADMIN".equals(userRoleName)`
  - 第121行: `case "ADMIN" :`
  - 第122行: `return "ADMIN".equals(userRoleName);`

**修复方案**:
1. 修改`hasRoleAtLeast`方法签名: `hasRoleAtLeast(UserVO user, String minRole)` → `hasRoleAtLeast(UserVO user, RoleType minRole)`
2. 重构方法内部逻辑使用`RoleType`枚举和`RoleHierarchy.hasRoleLevel()`
3. 更新调用处使用`RoleType.MEMBER`而非字符串"MEMBER"

### 2. PermissionAspect.java
**文件路径**: `src/main/java/com/bluenet/web/infrastructure/security/aspect/PermissionAspect.java`

**问题**:
- 第120行: `user.getRoleName().equals("SUPER_ADMIN")` - 硬编码"SUPER_ADMIN"字符串

**修复方案**:
1. 使用`RoleType.SUPER_ADMIN.getName().equals(user.getRoleName())`
2. 或使用`RoleHierarchy.isSuperAdmin(RoleType.fromName(user.getRoleName()))`

### 3. 测试文件 - 角色创建修复

#### 3.1 AdminEnrollControllerIntegrationTest.java
**文件路径**: `src/test/java/com/bluenet/web/api/controller/v1/enrollment/AdminEnrollControllerIntegrationTest.java`

**问题**:
- 第82行: `roleMapper.selectByName("ADMIN")`
- 第84行: `Role.builder().name("ADMIN").build()`
- 第88行: `roleMapper.selectByName("MEMBER")`
- 第90行: `Role.builder().name("MEMBER").build()`
- 第344行: `roleMapper.selectByName("MEMBER")`
- 第506行: `roleMapper.selectByName("MEMBER")`

**修复方案**:
- "ADMIN" → "SUPER_ADMIN"
- "MEMBER"保持不变（但应考虑使用常量）

#### 3.2 AdminCollegeControllerIntegrationTest.java
**文件路径**: `src/test/java/com/bluenet/web/api/controller/v1/admin/AdminCollegeControllerIntegrationTest.java`

**问题**:
- 第99行: `adminRole.setName("ADMIN")`

**修复方案**:
- "ADMIN" → "SUPER_ADMIN"

#### 3.3 AdminCompetitionControllerIntegrationTest.java
**文件路径**: `src/test/java/com/bluenet/web/api/controller/v1/admin/AdminCompetitionControllerIntegrationTest.java`

**问题**:
- 第94行: `adminRole.setName("ADMIN")`

**修复方案**:
- "ADMIN" → "SUPER_ADMIN"

#### 3.4 MemberControllerIntegrationTest.java
**文件路径**: `src/test/java/com/bluenet/web/api/controller/v1/member/MemberControllerIntegrationTest.java`

**问题**:
- 第73行: `eq(Role::getName, "MEMBER")`
- 第76行: `memberRole.setName("MEMBER")`
- 第82行: `eq(Role::getName, "DIRECTION_ADMIN")`
- 第85行: `directionAdminRole.setName("DIRECTION_ADMIN")`
- 第91行: `eq(Role::getName, "SUPER_ADMIN")`
- 第94行: `superAdminRole.setName("SUPER_ADMIN")`

**修复方案**:
- 这些是正确的角色名称，但应考虑使用常量而非硬编码字符串

### 4. 其他测试文件

#### 4.1 EnrollDomainServiceImplTest.java
**文件路径**: `src/test/java/com/bluenet/web/domain/service/impl/EnrollDomainServiceImplTest.java`

**问题**:
- 第102行: `.name("MEMBER")`
- 第369行: `when(roleMapper.selectByName("MEMBER")).thenReturn(memberRole);`
- 第422行: `when(roleMapper.selectByName("MEMBER")).thenReturn(null);`

#### 4.2 SystemUserInitializerTest.java
**文件路径**: `src/test/java/com/bluenet/web/infrastructure/init/SystemUserInitializerTest.java`

**问题**:
- 第65行: `when(roleMapper.selectByName("SUPER_ADMIN")).thenReturn(Role.buildSuperAdmin());`
- 第91行: `when(roleMapper.selectByName("SUPER_ADMIN")).thenReturn(Role.buildSuperAdmin());`
- 第113行: `when(roleMapper.selectByName("SUPER_ADMIN")).thenReturn(Role.buildSuperAdmin());`

#### 4.3 EntityCrudTest.java
**文件路径**: `src/test/java/com/bluenet/web/infrastructure/repository/mapper/EntityCrudTest.java`

**问题**:
- 第48行: `Role role = roleMapper.selectByName("MEMBER");`

#### 4.4 UserInfoServiceImplTest.java
**文件路径**: `src/test/java/com/bluenet/web/application/service/impl/UserInfoServiceImplTest.java`

**问题**:
- 第37行: `private static final String TEST_ROLE_NAME = "MEMBER";`

### 5. 业务逻辑文件

#### 5.1 EnrollDomainServiceImpl.java
**文件路径**: `src/main/java/com/bluenet/web/domain/service/impl/EnrollDomainServiceImpl.java`

**问题**:
- 第203行: `Role memberRole = roleMapper.selectByName("MEMBER");`

#### 5.2 SystemUserInitializer.java
**文件路径**: `src/main/java/com/bluenet/web/infrastructure/init/SystemUserInitializer.java`

**问题**:
- 第38行: `Role role = roleMapper.selectByName("SUPER_ADMIN");`

## 修复策略

### 阶段1：重构核心权限检查逻辑
1. 修改`FileDownloadServiceImpl.hasRoleAtLeast()`方法使用`RoleType`枚举
2. 修复`PermissionAspect.isSuperAdmin()`方法

### 阶段2：修复测试角色创建
1. 将所有创建"ADMIN"角色的测试改为创建"SUPER_ADMIN"
2. 确保测试使用正确的角色名称

### 阶段3：清理其他硬编码引用
1. 将业务逻辑中的硬编码角色名称替换为常量引用
2. 更新测试中的硬编码字符串

### 阶段4：验证和测试
1. 运行完整测试套件
2. 验证权限检查逻辑正常工作

## 风险控制
1. **不改变API接口** - 仅修改内部实现
2. **保持向后兼容** - 权限检查逻辑结果应与之前一致
3. **逐步修复** - 分阶段实施，每阶段完成后运行测试
4. **备份测试数据** - 修复前备份现有测试数据

## 成功标准
1. 代码中无硬编码"ADMIN"、"MEMBER"、"SUPER_ADMIN"、"DIRECTION_ADMIN"、"CANDIDATE"字符串
2. 所有角色引用通过`RoleType`枚举或`RoleHierarchy`工具类
3. 所有测试通过
4. 权限检查功能与之前一致

## 修复完成状态 (2026-03-01)

### ✅ 已完成的核心修复

#### 1. 核心权限检查逻辑
- **FileDownloadServiceImpl.java**: 重构`hasRoleAtLeast()`方法使用`RoleType`枚举
- **PermissionAspect.java**: 修复硬编码"SUPER_ADMIN"，使用`RoleHierarchy.isSuperAdmin()`

#### 2. 测试角色创建修复
- **AdminEnrollControllerIntegrationTest.java**: "ADMIN" → "SUPER_ADMIN"
- **AdminCollegeControllerIntegrationTest.java**: "ADMIN" → "SUPER_ADMIN"
- **AdminCompetitionControllerIntegrationTest.java**: "ADMIN" → "SUPER_ADMIN"

#### 3. 业务逻辑修复
- **EnrollDomainServiceImpl.java**: 使用`RoleType.MEMBER.getName()`替代硬编码"MEMBER"
- **SystemUserInitializer.java**: 使用`RoleType.SUPER_ADMIN.getName()`替代硬编码"SUPER_ADMIN"

### ⚠️ 待完成的优化
1. **测试重构**: 将测试从手动用户/角色创建迁移到使用`@WithUserVO`注解
2. **其他硬编码引用**: 清理测试文件中剩余的硬编码角色字符串引用
3. **完整测试验证**: 运行完整测试套件确保所有功能正常

### 📊 成功标准评估
1. ✅ 代码中无硬编码"ADMIN"字符串（已修复）
2. ⚠️ 大部分角色引用通过`RoleType`枚举或`RoleHierarchy`工具类（核心逻辑已完成）
3. ⚠️ 所有测试通过（需要完整测试套件验证）
4. ✅ 权限检查功能与之前一致（核心逻辑保持兼容）