## Context

当前系统已有 `tb_enroll` 数据库表和 `Enroll` 实体类、`EnrollMapper`，但缺少完整的 API 层、应用层、领域层和仓库层实现。报名系统需要支持外部用户提交报名申请，管理员审核报名，以及审核通过后自动创建用户账号的业务流程。

### 现有基础设施
- 数据库表 `tb_enroll` 已存在
- 实体类 `Enroll` 已定义
- `EnrollMapper` 已存在
- 枚举 `EnrollStatus` (pending/approved/rejected) 已定义
- 枚举 `Direction` (computer_vision/structural_design/embedded) 已定义

### 相关依赖
- `UserManagement` 领域服务：审核通过时创建用户账号
- `FileService`：头像文件处理
- `CollegeService`：学院信息查询

## Goals / Non-Goals

**Goals:**
- 实现完整的报名 CRUD API 接口
- 支持外部用户发起报名（无需登录）
- 支持重复学号检测与更新确认
- 支持管理员分页查询、筛选报名列表
- 支持管理员审核报名（通过/拒绝）
- 审核通过后自动创建用户账号
- 提供报名状态统计接口

**Non-Goals:**
- 不实现报名邮件通知（属于 message-notification 模块）
- 不实现考核系统关联（属于 evaluation-system 模块）
- 不实现批量导入/导出功能

## Decisions

### 1. API 设计

**公开接口（无需登录）**
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/v1/enrollments` | 发起报名（含 `forceUpdate` 字段处理冲突） |

**管理员接口（需要 ADMIN 角色）**
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/v1/admin/enrollments` | 分页查询报名列表 |
| GET | `/api/v1/admin/enrollments/{id}` | 获取报名详情 |
| PUT | `/api/v1/admin/enrollments/{id}/approve` | 通过报名 |
| PUT | `/api/v1/admin/enrollments/{id}/reject` | 拒绝报名 |
| GET | `/api/v1/admin/enrollments/statistics` | 报名状态统计 |

**理由**: 将公开接口与管理接口分离，便于权限控制和 API 管理。学号冲突通过 `forceUpdate` 字段处理，减少 API 调用次数。

### 1.1 接口详细定义

#### POST `/api/v1/enrollments` - 发起报名

**请求体** `CreateEnrollmentRequestDTO`:
```json
{
  "username": "张三",                    // 必填，真实姓名
  "studentId": "20210001001",           // 必填，学号 12-13 位
  "collegeId": 1,                       // 必填，学院ID
  "major": "计算机科学与技术",            // 必填，专业
  "grade": 2,                           // 必填，年级 1-6
  "direction": "computer_vision",       // 必填，方向枚举
  "avatarId": 123,                      // 可选，头像文件ID（需先调用文件上传接口获取）
  "internalReferralCode": "ABC12345",   // 可选，内推码（8位大写字母+数字）
  "forceUpdate": false                  // 可选，是否强制更新已有报名，默认 false
}
```

**响应**:

| 场景 | 状态码 | 响应体 |
|------|--------|--------|
| 新建成功 | `201 Created` | `EnrollmentBriefDTO` |
| 学号冲突 | `409 Conflict` | `EnrollmentConflictDTO` |
| 强制更新成功 | `200 OK` | `EnrollmentBriefDTO` |
| 参数校验失败 | `400 Bad Request` | `ResponseMessage<Void>` |

**201 成功响应** `EnrollmentBriefDTO`:
```json
{
  "code": 201,
  "msg": "报名成功",
  "data": {
    "id": 123,
    "username": "张三",
    "studentId": "20210001001",
    "direction": "computer_vision",
    "status": "pending",
    "createdAt": "2026-02-28T10:00:00"
  }
}
```

**409 冲突响应** `EnrollmentConflictDTO`:
```json
{
  "code": 409,
  "msg": "学号已存在，是否更新报名信息？",
  "data": {
    "id": 123,
    "username": "张三",
    "status": "pending",
    "direction": "computer_vision",
    "createdAt": "2026-02-28T10:00:00"
  }
}
```

---

#### GET `/api/v1/admin/enrollments` - 分页查询报名列表

**请求参数** `EnrollmentListQueryDTO`:
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| page | int | 否 | 页码，默认 0 |
| size | int | 否 | 每页数量，默认 20，最大 100 |
| status | string | 否 | 状态筛选：pending/approved/rejected |
| direction | string | 否 | 方向筛选：computer_vision/structural_design/embedded |
| keyword | string | 否 | 关键词搜索（姓名/学号） |

