## 1. DTO 层实现

- [x] 1.1 创建 `CreateEnrollmentRequestDTO` - 发起报名请求 DTO（含 `forceUpdate` 字段）
- [x] 1.2 创建 `EnrollmentBriefDTO` - 报名简要信息 DTO
- [x] 1.3 创建 `EnrollmentDetailDTO` - 报名详情 DTO
- [x] 1.4 创建 `EnrollmentStatisticsDTO` - 报名统计 DTO
- [x] 1.5 创建 `EnrollmentListQueryDTO` - 报名列表查询参数 DTO
- [x] 1.6 创建 `EnrollmentConflictDTO` - 学号冲突响应 DTO（409 返回）
- [x] 1.7 创建 `RejectEnrollmentRequestDTO` - 拒绝报名请求 DTO
- [x] 1.8 创建 `EnrollmentApprovalResultDTO` - 审核结果 DTO

## 2. 仓库层实现

- [x] 2.1 创建 `EnrollRepository` 接口
- [x] 2.2 实现 `EnrollRepositoryImpl` - 包含 VO 与 Entity 转换
- [x] 2.3 实现按状态和方向查询方法
- [x] 2.4 实现分页查询方法
- [x] 2.5 实现统计查询方法

## 3. 领域层实现

- [x] 3.1 创建 `EnrollVO` 值对象
- [x] 3.2 创建 `EnrollBriefVO` 值对象
- [x] 3.3 创建 `EnrollStatisticsVO` 值对象
- [x] 3.4 创建 `EnrollDomainService` 接口
- [x] 3.5 实现 `EnrollDomainServiceImpl`
- [x] 3.6 实现学号唯一性校验逻辑
- [x] 3.7 实现报名状态转换逻辑
- [x] 3.8 实现审核通过后创建用户逻辑（含头像复制）
- [x] 3.9 实现头像文件校验逻辑（验证 avatarId 对应的文件存在且类型为 avatar）

## 4. 应用层实现

- [x] 4.1 创建 `EnrollService` 接口
- [x] 4.2 实现 `EnrollServiceImpl`
- [x] 4.3 实现发起报名方法（含学号冲突检测与 forceUpdate 处理）
- [x] 4.4 实现分页查询报名列表方法
- [x] 4.5 实现获取报名详情方法
- [x] 4.6 实现审核通过方法
- [x] 4.7 实现审核拒绝方法
- [x] 4.8 实现获取统计数据方法
- [x] 4.9 实现 VO 与 DTO 转换

## 5. 控制层实现

- [x] 5.1 创建 `EnrollController` - 公开报名接口
- [x] 5.2 实现 POST `/api/v1/enrollments` - 发起报名（含冲突处理）
- [x] 5.3 创建 `AdminEnrollController` - 管理员报名接口
- [x] 5.4 实现 GET `/api/v1/admin/enrollments` - 分页查询列表
- [x] 5.5 实现 GET `/api/v1/admin/enrollments/{id}` - 获取详情
- [x] 5.6 实现 PUT `/api/v1/admin/enrollments/{id}/approve` - 通过报名
- [x] 5.7 实现 PUT `/api/v1/admin/enrollments/{id}/reject` - 拒绝报名
- [x] 5.8 实现 GET `/api/v1/admin/enrollments/statistics` - 统计数据

## 6. 权限配置

- [x] 6.1 添加报名相关权限常量（通过 @RequiresPermission 注解自动扫描）
- [x] 6.2 配置公开接口权限（无需登录）- enrollment:create
- [x] 6.3 配置管理员接口权限（需要 ADMIN 角色）- enrollment:list, enrollment:detail, enrollment:approve, enrollment:reject, enrollment:statistics
- [x] 6.4 权限自动同步到数据库（通过 PermissionScanner）

## 7. 测试实现

- [x] 7.1 创建 `EnrollRepositoryImplTest` - 仓库层单元测试
- [x] 7.2 创建 `EnrollDomainServiceImplTest` - 领域层单元测试
- [x] 7.3 创建 `EnrollServiceImplTest` - 应用层单元测试
- [x] 7.4 创建 `EnrollControllerIntegrationTest` - 公开接口集成测试
- [x] 7.5 创建 `AdminEnrollControllerIntegrationTest` - 管理接口集成测试

## 8. 文档与收尾

- [x] 8.1 添加 Swagger/OpenAPI 注解
- [x] 8.2 清理临时文件
- [x] 8.3 运行 lint 和 typecheck 验证
