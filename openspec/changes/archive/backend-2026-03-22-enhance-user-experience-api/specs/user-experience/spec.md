# User Experience Specification (Delta)

修改用户经历管理功能的权限控制要求，限制只有MEMBER及以上角色才能管理经历。

## MODIFIED Requirements

### Requirement: 项目经历添加
系统SHALL允许MEMBER及以上角色的用户添加新的项目经历。

#### Scenario: MEMBER添加项目
- **WHEN** MEMBER角色用户点击"添加项目"按钮
- **THEN** 系统显示项目表单（或模态框）
- **AND** 用户填写项目名称、角色、时间、描述、技术栈、链接
- **AND** 保存后项目出现在列表中

#### Scenario: DIRECTION_ADMIN添加项目
- **WHEN** DIRECTION_ADMIN角色用户添加项目
- **THEN** 系统允许添加并保存项目

#### Scenario: SUPER_ADMIN添加项目
- **WHEN** SUPER_ADMIN角色用户添加项目
- **THEN** 系统允许添加并保存项目

#### Scenario: CANDIDATE添加项目被拒绝
- **WHEN** CANDIDATE角色用户尝试添加项目
- **THEN** 系统返回403 Forbidden错误
- **AND** 错误消息为"无权限"

#### Scenario: 添加项目必填验证
- **WHEN** MEMBER及以上角色用户提交项目表单时缺少必填字段（项目名称、角色、时间、描述）
- **THEN** 系统显示验证错误提示

### Requirement: 项目经历编辑
系统SHALL允许MEMBER及以上角色的用户编辑已有的项目经历。

#### Scenario: MEMBER编辑自己的项目
- **WHEN** MEMBER角色用户点击自己的项目编辑按钮
- **THEN** 系统显示预填充当前数据的项目表单
- **AND** 用户修改后保存，更新项目信息

#### Scenario: 用户编辑他人项目被拒绝
- **WHEN** 用户尝试编辑不属于自己的项目
- **THEN** 系统返回404 Not Found错误
- **AND** 错误消息为"经历不存在"

#### Scenario: CANDIDATE编辑项目被拒绝
- **WHEN** CANDIDATE角色用户尝试编辑项目
- **THEN** 系统返回403 Forbidden错误

### Requirement: 项目经历删除
系统SHALL允许MEMBER及以上角色的用户删除项目经历。

#### Scenario: MEMBER删除自己的项目
- **WHEN** MEMBER角色用户点击自己的项目删除按钮
- **THEN** 系统显示删除确认提示
- **AND** 确认后项目从列表中移除

#### Scenario: 用户删除他人项目被拒绝
- **WHEN** 用户尝试删除不属于自己的项目
- **THEN** 系统返回404 Not Found错误

#### Scenario: CANDIDATE删除项目被拒绝
- **WHEN** CANDIDATE角色用户尝试删除项目
- **THEN** 系统返回403 Forbidden错误

### Requirement: 竞赛经历添加
系统SHALL允许MEMBER及以上角色的用户添加新的竞赛经历。

#### Scenario: MEMBER添加竞赛
- **WHEN** MEMBER角色用户点击"添加竞赛"按钮
- **THEN** 系统显示竞赛表单
- **AND** 用户填写竞赛名称、角色、时间、获奖等级、团队人数、描述、证书链接
- **AND** 保存后竞赛出现在列表中

#### Scenario: CANDIDATE添加竞赛被拒绝
- **WHEN** CANDIDATE角色用户尝试添加竞赛
- **THEN** 系统返回403 Forbidden错误

### Requirement: 竞赛经历编辑和删除
系统SHALL允许MEMBER及以上角色的用户编辑和删除竞赛经历。

#### Scenario: MEMBER编辑自己的竞赛
- **WHEN** MEMBER角色用户点击自己的竞赛编辑按钮
- **THEN** 系统显示预填充当前数据的竞赛表单

#### Scenario: MEMBER删除自己的竞赛
- **WHEN** MEMBER角色用户点击自己的竞赛删除按钮并确认
- **THEN** 竞赛从列表中移除

#### Scenario: CANDIDATE编辑或删除竞赛被拒绝
- **WHEN** CANDIDATE角色用户尝试编辑或删除竞赛
- **THEN** 系统返回403 Forbidden错误

### Requirement: 实习经历添加
系统SHALL允许MEMBER及以上角色的用户添加新的实习经历。

#### Scenario: MEMBER添加实习
- **WHEN** MEMBER角色用户点击"添加实习"按钮
- **THEN** 系统显示实习表单
- **AND** 用户填写公司、职位、时间、状态、描述、主要成就
- **AND** 保存后实习记录出现在列表中

#### Scenario: CANDIDATE添加实习被拒绝
- **WHEN** CANDIDATE角色用户尝试添加实习
- **THEN** 系统返回403 Forbidden错误

### Requirement: 实习经历编辑和删除
系统SHALL允许MEMBER及以上角色的用户编辑和删除实习经历。

#### Scenario: MEMBER编辑自己的实习
- **WHEN** MEMBER角色用户点击自己的实习编辑按钮
- **THEN** 系统显示预填充当前数据的实习表单

#### Scenario: MEMBER删除自己的实习
- **WHEN** MEMBER角色用户点击自己的实习删除按钮并确认
- **THEN** 实习记录从列表中移除

#### Scenario: CANDIDATE编辑或删除实习被拒绝
- **WHEN** CANDIDATE角色用户尝试编辑或删除实习
- **THEN** 系统返回403 Forbidden错误

### Requirement: 权限验证机制
系统SHALL通过权限注解和数据库权限记录验证用户权限。

#### Scenario: 权限注解验证
- **WHEN** 用户调用经历管理API（创建/更新/删除）
- **THEN** 系统检查 `@RequiresPermission` 注解
- **AND** 验证用户是否拥有对应权限

#### Scenario: 权限数据库记录
- **WHEN** 系统启动时
- **THEN** 数据库中存在权限记录：
  - user:experience:create
  - user:experience:update
  - user:experience:delete
- **AND** 这些权限已分配给 MEMBER、DIRECTION_ADMIN、SUPER_ADMIN 角色

#### Scenario: 未登录用户管理经历被拒绝
- **WHEN** 未登录用户尝试管理经历
- **THEN** 系统返回401 Unauthorized错误
- **AND** 错误消息为"未认证"
