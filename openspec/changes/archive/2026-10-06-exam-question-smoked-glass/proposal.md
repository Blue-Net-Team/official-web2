## Why

考生考题链路（assessment questions 列表页 + QuestionDetail 作答页）仍使用无底色纯透明玻璃配方（`bg-white/[0.06]` + 细边框），这是 `SmokedGlassCard.module.css` 头注释与 Issue #70 已证缺陷的形态：在 backdrop-filter 失效的环境（微信 Android XWEB 等）中玻璃"融化"无形，且与全站已统一的烟色玻璃（SmokedGlassCard）视觉语言脱节。 DarkVeil 动效光斑背景下，透明面板依赖底层光效显形，内层网格格子的分层对比也不稳定。

## What Changes

- 将考生考题作答页（`src/components/Assessment/QuestionDetail/` 全部 9 个文件 + questions 路由页残留的透明玻璃容器）的最外层容器级面板迁移为 `SmokedGlassCard` 组件：
  - 题面主卡及全部容器面板统一 → `SmokedGlassCard` 默认 **deep** 档（不挂 `hoverable`；沉浸式 DarkVeil 场景下 deep 实底可挡光斑染色，经实施评审拍板不用 soft）
- 迁移时删除面板内部自带的 Tailwind padding（`p-5`/`p-7` 等），统一吃组件默认 24px 内边距
- 面板内部的"面板状"内层容器（题目导航格、提交记录格等）去底留框：去掉 `bg-white/[0.03~0.06]` 底色，边框加强至 `border-white/[0.15]` 保证在 DarkVeil 光斑透色下仍可分层（评审 demo 结论：0.08 细框在光斑染红的底色上隐形）
- 内联点缀元素零改动：blockquote/行内代码/代码块（`bg-black/30`）/彩色状态标签（AC/WA/选中态 `#6677ff`）/antd 原子组件（Select/Tag/Button/Upload/Collapse/Tooltip，样式由全局 darkAlgorithm token 驱动）
- 新增临时评审页 `src/app/(public)/(other)/glass-demo/page.tsx`（已在探索阶段创建，含迁移前后对照），评审完成后删除
- 不改 `SmokedGlassCard` 组件本身，不新增 tone 档位

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `frontend-assessment-question-page`: 新增视觉材质需求——考生考题页面 SHALL 使用 SmokedGlassCard 烟色玻璃作为唯一容器级玻璃材质，禁止无底色纯透明玻璃配方；补充内层元素分层规则（容器用组件、内层格子去底留框 0.15 边框、内联点缀与 antd 原子件不改）

## Impact

- **前端**：`src/components/Assessment/QuestionDetail/`（index.tsx、QuestionSidebar.tsx、TeamPanel.tsx、AlgorithmQuestion.tsx、ChoiceQuestion.tsx、JudgeResultPanel.tsx、FileUploadArea.tsx、CountdownSection.tsx）、`src/app/(public)/(other)/assessment/[timeId]/questions/page.tsx` 中少量残留
- **临时资产**：`src/app/(public)/(other)/glass-demo/page.tsx`（评审后即删，不入归档）
- **无后端影响**、无 API 变更、无 antd 样式覆盖、无权限/ISR 语义变化（页面均为客户端组件，不触及 `export const revalidate` 规则）
