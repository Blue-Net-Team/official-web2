## Context

当前系统已有 `tb_user` 数据库表和 `User` 实体类、`UserMapper`，以及基础的 `UserInfoController`（仅实现 `/me` 接口）。蓝网团队官网需要对外展示团队成员信息，访客需要能够查看团队成员列表，并按方向筛选成员。

### 现有基础设施
- 数据库表 `tb_user` 已存在
- 实体类 `User` 已定义
- `UserMapper` 已存在
- 枚举 `Direction` (COMPUTER_VISION/STRUCTURAL_DESIGN/EMBEDDED) 已定义
- 枚举 `RoleType` (SUPER_ADMIN/DIRECTION_ADMIN/MEMBER/CANDIDATE) 已定义
- `UserVO` 值对象已定义
- `UserInfo` DTO 已定义

### 相关依赖
- `FileService`：头像URL生成
- `CollegeService`：学院信息查询

## Goals / Non-Goals

**Goals:**
- 实现公开的团队成员列表查询接口（无需登录）
- 支持分页查询
- 支持按方向筛选成员
- 仅返回已启用且具有角色（MEMBER及以上）的成员
- 实现方向负责人查询接口

**Non-Goals:**
- 不实现管理员用户管理接口（属于 user-management 模块）
- 不实现用户编辑功能
- 不实现用户禁用/启用功能

## Decisions

### 1. API 设计

**公开接口（无需登录）**
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/v1/members` | 分页查询团队成员列表 |
| GET | `/api/v1/members/{id}` | 获取成员详情 |
| GET | `/api/v1/members/direction-leaders` | 获取各方向负责人 |

**理由**: 团队成员信息为公开信息，访客无需登录即可查看。接口路径使用 `/members` 而非 `/users`，语义更清晰。

### 1.1 接口详细定义

#### GET `/api/v1/members` - 分页查询团队成员列表

**请求参数** `MemberListQueryDTO`:
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| page | int | 否 | 页码，默认 0 |
| size | int | 否 | 每页数量，默认 20，最大 100 |
| direction | string | 否 | 方向筛选：computer_vision/structural_design/embedded |

**响应** `Page<MemberBriefDTO>`:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": 123,
        "username": "张三",
        "nickname": "小张",
        "direction": "computer_vision",
        "job": "后端开发",
        "avatarUrl": "/api/v1/files/456",
        "college": "计算机学院",
        "major": "计算机科学与技术",
        "enrollmentYear": 2021
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

**业务规则**:
- 仅返回 `disable = false` 的用户
- 仅返回角色级别 >= MEMBER 的用户（排除 CANDIDATE）
- 按入学年份降序排列（新人在前）
- 入学年份从学号前4位推断（如 `20210001001` → 入学年份 2021）

---

#### GET `/api/v1/members/{id}` - 获取成员详情

**路径参数**:
| 参数 | 类型 | 描述 |
|------|------|------|
| id | long | 成员ID |

**响应** `MemberDetailDTO`:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123,
    "username": "张三",
    "nickname": "小张",
    "direction": "computer_vision",
    "job": "后端开发",
    "avatarUrl": "/api/v1/files/456",
    "college": "计算机学院",
    "major": "计算机科学与技术",
    "gender": "male",
    "githubUsername": "zhangsan",
    "wechatQrcode": "/api/v1/files/789"
  }
}
```

**业务规则**:
- 若用户不存在或被禁用，返回 404
- 若用户角色 < MEMBER，返回 404

---

#### GET `/api/v1/members/direction-leaders` - 获取各方向负责人

**响应** `List<DirectionLeaderDTO>`:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "direction": "computer_vision",
      "directionName": "计算机视觉",
      "leader": {
        "id": 1,
        "username": "李四",
        "nickname": "视觉组长",
        "avatarUrl": "/api/v1/files/100"
      }
    },
    {
      "direction": "structural_design",
      "directionName": "结构设计",
      "leader": {
        "id": 2,
        "username": "王五",
        "nickname": "结构组长",
        "avatarUrl": "/api/v1/files/101"
      }
    },
    {
      "direction": "embedded",
      "directionName": "嵌入式开发",
      "leader": null
    }
  ]
}
```

**业务规则**:
- 方向负责人判定：`role = DIRECTION_ADMIN` 或 `SUPER_ADMIN` 且 `direction` 字段对应
- 若某方向暂无负责人，`leader` 字段为 `null`
- 始终返回所有三个方向

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

### 3. 成员筛选逻辑

```sql
SELECT * FROM tb_user u
JOIN tb_role r ON u.role_id = r.id
WHERE u.deleted = false
  AND u.disable = false
  AND r.name IN ('MEMBER', 'DIRECTION_ADMIN', 'SUPER_ADMIN')
  AND (:direction IS NULL OR u.direction = :direction)
ORDER BY LEFT(u.student_id, 4) DESC, u.id ASC
```

**理由**:
- 通过角色名称过滤，确保只展示正式团队成员
- 按学号前4位（入学年份）降序排列，新人在前
- 同年级按 ID 升序，保证排序稳定性

### 4. 方向负责人判定逻辑

```
方向负责人 = 用户.role_id 对应的角色级别 >= DIRECTION_ADMIN
         AND 用户.direction = 该方向
```

**理由**: 根据产品文档定义，方向负责人为 `role = ADMIN` 且 `direction` 字段对应的用户。结合现有 RoleType 枚举，DIRECTION_ADMIN 和 SUPER_ADMIN 都可作为方向负责人。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 大量请求导致性能问题 | 添加缓存，设置合理的分页限制 |
| 敏感信息泄露 | DTO 仅包含公开字段，不返回邮箱、学号等敏感信息 |
| 头像文件丢失 | 头像URL由FileService生成，支持默认头像 |

## Migration Plan

1. **部署前**: 无数据库迁移需求，表结构已存在
2. **部署**: 滚动更新后端服务
3. **回滚**: 直接回滚代码即可，无数据迁移
