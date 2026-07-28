# User Experience Specification

## Purpose

定义用户经历管理功能，支持用户维护项目经历和实习经历的增删改查。
## Requirements
### Requirement: 项目经历展示

系统SHALL在"项目经历"Tab展示用户的项目经历列表，每个项目显示名称、角色、时间、描述、技术栈和链接。

#### Scenario: 展示项目经历卡片
- **WHEN** 用户访问"项目经历"Tab
- **THEN** 系统展示项目卡片列表，每个卡片包含：
  - 项目名称
  - 角色（如：项目负责人、核心开发）
  - 时间范围（如：2024.09 - 2025.01）
  - 项目描述
  - 技术标签列表
  - GitHub链接和项目演示链接（如有）
  - 编辑和删除按钮

#### Scenario: 空状态展示
- **WHEN** 用户没有任何项目经历
- **THEN** 系统展示空状态提示和"添加项目"引导

### Requirement: 项目经历添加

系统SHALL允许用户添加新的项目经历。

#### Scenario: 添加项目
- **WHEN** 用户点击"添加项目"按钮
- **THEN** 系统显示项目表单（或模态框）
- **AND** 用户填写项目名称、角色、时间、描述、技术栈、链接
- **AND** 保存后项目出现在列表中

#### Scenario: 添加项目必填验证
- **WHEN** 用户提交项目表单时缺少必填字段（项目名称、角色、时间、描述）
- **THEN** 系统显示验证错误提示

### Requirement: 项目经历编辑

系统SHALL允许用户编辑已有的项目经历。

#### Scenario: 编辑项目
- **WHEN** 用户点击项目的编辑按钮
- **THEN** 系统显示预填充当前数据的项目表单
- **AND** 用户修改后保存，更新项目信息

### Requirement: 项目经历删除

系统SHALL允许用户删除已有的项目经历。

#### Scenario: 删除项目
- **WHEN** 用户点击项目的删除按钮
- **THEN** 系统显示删除确认提示
- **AND** 确认后项目从列表中移除

### Requirement: 竞赛经历展示
系统SHALL在"竞赛经历"Tab展示用户的竞赛经历列表，每个竞赛显示名称、角色、时间、获奖等级、团队人数和描述。

#### Scenario: 展示竞赛经历卡片
- **WHEN** 用户访问"竞赛经历"Tab
- **THEN** 系统展示竞赛卡片列表，每个卡片包含：
  - 竞赛名称
  - 角色（如：团队负责人、技术负责人）
  - 获奖等级Badge（一等奖/二等奖/三等奖，对应金/银/铜色）
  - 竞赛时间
  - 团队人数
  - 竞赛描述
  - 获奖证书链接（如有）
  - 编辑和删除按钮

### Requirement: 竞赛经历添加
系统SHALL允许用户添加新的竞赛经历。

#### Scenario: 添加竞赛
- **WHEN** 用户点击"添加竞赛"按钮
- **THEN** 系统显示竞赛表单
- **AND** 用户填写竞赛名称、角色、时间、获奖等级、团队人数、描述、证书链接
- **AND** 保存后竞赛出现在列表中

### Requirement: 竞赛经历编辑和删除
系统SHALL允许用户编辑和删除竞赛经历。

#### Scenario: 编辑竞赛
- **WHEN** 用户点击竞赛的编辑按钮
- **THEN** 系统显示预填充当前数据的竞赛表单

#### Scenario: 删除竞赛
- **WHEN** 用户点击竞赛的删除按钮并确认
- **THEN** 竞赛从列表中移除

### Requirement: 实习经历展示

系统SHALL在"实习经历"Tab展示用户的实习经历列表，每条记录显示公司、职位、时间、状态、描述和主要成就。

#### Scenario: 展示实习经历卡片
- **WHEN** 用户访问"实习经历"Tab
- **THEN** 系统展示实习卡片列表，每个卡片包含：
  - 公司名称
  - 职位
  - 时间范围
  - 状态Badge（在职/已离职）
  - 工作描述
  - 主要成就（如有）
  - 编辑和删除按钮

#### Scenario: 展示在职状态
- **WHEN** 实习状态为"在职"
- **THEN** 状态Badge显示绿色渐变

#### Scenario: 展示已离职状态
- **WHEN** 实习状态为"已离职"
- **THEN** 状态Badge显示灰色

### Requirement: 实习经历添加

系统SHALL允许用户添加新的实习经历。

#### Scenario: 添加实习
- **WHEN** 用户点击"添加实习"按钮
- **THEN** 系统显示实习表单
- **AND** 用户填写公司、职位、时间、状态、描述、主要成就
- **AND** 保存后实习记录出现在列表中

### Requirement: 实习经历编辑和删除

系统SHALL允许用户编辑和删除实习经历。

#### Scenario: 编辑实习
- **WHEN** 用户点击实习的编辑按钮
- **THEN** 系统显示预填充当前数据的实习表单

#### Scenario: 删除实习
- **WHEN** 用户点击实习的删除按钮并确认
- **THEN** 实习记录从列表中移除

### Requirement: 前端可获取用户经历列表

前端 SHALL 通过 `GET /api/v1/user/experiences` 获取当前用户的项目/实习经历列表，支持按类型过滤。

#### Scenario: 获取全部经历
- **WHEN** 用户访问个人主页经历相关 Tab
- **THEN** 系统调用 `getExperiences()` API
- **AND** 展示真实的项目/实习经历数据

#### Scenario: 按类型过滤经历
- **WHEN** 用户访问项目/实习 Tab
- **THEN** 系统调用 `getExperiences(type)` API 传递对应类型
- **AND** 仅展示该类型的经历

### Requirement: 前端可创建用户经历

前端 SHALL 通过 `POST /api/v1/user/experiences` 创建新的项目或实习经历记录。

#### Scenario: 创建项目经历
- **WHEN** MEMBER 及以上用户点击添加项目经历并提交表单
- **THEN** 系统调用 `createExperience({ type: 'project', ... })` API
- **AND** 创建成功后刷新列表并显示成功提示

#### Scenario: 创建实习经历
- **WHEN** MEMBER 及以上用户点击添加实习经历并提交表单
- **THEN** 系统调用 `createExperience({ type: 'internship', ... })` API
- **AND** 创建成功后刷新列表并显示成功提示

#### Scenario: CANDIDATE 尝试创建经历
- **WHEN** CANDIDATE 用户点击添加经历
- **THEN** 前端显示"仅成员及以上可添加经历"提示
- **AND** 不显示添加按钮或禁用添加功能

### Requirement: 前端可更新用户经历

前端 SHALL 通过 `PUT /api/v1/user/experiences/{id}` 更新指定的项目或实习经历。

#### Scenario: 更新经历
- **WHEN** MEMBER 及以上用户修改已有经历并保存
- **THEN** 系统调用 `updateExperience(id, data)` API
- **AND** 更新成功后刷新列表

### Requirement: 前端可删除用户经历

前端 SHALL 通过 `DELETE /api/v1/user/experiences/{id}` 删除指定的项目或实习经历。

#### Scenario: 删除经历
- **WHEN** MEMBER 及以上用户删除某条经历
- **THEN** 系统调用 `deleteExperience(id)` API
- **AND** 删除成功后从列表中移除该项

