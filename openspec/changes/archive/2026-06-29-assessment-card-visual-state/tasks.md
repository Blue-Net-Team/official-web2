## 1. 准备与基线

- [x] 1.1 确认前端 3000 端口占用情况（CLAUDE.md 规范）：占用则复用现有服务，未占用方可 `pnpm dev`
- [x] 1.2 准备覆盖四态的考核数据（`notStarted`：startTime 未来；`inProgress`：now 落区间；`ended`：endTime 过去；`eliminated`：`eliminated=true`），并确保至少一条 `totalQuestions > 0` 以触发进度条
- [x] 1.3 用 Playwright 打开考核页 `/assessment`，对四态卡片各截一张基线图存档（改动前）（人工验证完成）

## 2. 重构实现（仅 `src/frontend/src/components/Assessment/AssessmentCard/index.tsx`）

- [x] 2.1 新增 `VisualState` 联合类型与 `visualState` 派生表达式（优先级：eliminated > inProgress > ended > notStarted），替换原 49–52 行散落布尔
- [x] 2.2 在组件外定义模块级 `STYLES: Record<VisualState, {...}>`，字段含 border / iconBg / icon / badgeClass / badgeText / buttonClass / buttonText / progressBar / topLine / clickable
- [x] 2.3 逐位从现有代码复制四态取值填入 `STYLES`（不调色、不改文案、不换图标），显式写出淘汰态 `icon = <InboxOutlined/>` 与 `progressBar = 灰色`
- [x] 2.4 替换边框（62–70）、图标底色（82–90）为 `STYLES[visualState].border` / `.iconBg`
- [x] 2.5 替换图标元素（92–98）为 `STYLES[visualState].icon`
- [x] 2.6 替换徽章底色（108–116）与徽章文案（118–124）为 `.badgeClass` / `.badgeText`（统一判断源）
- [x] 2.7 替换进度条颜色（164–170）为 `.progressBar`
- [x] 2.8 替换按钮样式（179–187）与按钮文案（193–199）为 `.buttonClass` / `.buttonText`
- [x] 2.9 替换顶部高光线（72–77）为基于 `.topLine` 的单一条件渲染
- [x] 2.10 onClick 拦截（188–191）与右箭头显隐（200）改为读 `STYLES[visualState].clickable`
- [x] 2.11 删除不再被引用的 `isInProgress` / `isEnded` / `isNotStarted` 等中间布尔（保留 `actualStatus`、`eliminated` 供派生）

## 3. 静态校验

- [x] 3.1 TypeScript 类型检查通过（`Record<VisualState,…>` 强制四态字段齐全，无 `any`）
- [x] 3.2 ESLint / 项目 lint 通过，无未使用变量告警

## 4. 端到端验证（Playwright，逐位对照 spec）

- [x] 4.1 四态视觉逐张与基线图比对，差异为 0：边框、图标底、图标元素、徽章底色+文案、按钮样式+文案、进度条、顶部高光线
- [x] 4.2 重点核验淘汰态：图标仍为 `Inbox`、进度条仍为灰色（验证显式化未改变现状）
- [x] 4.3 交互：`inProgress`/`ended` 点击按钮跳转 `/assessment/{id}/questions` 且显示右箭头
- [x] 4.4 交互：`eliminated`/`notStarted` 点击不跳转、光标 `not-allowed`、无右箭头
- [x] 4.5 进度条边界：`totalQuestions = 0` 时不渲染进度条
- [x] 4.6 两处调用方（`assessment/page.tsx`、Profile `AssessmentList`）渲染正常，无需改动

## 5. 收尾

- [x] 5.1 自检 diff 仅限单文件，props 与对外行为未变
- [x] 5.2 用户确认后按提交规范提交（`refactor: ...`，引用 `ref #37`）
- [x] 5.3 用户确认后归档（`/opsx:archive`）
