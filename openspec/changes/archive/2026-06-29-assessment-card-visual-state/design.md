## Context

`AssessmentCard`（`src/frontend/src/components/Assessment/AssessmentCard/index.tsx`，约 204 行）是考核列表的叶子展示组件，入参仅 `assessment: AssessmentTimeDTO`。其视觉本质是一个四态枚举（淘汰/进行中/已结束/未开始），但当前实现派生了 4 个相互不正交的布尔量（`eliminated`、`isInProgress`、`isEnded`、`isNotStarted`），并在 9 个渲染位各自重复同一套优先级判断：

| 渲染位 | 行号 | 形态 |
|--------|------|------|
| 卡片边框 | 62–70 | className 四元 |
| 图标底色 | 82–90 | className 四元 |
| 图标元素 | 92–98 | JSX 三元（仅 3 态，无淘汰分支） |
| 徽章底色 | 108–116 | className 四元 |
| 徽章文案 | 118–124 | 文本三元（判断源用 `actualStatus` 而非 `isEnded`） |
| 进度条颜色 | 164–170 | className 三元（仅 3 态，无淘汰分支） |
| 按钮样式 | 179–187 | className 四元 |
| 按钮文案 | 193–199 | 文本三元 |
| 顶部高光线 | 72–77 | 两个 `&&` |

约束：项目前端源码无 `*.test.tsx`，无单测网兜底；两处调用方（`assessment/page.tsx`、`Profile/AssessmentList`）仅传 props，不应受影响。

## Goals / Non-Goals

**Goals:**
- 将 4 个散落布尔收敛为单一 `VisualState` 派生值，集中表达状态优先级
- 用模块级静态 `STYLES` 映射表集中全部视觉规格，9 个渲染位统一读表
- 消除「徽章文案判断源不一致」与「图标/进度条靠 fallthrough 隐式取值」两处坏味道，改为显式声明
- 保证对外渲染结果与交互行为与重构前逐像素等价（纯重构）

**Non-Goals:**
- 不改变任何颜色、文案、图标、交互行为（不借机「美化」UI）
- 不改组件 props、不改 `AssessmentTimeDTO` 类型、不动调用方
- 不引入设计 token / 主题系统 / CSS 变量等更大范围的样式架构改造
- 不新增前端测试框架（项目当前无前端单测基建，超出本次范围）

## Decisions

### 决策 1：单一 `visualState` 派生 + 模块级 `STYLES` 映射表

```tsx
type VisualState = 'eliminated' | 'inProgress' | 'ended' | 'notStarted'

const visualState: VisualState =
  eliminated ? 'eliminated'
  : actualStatus === 'IN_PROGRESS' ? 'inProgress'
  : actualStatus === 'ENDED' ? 'ended'
  : 'notStarted'
```

`STYLES` 定义为组件**外**的模块级常量（静态、不随渲染重建），每个状态是一份完整视觉规格：

```tsx
const STYLES: Record<VisualState, {
  border: string
  iconBg: string
  icon: ReactNode
  badgeClass: string
  badgeText: string
  buttonClass: string
  buttonText: string
  progressBar: string
  topLine: string | null
  clickable: boolean
}> = { eliminated: {...}, inProgress: {...}, ended: {...}, notStarted: {...} }
```

**理由**：`Record<VisualState, …>` 让 TS 强制每个状态都提供全部字段，新增状态时若漏填会编译报错——这正是消除「漏改导致不一致」的根本机制。

**备选方案**：① 保留布尔、仅抽函数 `getBorderClass(state)` 等 —— 仍是 9 个函数各判优先级，没解决重复；② 用 `class-variance-authority` 等库 —— 引入新依赖，超出纯重构范围。均不采用。

### 决策 2：`icon` 与 `progressBar` 显式纳入映射表

当前淘汰态的图标（落 `Inbox`）与进度条（落灰）是条件 fallthrough 的副产物，未显式声明。映射表中 `eliminated.icon = <InboxOutlined/>`、`eliminated.progressBar = 灰` 显式写出，**取值与现状逐字一致**。

**理由**：把隐式行为显式化，避免后续维护者误以为「淘汰态没定义图标」而改错。

### 决策 3：可点击性收敛为 `clickable` 字段

onClick 的 `if (isNotStarted || eliminated) return`（189 行）与右箭头 `!isNotStarted && !eliminated`（200 行）统一改为读 `STYLES[visualState].clickable`（`inProgress`/`ended` 为 `true`，其余 `false`）。

**理由**：可点击性也是状态的一个维度，集中后与视觉同源，杜绝「视觉是 A 态、行为按 B 态」的分叉。

### 决策 4：等价改写纪律——先建基线，逐位比对

实现时 `STYLES` 各字段取值**从现有代码逐位复制**，不重排、不调色、不改文案。改动前先用 Playwright 对四态各截一张基线图，改动后逐张对比，差异目标为 0。

## Risks / Trade-offs

- **[手抖改变某状态渲染]** 重构 9 处时复制错颜色/文案/图标 → 缓解：决策 4 的基线截图逐位比对；spec 中四态取值已逐一列明作为对照表。
- **[淘汰态隐式值被「修正」]** 把 fallthrough 显式化时，误把淘汰态图标从 `Inbox` 改成别的 → 缓解：spec 显式 Scenario 锁定「淘汰态图标 = Inbox、进度条 = 灰」，作为验证项。
- **[无单测兜底]** 改坏了只能靠人工/E2E 发现 → 缓解：四态 + 可点击行为全部纳入 Playwright 验证清单；改动范围极小（单文件、无 props 变化）限制了爆炸半径。
- **[ReactNode 存于模块常量]** 把 JSX 元素存入模块级 `STYLES` 是合法的（React 元素是普通对象），无状态、无副作用，安全。

## Migration Plan

无数据/接口迁移。纯前端组件内部重构：
1. 改前对四态截基线图
2. 替换实现
3. 逐张比对 + 交互验证
4. 回滚策略：单文件 `git revert` 即可，无副作用
