## Why

`AssessmentCard` 组件的视觉状态本质是一个四态枚举（淘汰 → 进行中 → 已结束 → 未开始），但当前实现把它拆成 4 个散落的布尔量（`eliminated`、`isInProgress`、`isEnded`、`isNotStarted`），导致同一套状态优先级判断在组件内被复制了 9 处。新增一种状态需要同步修改全部副本，极易遗漏并造成 UI 不一致（见 issue #37）。本次变更将其收敛为单一视觉状态 + 集中样式映射表，是一次行为等价的纯重构。

## What Changes

- 新增 `VisualState` 联合类型与单一 `visualState` 派生表达式，集中表达「淘汰 > 进行中 > 已结束 > 未开始」的状态优先级
- 新增模块级静态 `STYLES` 映射表，按状态集中定义全部视觉规格（边框、图标底色、图标元素、徽章底色+文案、按钮样式+文案、进度条、顶部高光线、可点击性）
- 将组件内 9 处重复的嵌套三元 / 条件渲染替换为读取 `STYLES[visualState]`
- 将 onClick 拦截与右箭头显隐统一改为读取 `STYLES[visualState].clickable`
- 显式补齐当前依赖「fallthrough」隐式决定的两处（淘汰态图标 = `Inbox`、淘汰态进度条 = 灰色），取值与现状逐字一致
- **非破坏性**：组件 props（`assessment: AssessmentTimeDTO`）与对外渲染结果保持不变

## Capabilities

### New Capabilities
- `assessment-card-visual-state`: 考核卡片（`AssessmentCard`）四态视觉渲染契约——定义淘汰/进行中/已结束/未开始四种状态下边框、图标、徽章、按钮、进度条、高光线的视觉表现与可点击行为，以及状态优先级规则。该契约此前未被规格化，本次以新能力形式固化重构必须保持的行为不变量。

### Modified Capabilities
- 无（纯前端组件内部重构，不改变任何后端能力的需求或接口）

## Impact

- **改动文件（唯一）**：`src/frontend/src/components/Assessment/AssessmentCard/index.tsx`
- **调用方（不受影响）**：
  - `src/frontend/src/app/(public)/(other)/assessment/page.tsx:95`
  - `src/frontend/src/components/Profile/AssessmentList/index.tsx:38`
- **类型定义**：`src/frontend/src/apis/schema/assessment.dto.ts` 不改动
- **后端**：无任何改动
- **测试**：项目前端源码无 `*.test.tsx`，无单元测试网兜底，验证依赖 Playwright 端到端视觉与交互比对
- **ISR**：两个调用页均为 `'use client'` 客户端组件，不涉及 `revalidate`