**响应** `Page<EnrollmentBriefDTO>`:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": 123,
        "username": "张三",
        "studentId": "20210001001",
        "collegeName": "计算机学院",
        "major": "计算机科学与技术",
        "grade": 2,
        "direction": "computer_vision",
        "status": "pending",
        "avatarId": 456,
        "createdAt": "2026-02-28T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

---

#### GET `/api/v1/admin/enrollments/{id}` - 获取报名详情

**路径参数**:
| 参数 | 类型 | 描述 |
|------|------|------|
| id | long | 报名ID |

**响应** `EnrollmentDetailDTO`:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123,
    "username": "张三",
    "studentId": "20210001001",
    "collegeId": 1,
    "collegeName": "计算机学院",
    "major": "计算机科学与技术",
    "grade": 2,
    "direction": "computer_vision",
    "status": "pending",
    "avatarId": 456,
    "avatarUrl": "/api/v1/files/456",
    "internalReferralCode": "ABC12345",
    "referralUserName": "李四",          // 内推人姓名（如果有）
    "createdAt": "2026-02-28T10:00:00",
    "updatedAt": "2026-02-28T10:00:00"
  }
}
```

---

#### PUT `/api/v1/admin/enrollments/{id}/approve` - 通过报名

**路径参数**:
| 参数 | 类型 | 描述 |
|------|------|------|
| id | long | 报名ID |

**响应**:
```json
{
  "code": 200,
  "msg": "审核通过，账号已发放",
  "data": {
    "id": 123,
    "status": "approved",
    "createdUserId": 456              // 新创建的用户ID
  }
}
```

**业务逻辑**:
1. 更新报名状态为 `approved`
2. 检查学号是否已存在用户
3. 若不存在，创建用户账号（使用报名信息，角色设为 `member`）
4. 报名时的头像自动应用到用户账号

---

#### PUT `/api/v1/admin/enrollments/{id}/reject` - 拒绝报名

**路径参数**:
| 参数 | 类型 | 描述 |
|------|------|------|
| id | long | 报名ID |

**请求体** `RejectEnrollmentRequestDTO`:
```json
{
  "reason": "面试未通过"              // 可选，拒绝原因
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "已拒绝",
  "data": {
    "id": 123,
    "status": "rejected"
  }
}
```

---

#### GET `/api/v1/admin/enrollments/statistics` - 报名统计

**响应** `EnrollmentStatisticsDTO`:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "byStatus": {
      "pending": 50,
      "approved": 30,
      "rejected": 20
    },
    "byDirection": {
      "computer_vision": 40,
      "structural_design": 35,
      "embedded": 25
    }
  }
}
```

### 2. 分层架构

遵循项目 DDD 四层架构：
```
Controller (DTO)
    ↓
Application Service (DTO ↔ VO 转换)
    ↓
Domain Service (VO)
    ↓
Repository (VO ↔ Entity 转换)
    ↓
Mapper (Entity)
```

**理由**: 符合项目现有架构规范，保持一致性。

### 3. 重复学号处理流程

```
用户提交报名 (forceUpdate=false/未传)
    ↓
检查学号是否存在
    ├── 不存在 → 创建报名 → 返回 201 Created
    └── 存在 → 返回 409 Conflict + 现有报名信息
                    ↓
         前端提示用户确认更新
                    ↓
         用户确认后重新提交 (forceUpdate=true)
                    ↓
         更新现有报名 → 返回 200 OK
```

**理由**: 通过单一接口处理冲突，减少 API 调用次数，前端体验更流畅。详细请求/响应格式见 1.1 接口详细定义。

### 4. 审核通过后创建用户

审核通过时：
1. 检查学号是否已存在用户账号
2. 若不存在，使用报名信息创建用户账号
3. 设置默认角色为 `member`
4. 将报名时的头像（avatarId）复制到用户账号
5. 发送账号创建通知（可选，由 message-notification 模块实现）

**理由**: 自动化用户创建流程，减少管理员手动操作。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 恶意批量报名攻击 | 添加 IP 限流、验证码校验 |
| 审核通过时学号冲突 | 先检查用户表，若已存在则跳过创建，记录日志 |
| 头像文件丢失 | 头像为可选字段，不影响报名流程 |
| 并发审核冲突 | 使用乐观锁或数据库行锁 |

## Migration Plan

1. **部署前**: 无数据库迁移需求，表结构已存在
2. **部署**: 滚动更新后端服务
3. **回滚**: 直接回滚代码即可，无数据迁移
