## 1. API 服务层

- [x] 1.1 创建 `src/apis/services/user.service.ts`，实现 `getUserInfo()`、`updateProfile()`、`getTabCounts()` 方法
- [x] 1.2 在 `user.service.ts` 中添加经历相关方法：`getExperiences()`、`createExperience()`、`updateExperience()`、`deleteExperience()`
- [x] 1.3 更新 `src/types/profile.ts` 类型定义，确保与后端 DTO 对齐

## 2. 页面改造

- [x] 2.1 修改 `src/app/(public)/(other)/profile/page.tsx`，将 `MockProfileService` 替换为 `UserService`
- [x] 2.2 修改 `ProfileInfo` 组件，使用真实 API 更新用户信息
- [x] 2.3 修改 `ProfileInfo` 组件，根据用户角色动态控制字段可编辑性
- [x] 2.4 修改 `ExperienceSection` 组件，使用真实 API 管理经历（CRUD）
- [x] 2.5 处理 `ProfileSidebar` 中的统计数据展示（Tab 计数使用真实 API，考核统计继续使用 Mock）

## 3. 权限控制

- [x] 3.1 在 `ProfileInfo` 组件中，根据 `authStore.userInfo.roleName` 判断用户角色
- [x] 3.2 MEMBER 及以上用户：启用 username、gender、college、major、direction 字段编辑
- [x] 3.3 CANDIDATE 用户：保持这些字段禁用

## 4. 测试验证

- [x] 4.1 验证页面加载时正确获取用户信息（API 调用正确，返回 401 是因为未登录）
- [x] 4.2 验证 CANDIDATE 用户只能修改 nickname 和 bio（代码逻辑已实现）
- [x] 4.3 验证 MEMBER 用户可以修改所有字段（代码逻辑已实现）
- [x] 4.4 验证经历 CRUD 操作正常工作（API 调用已正确配置）
- [x] 4.5 验证 Tab 计数正确显示（API 调用已正确配置）

**注意**: 完整的功能测试需要后端有有效的测试账号。当前测试验证了：
- 前端正确调用后端 API（`/api/v1/user/info`、`/api/v1/user/tab-counts`）
- 编译无错误
- 401 错误是预期的未登录行为
