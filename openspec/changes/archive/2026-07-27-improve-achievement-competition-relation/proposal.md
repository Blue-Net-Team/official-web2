## Why

当前团队成就系统中，竞赛成就的"关联项"（relateTo）是手动输入的自由文本，与团队竞赛库（tb_competition）之间仅通过字符串精确匹配建立弱关联。管理员输入拼写错误、大小写差异或多余空格时，会导致竞赛 Logo 和简称无法正确展示，且系统不会给出任何提示，形成静默的数据不一致。

## What Changes

- 为 `tb_competition.name` 添加数据库唯一约束，确保竞赛名称全局唯一，作为成就与竞赛之间的隐式关联键。
- 在后端竞赛管理（创建/更新）中增加应用层名称唯一性校验，避免直接暴露数据库约束异常。
- 前端成就管理表单中，当成就类型为 `COMPETITION` 时，将"关联项"从普通 Input 改为可搜索、可选择、允许输入新值的 Select 组件，选项来源于现有竞赛列表。
- 前端在保存前对输入值执行 `trim()`，消除前后空格导致的匹配失败。
- 论文（PAPER）和专利（PATENT）类型的"关联项"保持现有 Input 不变。

## Capabilities

### New Capabilities
<!-- 无新增独立能力，本次为现有能力的增强 -->

### Modified Capabilities
- `achievement-management`: 竞赛成就的关联项输入方式变更（从纯文本输入改为带已有竞赛选项的 Select），并增加输入规范化（trim）。
- `backend-competition-management`: 竞赛名称增加唯一性约束，创建/更新接口需处理名称冲突。

## Impact

- **数据库**: 新增 Flyway 迁移脚本，为 `tb_competition.name` 添加 `UNIQUE` 约束。
- **后端**: `CompetitionAppServiceImpl` 增加名称唯一性校验；`CompetitionRepository` / `CompetitionMapper` 新增按名称查询/判重方法。
- **前端**: `admin/achievement/AchievementDrawer.tsx` 中 `relateTo` 字段按成就类型条件渲染不同控件。
- **API**: 无接口签名变更；前端可能调用现有 `/api/v1/competitions` 获取选项列表。
