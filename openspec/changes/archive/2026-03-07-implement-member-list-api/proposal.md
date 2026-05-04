## Why

当前系统已有 `tb_user` 数据库表和基础实体定义，但缺少团队成员列表查询接口。蓝网团队官网需要对外展示团队成员信息，访客需要能够查看团队成员列表，并按方向筛选成员。需要实现公开的团队成员列表查询接口，支撑官网团队成员展示功能。

## What Changes

- 新增公开的团队成员列表查询接口（支持分页）
- 支持按方向筛选成员（结构设计/嵌入式开发/计算机视觉）
- 仅返回已启用且具有角色的成员（非禁用用户）
- 返回成员基本信息：姓名、方向、头像、职责等
- 新增方向负责人查询接口（获取各方向的负责人信息）

## Capabilities

### New Capabilities

- `member-list-api`: 团队成员列表 REST API 接口层，包含公开的成员列表查询、方向筛选、方向负责人查询等功能

### Modified Capabilities

- `user-management`: 扩展现有用户管理规格，新增公开 API 层面的需求定义

## Impact

- **新增文件**:
  - `MemberController.java` - 团队成员控制器（公开接口）
  - `MemberService.java` / `MemberServiceImpl.java` - 成员应用服务
  - `MemberDomainService.java` / `MemberDomainServiceImpl.java` - 成员领域服务
  - `MemberRepository.java` - 成员仓库接口
  - 相关 DTO 类（`MemberBriefDTO`、`MemberDetailDTO`、`DirectionLeaderDTO`）
- **修改文件**:
  - 权限初始化脚本（新增成员列表相关权限）
- **依赖**:
  - 复用现有 `User` 实体、`UserMapper`
  - 复用现有 `UserVO`、`UserConverter`
  - 依赖 `FileService`（头像URL生成）
