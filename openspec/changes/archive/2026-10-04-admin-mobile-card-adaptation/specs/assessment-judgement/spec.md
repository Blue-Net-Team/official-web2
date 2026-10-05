# Delta: assessment-judgement

## ADDED Requirements

### Requirement: 题目评分提交列表移动端卡片化
题目评分页（`/admin/assessment/judge/score`）的提交列表（含题目视图与考生视图中的提交列表、队伍头行与成员行）SHALL 在移动端（`isMobile = !screens.md`）由内嵌 Table 改为卡片列表，解决学号等窄列在手机上每行仅显示一个数字的问题。

卡片 SHALL 按以下结构渲染：
- 队伍头行渲染为通栏卡片（队名 + 人数 + 队长标签，图标使用 `TeamOutlined`）
- 成员行渲染为缩进卡片：主行姓名（含队长 `CrownOutlined` 标签/队伍标签）+ 学号独立一行（等宽数字），右侧得分 `x / max` 与评分状态标签（已评分/待评分）
- 操作区：评判时间 + 评分/改分主按钮

#### Scenario: 移动端成员卡片展示学号
- **WHEN** 评分人在移动端视口查看某题目的提交列表
- **THEN** 每名考生渲染一张卡片，学号在独立行完整展示
- **AND** 得分、结果、状态与操作按钮在卡片内不挤压、不折行

#### Scenario: 队伍卡片与成员卡片层级
- **WHEN** 提交以队伍为单位展示
- **THEN** 队伍头行渲染为通栏卡片，其成员卡片相对队伍卡片缩进显示
- **AND** 队长卡片带皇冠图标标签，与桌面端标识一致

#### Scenario: 移动端触发评分弹窗
- **WHEN** 用户点击卡片上的评分 / 改分按钮
- **THEN** SHALL 打开与桌面端一致的评分 Modal（宽度在移动端为全屏）

#### Scenario: 桌面端保持内嵌表格
- **WHEN** 视口大于等于 md 断点
- **THEN** 提交列表 SHALL 以原内嵌 Table 渲染，含队伍头行合并行逻辑，行为与改动前一致

### Requirement: 移动端范围限定
本次卡片化范围 SHALL 限定为提交列表（submissionColumns / submissionMemberColumns 及其渲染处）；排行榜（questionColumns / candidateColumns / decisionColumns）、筛选区与详情 Drawer 仅在既有 `screens.md` 自适应基础上保证不溢出，不改为卡片。

#### Scenario: 排行榜在移动端不溢出
- **WHEN** 用户在移动端查看题目/考生排行榜
- **THEN** 排行榜区域 SHALL 可横向滚动查看完整内容，不发生内容截断或布局错乱
