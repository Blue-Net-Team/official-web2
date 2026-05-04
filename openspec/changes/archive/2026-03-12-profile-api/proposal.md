## Why

个人主页是用户管理个人信息、维护项目/竞赛/实习经历的核心功能模块。当前前端已完成Mock数据设计和页面开发，需要后端提供对应的REST API接口，将前端Mock数据与后端真实数据库对接，实现用户画像的完整功能。

## What Changes

- 新增用户画像API，返回用户基本信息和各Tab计数
- 新增用户基本信息更新API，支持修改昵称、个人简介等
- 新增用户经历（项目/竞赛/实习）的CRUD API
- 扩展`tb_user`表，新增`bio`（个人简介）字段

## Capabilities

### New Capabilities

- `user-profile`: 用户画像能力，包含用户基本信息查询与更新
- `user-experience`: 用户经历管理能力，支持项目、竞赛、实习三种类型经历的CRUD操作

### Modified Capabilities

无现有能力修改

## Impact

### 数据库变更
- `tb_user`表新增字段：`bio`(TEXT)

### API接口

#### 用户画像接口（UserInfoController）
路径前缀：`/api/v1/user/profile`
- `GET /api/v1/user/profile` - 获取用户画像
- `PUT /api/v1/user/profile` - 更新用户基本信息

#### 用户经历接口（UserExperienceController）
路径前缀：`/api/v1/user/experiences`
- `GET /api/v1/user/experiences` - 获取经历列表（支持type参数过滤）
- `POST /api/v1/user/experiences` - 创建经历
- `PUT /api/v1/user/experiences/{id}` - 更新经历
- `DELETE /api/v1/user/experiences/{id}` - 删除经历

### 涉及模块

#### 控制层
- `UserInfoController`：扩展，新增用户画像相关接口
- `UserExperienceController`：新增，处理用户经历CRUD

#### 应用层
- `UserInfoService`：扩展，新增用户画像查询和更新方法
- `UserExperienceService`：新增，处理用户经历业务逻辑

#### 领域层
- `UserDomainService`：扩展，新增用户画像查询和更新方法
- `UserExperienceDomainService`：新增，处理用户经历领域逻辑

#### 仓库层
- `UserRepository`：扩展，新增查询用户画像方法
- `UserExperienceRepository`：新增，处理用户经历数据操作
