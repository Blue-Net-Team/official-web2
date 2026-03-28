## Context

个人主页是蓝网官网的核心用户功能模块，前端已完成Mock数据设计和页面开发。当前需要后端提供REST API接口，将前端Mock数据与后端PostgreSQL数据库对接。

### 现有数据库结构
- `tb_user`: 用户表，包含基本信息（学号、邮箱、姓名、学院、专业、方向等）
- `tb_user_experience`: 用户经历表，包含type（项目/竞赛/实习）、title、content、startTime、endTime

### 前端数据结构
前端定义了以下核心类型：
- `UserProfile`: 用户基本信息
- `TabCounts`: Tab计数
- `Project/Competition/Internship`: 三种经历类型

## Goals / Non-Goals

**Goals:**
- 提供用户画像API，支持前端基本信息展示和更新
- 提供经历管理API，支持项目、竞赛/实习三种类型的CRUD
- 复用现有数据库字段，最小化数据库变更
- 遵循DDD四层架构规范

**Non-Goals:**
- 不实现邮箱验证功能（绑定邮箱时验证属于独立功能，有邮箱即表示已验证）
- 不实现头像上传功能（已有FileDomainService）
- 不实现权限管理变更
- 不实现考核相关功能（本次变更不包含）
- 不实现GitHub链接功能（暂不支持）

## Decisions

### 1. 数据库字段扩展

**决定**: 在`tb_user`表新增1个字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `bio` | TEXT | 个人简介 |

**理由**: 前端需要个人简介字段，现有数据库不支持。

**备选方案**: 创建独立的`tb_user_profile`扩展表
**拒绝理由**: 字段较少，直接扩展更简单，避免JOIN查询

**关于邮箱验证**: 绑定邮箱时必须验证，因此有邮箱即表示已验证，无需额外字段。

### 2. 经历表Content字段设计

**决定**: 使用JSON格式存储不同类型经历的详细内容

| 经历类型 | title | content (JSON) |
|----------|-------|----------------|
| 项目(project) | 项目名称 | `{role, description, techStack[], demoUrl}` |
| 竞赛(competition) | 竞赛名称 | `{level, award, teamSize, description, certificateUrl}` |
| 实习(internship) | 公司名称 | `{position, description, achievements[]}` |

**字段说明**:
- **竞赛经历**:
  - `level`: 竞赛级别（市级/省级/国家级等）
  - `award`: 获奖等级（一等奖/二等奖/三等奖等）
- **实习经历**:
  - `position`: 实习岗位

**理由**:
- 复用现有`tb_user_experience`表结构
- 不同类型经历字段差异大，JSON灵活存储
- 前端可直接解析JSON渲染

**备选方案**: 为每种经历创建独立表
**拒绝理由**: 增加表数量，查询复杂，不符合现有设计

### 3. 模块组织结构

**决定**: 将功能整合到现有User模块，而非创建独立的Profile模块

#### 控制层组织
| 控制器 | 路径前缀 | 职责 |
|--------|----------|------|
| `UserInfoController` | `/api/v1/user/profile` | 用户画像查询与更新（扩展现有控制器） |
| `UserExperienceController` | `/api/v1/user/experiences` | 用户经历CRUD（新建控制器） |

#### 应用层组织
| 服务类 | 职责 |
|--------|------|
| `UserInfoService` | 扩展，新增getProfile、updateProfile方法 |
| `UserExperienceService` | 新建，处理用户经历业务逻辑 |

#### 领域层组织
| 服务类 | 职责 |
|--------|------|
| `UserDomainService` | 扩展，新增用户画像查询和更新方法 |
| `UserExperienceDomainService` | 新建，处理用户经历领域逻辑 |

#### 仓库层组织
| 仓库类 | 职责 |
|--------|------|
| `UserRepository` | 扩展，新增查询用户画像方法 |
| `UserExperienceRepository` | 新建，处理用户经历数据操作 |

**理由**:
- 用户画像是用户模块的自然扩展
- 遵循现有项目结构，降低学习成本
- 避免过度设计，保持代码组织简洁

## Risks / Trade-offs

### Risk 1: JSON字段查询性能
**风险**: `content`字段使用JSON存储，无法直接索引
**缓解**: 经历数据量有限（每用户约10-20条），全表扫描可接受

### Risk 2: 前后端数据格式不一致
**风险**: JSON字段格式变化可能导致前端解析失败
**缓解**:
- 在VO层定义明确的JSON结构类
- 使用Jackson注解确保序列化一致
- API文档明确JSON格式

## Testing Requirements

### 单元测试

#### 领域层测试
- **UserDomainService扩展方法测试**
  - 测试用户画像查询逻辑
  - 测试用户基本信息更新逻辑
  - 测试年级计算逻辑
  - 测试方向枚举转换

- **UserExperienceDomainService测试**
  - 测试经历创建逻辑（三种类型）
  - 测试经历更新逻辑
  - 测试经历删除逻辑
  - 测试权限校验（只能操作自己的经历）
  - 测试JSON内容序列化/反序列化

#### 仓库层测试
- **UserRepository扩展方法测试**
  - 测试用户画像数据查询
  - 测试Tab计数计算

- **UserExperienceRepository测试**
  - 测试按类型过滤查询
  - 测试CRUD操作

### 集成测试

#### UserInfoController扩展接口测试
- `GET /api/v1/user/profile`
  - 已登录用户成功获取画像
  - 未登录用户返回401
  - 验证返回字段完整性
  - 验证Tab计数正确性

- `PUT /api/v1/user/profile`
  - 成功更新基本信息
  - 部分字段更新
  - 未登录用户返回401

#### UserExperienceController测试
- `GET /api/v1/user/experiences`
  - 成功获取所有经历
  - 按类型过滤
  - 未登录用户返回401

- `POST /api/v1/user/experiences`
  - 成功创建项目经历
  - 成功创建竞赛经历
  - 成功创建实习经历
  - 验证JSON内容存储格式
  - 未登录用户返回401

- `PUT /api/v1/user/experiences/{id}`
  - 成功更新自己的经历
  - 更新他人经历返回404
  - 未登录用户返回401

- `DELETE /api/v1/user/experiences/{id}`
  - 成功删除自己的经历
  - 删除他人经历返回404
  - 未登录用户返回401

### 测试覆盖率要求
- 领域层：覆盖率 ≥ 80%
- 应用层：覆盖率 ≥ 80%
- 控制层：覆盖率 ≥ 70%

### 测试数据准备
- 创建测试用户（不同角色、不同方向）
- 创建测试经历数据（三种类型各若干条）
- 准备边界测试数据（空值、超长字符串等）

## Migration Plan

### 数据库迁移
```sql
-- V14__add_profile_fields.sql
ALTER TABLE tb_user ADD COLUMN bio TEXT;

COMMENT ON COLUMN tb_user.bio IS '个人简介';
```

### 部署步骤
1. 执行数据库迁移
2. 部署后端服务
3. 前端切换Mock到真实API

### 回滚策略
1. 前端切回Mock数据
2. 回滚后端服务
3. 执行数据库回滚（删除新增字段）
