## Why

系统 RBAC 角色体系存在严重不一致问题：规范定义的四个角色（SUPER_ADMIN、DIRECTION_ADMIN、MEMBER、CANDIDATE）在代码实现中被错误地替换或硬编码为不存在的 "ADMIN" 角色。这种不一致导致权限检查失效，可能引发安全漏洞和功能异常。

## What Changes

- **重构 FileDownloadServiceImpl**：将硬编码的 "ADMIN"/"MEMBER" 字符串检查替换为基于 RoleType 枚举和 RoleHierarchy 工具类的类型安全权限检查
- **修复测试角色创建**：更新所有创建错误 "ADMIN" 角色的测试代码，使用正确的角色名称（SUPER_ADMIN 或 DIRECTION_ADMIN）
- **统一角色引用**：清理代码库中所有硬编码角色字符串，确保只通过 RoleType 枚举引用角色
- **更新 @WithUserVO 注解使用**：同步修改所有测试中使用 @WithUserVO 注解的地方，确保角色名称与规范一致

## Capabilities

### New Capabilities
<!-- 无需新增能力，问题在于现有能力实现不一致 -->

### Modified Capabilities
- **rbac-role-management**：确保所有代码实现严格遵循规范定义的角色名称和层级关系，修复硬编码字符串问题
- **file-download-handler**：修改文件下载权限检查逻辑，使用 RoleHierarchy 工具类替代字符串比较
- **testing**：更新测试环境和测试用例，使用正确的角色体系进行权限测试

## Impact

- **修改类**：
  - `FileDownloadServiceImpl` - 重构权限检查方法
  - `AdminEnrollControllerIntegrationTest`, `AdminCollegeControllerIntegrationTest`, `AdminCompetitionControllerIntegrationTest` - 修复测试角色创建
  - 其他可能存在硬编码角色字符串的业务类
- **测试更新**：所有使用 @WithUserVO 注解的测试需要更新角色名称
- **权限逻辑**：文件下载权限检查将更严格遵循 RBAC 层级，但功能行为保持不变
- **兼容性**：不改变现有 API 接口，不影响前端调用，仅内部权限检查逻辑优化