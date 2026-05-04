## 1. API 层 DTO 修改

- [x] 1.1 修改 `MemberBriefDTO`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 1.2 修改 `MemberDetailDTO`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 1.3 修改 `DirectionLeaderDTO.LeaderInfo`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 1.4 修改 `UserInfo`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 1.5 修改 `EnrollmentDetailDTO`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 1.6 修改 `EnrollmentBriefDTO`：移除 `avatarUrl`（如果有），添加 `avatarFileId` 字段

## 2. 领域层 VO 修改

- [x] 2.1 修改 `MemberVO`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 2.2 修改 `UserVO`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 2.3 修改 `EnrollVO`：移除 `avatarUrl`，添加 `avatarFileId` 字段
- [x] 2.4 修改 `EnrollBriefVO`：移除 `avatarUrl`（如果有），添加 `avatarFileId` 字段

## 3. 应用层 Converter 修改

- [x] 3.1 修改 `MemberConverter`：将 `avatarUrl` 改为 `avatarFileId` 映射
- [x] 3.2 修改 `UserConverter`：将 `avatarUrl` 改为 `avatarFileId` 映射
- [x] 3.3 修改 `EnrollConverter`（如果存在）：更新字段映射

- [x] 3.4 更新 `EnrollServiceImpl`：更新字段映射

- [x] 3.5 更新 `EnrollDomainServiceImpl`：更新字段映射

## 4. 基础设施层 Repository 修改
- [x] 4.1 修改 `MemberRepositoryImpl`：移除 File 表查询，直接返回 `avatarId`
- [x] 4.2 修改 `UserRepositoryImpl`：移除 File 表查询，直接返回 `avatarId`
- [x] 4.3 修改 `EnrollRepositoryImpl`：移除 `getAvatarUrl` 方法，直接返回 `avatarId`

## 5. 测试更新
- [x] 5.1 更新 `MemberConverterTest`：修改断言使用 `avatarFileId`
- [x] 5.2 更新 `MemberServiceImplTest`：修改断言使用 `avatarFileId`
- [x] 5.3 更新 `MemberDomainServiceImplTest`：修改断言使用 `avatarFileId`
- [x] 5.4 更新 `UserInfoControllerTest`：修改断言使用 `avatarFileId`
- [x] 5.5 更新 `MemberControllerIntegrationTest`：无需修改（未直接使用 avatar 字段）
- [x] 5.6 更新 `EnrollRepositoryImplTest`：移除 `avatarUrl` 相关测试
- [x] 5.7 更新 `EnrollDomainServiceImplTest`：无需修改（使用 `avatarId`）
- [x] 5.8 更新 `EnrollServiceImplTest`：修改断言使用 `avatarFileId`

## 6. 验证与清理
- [x] 6.1 运行所有单元测试验证通过
- [x] 6.2 运行所有集成测试验证通过
- [x] 6.3 检查 API 文档（OpenAPI）注解是否正确
- [x] 6.4 代码格式化：`./mvnw spotless:apply`
