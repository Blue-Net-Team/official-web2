## Why

管理端考题抽屉（`QuestionDrawer`）现在同时存在三套互相冲突的滚动/高度契约：题面 tab 用 flex 高度链 + `min-h-[620px]` 让左右两列各滚，判题配置与资源限制 tab 用 `.statement-tabs` 全局 CSS 把确定高度灌进 `h-full overflow-y-auto` 面板，最近又在抽屉 body 上开了纵向滚动。结果是同一个抽屉里滚轮在 2~3 个滚动区域之间"接力丢失"，嵌在面板内的 Monaco 编辑器与 Select 下拉容易被内层容器裁切，且矮屏下题面编辑区会被压成很矮的一条。高度来源散落成三处魔法值，任何一次布局调整都要同时改三处。

`openspec/specs/question-statement-preview/spec.md` 里还留着「抽屉本体不得滚动」的旧要求，与当前实现已经矛盾，说明这套契约确实需要一次性收敛。

## What Changes

- 抽屉 body 成为**唯一**的纵向滚动容器；移除题面 tab 之外的两个 `h-full overflow-y-auto` 内层面板，判题配置与资源限制 tab 改为自然流动的长表单
- 移除 `.statement-tabs` 全局 CSS 高度补丁与 Form/Tabs 上的 flex 高度链（`flex-1 min-h-0`），删除 `min-h-[620px]` 魔法下限
- Tabs 导航条与元信息、左列编辑控件一起随抽屉 body 滚出视野（不吸顶）
- 题面 tab 的右列预览改为 sticky + 视口高度上限（`max-h`）并保留自身滚动；左列编辑控件随 body 自然流动，Markdown 编辑框保持随内容增高
- 切换 tab 时把抽屉 body 滚动位置复位到顶部，避免从一个长 tab 切到另一个 tab 时停在半空
- **BREAKING（内部契约）**：题面 tab 的左右两列不再"各自独立滚动"，改为「左列随抽屉滚动、右列 sticky 自滚」；对应的 spec 要求需要改写

## Capabilities

### New Capabilities

无

### Modified Capabilities

- `assessment-question-admin-ui`：抽屉滚动契约从「左右两列各自滚动 + 抽屉不滚」改写为「抽屉 body 单一滚动容器 + 右列预览 sticky」，并补充判题配置/资源限制 tab 不做内层滚动的场景
- `question-statement-preview`：删除「抽屉本体不得滚动」的旧要求，改为左列随抽屉流动、右列预览 sticky 且有视口高度上限

## Impact

- `src/frontend/src/app/admin/assessment/question/QuestionDrawer.tsx`：`renderStatementWorkspace`、`renderAlgorithmTabs`、`Drawer.styles.body`、`Form`/`Tabs` 的 className
- `src/frontend/src/app/globals.css`：移除 `.statement-tabs` 区块（4 条规则）
- 无后端、接口、依赖变更；`buildPreviewStatement` 与共享题面组件（`QuestionStatement/*`）不受影响
- 文档：`docs/05-参考手册/05-03-算法判题指南.md` 中「滚动与高度」段落需要同步
