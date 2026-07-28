## Purpose

定义成就与成员的关联关系，支持为成就关联系统内用户及外部协作者，并在删除成就时级联清理关联数据。

## MODIFIED Requirements

### Requirement: 管理员可以为成就关联系统内成员

系统SHALL允许超级管理员在创建或更新成就时，通过 `Mentions` 组件选择系统内已注册用户作为该成就的关联成员。

#### Scenario: 创建成就时关联系统内成员
- **WHEN** 超级管理员填写成就表单并 `@` 选择了用户 ID 为 1 和 2 的成员
- **THEN** 系统保存成就记录
- **AND** 系统在 `tb_user_achievement` 中写入 `(1, achievement_id)` 和 `(2, achievement_id)` 两条关联

#### Scenario: 更新成就时替换系统内成员
- **WHEN** 超级管理员编辑已有成就，将关联成员从用户 1、2 改为用户 3
- **THEN** 系统更新成就记录
- **AND** 系统删除该成就原有的 `tb_user_achievement` 关联
- **AND** 系统写入新的 `(3, achievement_id)` 关联

#### Scenario: 关联不存在的用户
- **WHEN** 超级管理员提交的 `userIds` 中包含不存在的用户 ID
- **THEN** 系统返回 400 错误，提示“存在无效的成员用户”

### Requirement: 管理员可以为成就添加外部协作者

系统SHALL允许超级管理员在创建或更新成就时，添加不属于本系统的外部协作者姓名。

#### Scenario: 创建成就时添加外部协作者
- **WHEN** 超级管理员填写成就表单，并在“外部协作者”输入框中添加了“张三-外校”和“李四-他队”
- **THEN** 系统保存成就记录
- **AND** 系统在 `tb_achievement_external_member` 中写入两条记录，分别关联到该成就

#### Scenario: 外部协作者姓名去重与规范化
- **WHEN** 超级管理员提交的外部协作者列表包含重复项或前后空格（如 " 张三 " 和 "张三"）
- **THEN** 系统去除前后空格
- **AND** 系统对重复姓名去重后保存

#### Scenario: 外部协作者姓名为空或超长
- **WHEN** 超级管理员提交空字符串或长度超过 100 字符的外部协作者姓名
- **THEN** 系统忽略空字符串
- **AND** 对超长姓名返回 400 错误，提示“外部协作者姓名不能超过 100 字符”

### Requirement: 删除成就时级联清理关联数据

系统SHALL在删除成就记录时，同步清理该成就关联的系统内成员和外部协作者数据。

#### Scenario: 删除成就并清理成员关联
- **WHEN** 超级管理员删除指定成就
- **THEN** 系统删除 `tb_achievement` 中的成就记录
- **AND** 系统删除 `tb_user_achievement` 中该成就的所有关联
- **AND** 系统删除 `tb_achievement_external_member` 中该成就的所有记录

### Requirement: 成就返回数据包含关联成员信息

系统SHALL在成就详情和列表返回数据中，包含系统内成员和外部协作者信息。

#### Scenario: 查询成就详情
- **WHEN** 客户端请求成就详情或列表
- **THEN** 返回数据中包含 `members` 字段，展示已关联系统用户的 id、姓名、头像
- **AND** 返回数据中包含 `externalMembers` 字段，展示外部协作者姓名列表
