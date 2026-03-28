## Why

当前系统已有 `tb_enroll` 数据库表和基础实体定义，但缺少完整的报名 API 接口实现。外部用户无法通过前端提交报名申请，管理员也无法通过系统审核报名。需要实现完整的报名生命周期管理接口，支撑蓝网团队招新业务流程。

## What Changes

- 新增外部用户发起报名接口（支持重复学号检测与更新确认）
- 新增管理员分页查询报名列表接口（支持按状态、方向筛选）
- 新增管理员查看报名详情接口
- 新增管理员审核报名接口（通过/拒绝）
- 新增报名状态统计接口（用于仪表盘展示）
- 实现报名通过后自动创建用户账号的业务逻辑

## Capabilities

### New Capabilities

- `enrollment-api`: 报名 REST API 接口层，包含发起报名、查询报名列表、查看详情、审核报名等接口

### Modified Capabilities

- `enrollment`: 扩展现有报名规格，新增 API 层面的需求定义，包括接口契约、权限控制、异常处理等

## Impact

- **新增文件**:
  - `EnrollController.java` - 报名控制器
  - `AdminEnrollController.java` - 管理员报名管理控制器
  - `EnrollService.java` / `EnrollServiceImpl.java` - 报名应用服务
  - `EnrollDomainService.java` / `EnrollDomainServiceImpl.java` - 报名领域服务
  - `EnrollRepository.java` - 报名仓库接口
  - 相关 DTO 类（请求/响应）
- **修改文件**:
  - 权限初始化脚本（新增报名相关权限）
- **依赖**:
  - 复用现有 `Enroll` 实体、`EnrollMapper`
  - 依赖 `UserManagement` 领域服务（审核通过时创建用户）
  - 依赖 `FileService`（头像文件处理）
