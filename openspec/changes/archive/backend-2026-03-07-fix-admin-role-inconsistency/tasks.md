## 1. 代码分析和准备

- [x] 1.1 扫描全代码库，识别所有硬编码角色字符串（"ADMIN", "MEMBER", "DIRECTION_ADMIN", "SUPER_ADMIN", "CANDIDATE"）
- [x] 1.2 创建修复清单，列出需要修改的文件和具体问题
- [x] 1.3 备份现有测试数据，准备回滚方案
- [x] 1.4 验证现有测试套件正常运行，建立基准

## 2. FileDownloadServiceImpl 重构

- [x] 2.1 修改 `hasRoleAtLeast` 方法签名：从 `hasRoleAtLeast(UserVO user, String minRole)` 改为 `hasRoleAtLeast(UserVO user, RoleType minRole)`
- [x] 2.2 实现新的 `hasRoleAtLeast` 方法，使用 `RoleType.fromName()` 和 `RoleHierarchy.hasRoleLevel()`
- [x] 2.3 更新所有调用 `hasRoleAtLeast` 的地方，使用 `RoleType` 枚举常量
- [x] 2.4 移除硬编码的 "ADMIN" 和 "MEMBER" 字符串比较逻辑
- [x] 2.5 验证文件下载权限检查逻辑仍然正确工作

## 3. 测试角色创建修复

- [x] 3.1 修复 `AdminEnrollControllerIntegrationTest`：将创建 "ADMIN" 角色的代码改为创建 "SUPER_ADMIN" 角色
- [x] 3.2 修复 `AdminCollegeControllerIntegrationTest`：将创建 "ADMIN" 角色的代码改为创建 "SUPER_ADMIN" 角色
- [x] 3.3 修复 `AdminCompetitionControllerIntegrationTest`：将创建 "ADMIN" 角色的代码改为创建 "SUPER_ADMIN" 角色
- [x] 3.4 检查其他集成测试中是否创建错误的 "ADMIN" 角色，并修复

## 4. @WithUserVO 注解更新

- [x] 4.1 搜索所有使用 `@WithUserVO(roleName = "ADMIN")` 注解的测试
- [x] 4.2 根据测试场景，将 "ADMIN" 替换为 "SUPER_ADMIN" 或 "DIRECTION_ADMIN"
- [x] 4.3 验证所有 `@WithUserVO` 注解使用的角色名称都是有效的（SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, CANDIDATE）
- [x] 4.4 更新测试文档，说明正确的角色名称使用方法

## 5. 代码清理和验证

- [x] 5.1 搜索并清理业务代码中的硬编码角色字符串（权限检查、业务规则等）
- [x] 5.2 确保所有角色引用都通过 `RoleType` 枚举或 `RoleHierarchy` 工具类
- [x] 5.3 更新相关日志和异常消息，使用正确的角色名称
- [x] 5.4 运行静态代码分析，确保没有遗漏的硬编码字符串

## 6. 测试和验证

- [x] 6.1 运行完整的单元测试套件，确保所有测试通过
- [x] 6.2 运行集成测试，特别关注文件下载、报名审核、成员管理等权限相关功能
- [x] 6.3 执行端到端测试，验证核心业务流程正常工作
- [x] 6.4 检查测试覆盖率，确保权限相关代码有足够的测试覆盖
- [x] 6.5 验证生产环境模拟测试，确保权限检查逻辑正确

## 7. 文档和代码审查

- [x] 7.1 更新相关代码注释，说明角色引用最佳实践
- [x] 7.2 添加开发者文档，说明如何使用 RoleType 和 RoleHierarchy 进行权限检查
- [x] 7.3 进行代码审查，确保所有修改符合项目编码规范
- [x] 7.4 更新变更日志，记录角色一致性修复