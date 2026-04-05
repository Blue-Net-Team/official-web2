## MODIFIED Requirements

### Requirement: 考核卡片操作按钮跳转
考核列表页卡片的操作按钮 SHALL 根据考核状态跳转到考题目录页：
- 进行中：点击"继续答题"按钮跳转到 `/assessment/{timeId}/questions`
- 已结束：点击"查看详情"按钮跳转到 `/assessment/{timeId}/questions`
- 未开始：按钮保持禁用状态，不可点击

#### Scenario: 点击进行中考核的操作按钮
- **WHEN** 用户点击状态为"进行中"的考核卡片的"继续答题"按钮
- **THEN** 系统跳转到 `/assessment/{item.id}/questions` 页面

#### Scenario: 点击已结束考核的操作按钮
- **WHEN** 用户点击状态为"已结束"的考核卡片的"查看详情"按钮
- **THEN** 系统跳转到 `/assessment/{item.id}/questions` 页面

#### Scenario: 点击未开始考核的操作按钮
- **WHEN** 用户点击状态为"未开始"的考核卡片的"暂不可进入"按钮
- **THEN** 按钮无响应，不发生跳转
