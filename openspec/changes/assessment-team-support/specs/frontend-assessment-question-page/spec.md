## ADDED Requirements

### Requirement: FILE_UPLOAD 题在组队考核中展示组队前置流程
在允许组队的考核中，FILE_UPLOAD 类型的题目 SHALL 在考题目录页和题目详情页展示组队相关状态。未组队的用户 SHALL 看到创建/加入队伍的入口，已组队的用户 SHALL 看到队伍信息面板。

#### Scenario: 未组队用户查看 FILE_UPLOAD 题
- **WHEN** 未组队的用户进入允许组队的考核的考题目录页
- **THEN** FILE_UPLOAD 题 SHALL 显示"创建队伍"或"加入队伍"按钮，点击后进入组队流程

#### Scenario: 已组队用户查看 FILE_UPLOAD 题
- **WHEN** 已组队的用户进入允许组队的考核的考题目录页
- **THEN** FILE_UPLOAD 题 SHALL 显示队伍名称和当前成员数

#### Scenario: 非 FILE_UPLOAD 题不受组队影响
- **WHEN** 用户查看单选题、多选题或算法题
- **THEN** 页面 SHALL 正常展示个人答题界面，不显示组队相关 UI

### Requirement: 邀请码输入与预览确认
用户 SHALL 通过输入邀请码预览队伍信息，确认后再加入。预览 SHALL 展示队伍名称、队长信息、成员列表（含方向）。

成员列表 SHALL 使用现有的用户信息展示组件，复用 `UserInfo` 数据结构。

#### Scenario: 输入邀请码预览队伍
- **WHEN** 用户在加入队伍弹窗中输入邀请码并点击预览
- **THEN** 系统 SHALL 调用预览接口并展示队伍名称、队长、成员列表（含各成员方向）

#### Scenario: 确认加入队伍
- **WHEN** 用户预览队伍信息后点击确认加入
- **THEN** 系统 SHALL 调用加入接口，成功后刷新页面并展示队伍信息

#### Scenario: 取消加入队伍
- **WHEN** 用户预览队伍信息后点击取消
- **THEN** 系统 SHALL 关闭弹窗，不调用加入接口

### Requirement: 队长上传区与队员只读区
FILE_UPLOAD 题的题目详情页 SHALL 根据当前用户是否为队长展示不同的操作区。

队长 SHALL 看到文件上传组件和提交按钮。队员 SHALL 看到"队长已提交"的文件列表，并显示"您无上传权限，请联系队长"的提示。

#### Scenario: 队长进入 FILE_UPLOAD 题
- **WHEN** 队长进入 FILE_UPLOAD 题详情页
- **THEN** 页面 SHALL 展示文件上传组件、已上传文件列表、提交/更新按钮

#### Scenario: 队员进入 FILE_UPLOAD 题
- **WHEN** 队员进入 FILE_UPLOAD 题详情页
- **THEN** 页面 SHALL 展示队长已提交的文件列表，不展示上传组件和提交按钮，并显示无权限提示

#### Scenario: 队员查看队长更新的文件
- **WHEN** 队长更新提交的文件后，队员刷新页面
- **THEN** 队员 SHALL 看到最新的文件列表

### Requirement: 题目页展示队伍信息面板
FILE_UPLOAD 题的题目详情页 SHALL 在题目描述区域附近展示队伍信息面板，包含队伍名称、队长、成员列表及各自方向。

成员信息 SHALL 复用现有的用户展示组件。

#### Scenario: 队伍信息面板展示
- **WHEN** 已组队的用户进入 FILE_UPLOAD 题详情页
- **THEN** 页面 SHALL 展示队伍信息面板，包含队伍名称、队长姓名、成员列表（含方向标签）
