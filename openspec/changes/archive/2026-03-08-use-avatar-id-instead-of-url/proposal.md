## Why

当前成员列表接口和获取自身信息接口返回的是 `avatarUrl`（头像下载 URL），但这种方式存在以下问题：

1. **不一致性**：报名相关接口（如 `EnrollmentDetailDTO`）同时返回 `avatarId` 和 `avatarUrl`，而成员接口只返回 `avatarUrl`
2. **前端灵活性降低**：前端无法直接获取文件 ID，无法进行自定义处理（如预加载、缓存控制等）
3. **废弃字段**：代码中已有注释标记 `avatarUrl` 应该被弃用，应使用头像文件 ID

## What Changes

- **BREAKING** 移除成员相关 DTO 中的 `avatarUrl` 字段，替换为 `avatarFileId` 字段
- 受影响的 DTO：
  - `MemberBriefDTO` - 成员简要信息
  - `MemberDetailDTO` - 成员详细信息
  - `DirectionLeaderDTO.LeaderInfo` - 方向负责人信息
  - `UserInfo` - 用户基本信息（获取自身信息接口）
- **BREAKING** 移除 VO 层中的 `avatarUrl` 字段（`MemberVO`、`UserVO`）
- 更新 Converter 层，不再构建 URL，直接传递文件 ID
- 更新 Repository 层，不再查询 File 表获取 URL

## Capabilities

### New Capabilities

无新能力。

### Modified Capabilities

- `user-management`: 获取当前用户信息接口返回的 `avatarUrl` 字段变更为 `avatarFileId`
- `enrollment`: 报名相关 DTO 中的 `avatarUrl` 字段变更为 `avatarFileId`（保持一致性）

## Impact

### 受影响的文件

**API 层（DTO）**
- `src/main/java/com/bluenet/web/api/dto/member/MemberBriefDTO.java`
- `src/main/java/com/bluenet/web/api/dto/member/MemberDetailDTO.java`
- `src/main/java/com/bluenet/web/api/dto/member/DirectionLeaderDTO.java`
- `src/main/java/com/bluenet/web/api/dto/user/UserInfo.java`
- `src/main/java/com/bluenet/web/api/dto/enrollment/EnrollmentDetailDTO.java`
- `src/main/java/com/bluenet/web/api/dto/enrollment/EnrollmentBriefDTO.java`

**领域层（VO）**
- `src/main/java/com/bluenet/web/domain/model/vo/MemberVO.java`
- `src/main/java/com/bluenet/web/domain/model/vo/UserVO.java`
- `src/main/java/com/bluenet/web/domain/model/vo/EnrollVO.java`
- `src/main/java/com/bluenet/web/domain/model/vo/EnrollBriefVO.java`

**应用层（Converter）**
- `src/main/java/com/bluenet/web/application/converter/MemberConverter.java`
- `src/main/java/com/bluenet/web/application/converter/UserConverter.java`
- `src/main/java/com/bluenet/web/application/converter/EnrollConverter.java`

**基础设施层（Repository）**
- `src/main/java/com/bluenet/web/infrastructure/repository/impl/MemberRepositoryImpl.java`
- `src/main/java/com/bluenet/web/infrastructure/repository/impl/UserRepositoryImpl.java`
- `src/main/java/com/bluenet/web/infrastructure/repository/impl/EnrollRepositoryImpl.java`

**测试文件**
- 相关单元测试和集成测试需要更新

### API 变更

| 接口 | 旧字段 | 新字段 |
|------|--------|--------|
| GET /api/v1/members | avatarUrl | avatarFileId |
| GET /api/v1/members/{id} | avatarUrl | avatarFileId |
| GET /api/v1/members/direction-leaders | avatarUrl | avatarFileId |
| GET /api/v1/user/info | avatarUrl | avatarFileId |
| GET /api/v1/enrollments | avatarUrl | avatarFileId |
| GET /api/v1/enrollments/{id} | avatarUrl | avatarFileId |

### 前端适配

前端需要修改头像显示逻辑：
```javascript
// 旧方式
<img src={member.avatarUrl} />

// 新方式
<img src={`/api/v1/file/download/${member.avatarFileId}`} />
```
