## Context

全站玻璃卡片存在 4 种平行实现：

| 家族 | 位置 | 配方 |
|------|------|------|
| A `.glass-card` | `globals.css:230`，CompetitionCard / AchievementCard / AchievementStats | 无底色 `rgba(255,255,255,0.06)` + blur 24px，阴影黑(0.12) 在黑底不可见 |
| B 内联 Tailwind | resources、EquipmentCard、VenueCard | `bg-white/[0.05]` + blur-xl + hover 浮起 |
| C MemberCard 增强 | Members 页 | B + 顶部 3px 渐变高光条 + 蓝紫外发光双层阴影 |
| D AssessmentCard 主题 | assessment 页 + Profile | 4 状态 × 8 字段颜色矩阵（28 处魔法数字） |

Issue #70 报告者（微信 Android XWEB 内核）看到家族 A 退化为透明薄膜。设计验证页 `/glass-demo` 已与用户逐轮确认烟色玻璃配方与 API 形态。项目方向：暗色主题 + DarkVeil 动态光效背景为网站高级感支柱（用户明确），故不采用 Material You 式实色 surface 方案。

约束：
- 前端 Next.js 15 + React 19 + Tailwind CSS 4 + antd 6
- `feb2db4b` 迁移方向为“删除 CSS Module、全面 Tailwind”，本 change 是组件化收敛而非回退（见 Decisions D1）
- `hoverable` 默认 `false`、`radius` 默认 `16`、纯 `div` 不包 antd Card（用户已拍板）
- ProfileTabs 后续单独改造，不在本 change；admin 后台为浅色主题，不适用

## Goals / Non-Goals

**Goals:**

- 提供唯一玻璃卡片组件 `SmokedGlassCard`，名字即效果，消灭 4 种平行实现
- 任何内核（含 XWEB）下卡片保底有形，模糊只是加分项
- hover 阴影平滑渐变（同构插值）
- 批次 1 直接修复 #70；批次 2-4 收敛一致性
- AssessmentCard 状态视觉 token 化（`--accent` 单变量），为将来换主题色留通道

**Non-Goals:**

- 不设立纯磨砂（无底色玻璃）组件——已与旧版对照验证无法区分，属缺陷形态
- 不改 ProfileTabs、Members 筛选控件条、admin、首页组件
- 不调 DarkVeil 光斑位置（曾讨论的“上游病因补丁”，底色保底后降为可选优化，不纳入）
- 不改组件迁移页面的信息架构与内容，仅替换外框材质

## Decisions

**D1：样式用 CSS Module 而非 Tailwind 内联**

烟色玻璃配方含 5 层叠加背景、同构 3 层阴影、CSS 变量派生（accent），用 Tailwind 任意值表达会产生超长难读类名字符串，且 hover 插值依赖精确的层结构，放 CSS 中更易维护和审查。与 `feb2db4b` 迁移方向的关系：那次迁移的初心是消除“每种卡片一个 CSS Module”的散状重复，本 change 用**单一共享组件**收敛，是同一初心（唯一实现、一处维护）的更彻底形态，并非回退。组件内布局仍用 Tailwind（padding 除外，见 D4）。

**D2：深色渐变底色保底，不设 `@supports` 回退**

```
linear-gradient(180deg, rgba(255,255,255,0.045), transparent 42%)   ← 顶部高光
linear-gradient(160deg, #1b1b24 0%, #12121a 100%)                   ← 烟色底（不透明）
+ backdrop-filter: blur(12px) saturate(1.2)                        ← 仅增强层
```

底色是卡片存在感的来源；模糊失效时降级为“深色渐变卡片”，观感依然完整。`@supports not (backdrop-filter)` 回退因此不需要——回退是设计出来的，不是打补丁。备选（半透明底 + 更强 blur）曾演示，透明感提升有限且降级质量变差，用户否掉。

**D3：`tone: 'deep' | 'soft'` 命名浓淡，而非 `variant: 'card' | 'panel'`**

`card/panel` 命名的是使用场景，开发者看不出视觉差异；`tone` 直接描述外观浓淡：`deep`=浓烟（物体卡片，实底+投影，可交互），`soft`=淡烟（分区面板，半透明底 `rgba(30,30,40,0.5)→rgba(20,20,28,0.5)` + blur 20px + 无阴影，纯数据容器）。磨砂感来自背景透过半透明底被模糊扩散，底 alpha 须维持约 0.5——0.6 实测退化为实色板（用户反馈“磨砂感消失”），无底色则失去 XWEB 降级保底。交互语义与 tone 绑定：**只有 `deep` 允许 `hoverable`，`soft` 传入 `hoverable` 一律不生效**——可点击交互只属于卡片，容器/面板不提供 hover 反馈（用户在演示页实测 panel 浮起效果差后拍板）。备选：`subtle` 布尔值（可扩展性差，第三档无处安放）、`density`（语义偏布局密度，有歧义）。

**D4：默认 padding 走 CSS 变量通道 `--sgc-padding`（默认 24px）**

调用方覆盖 padding（如竞赛条目 `md:px-8`）时，Tailwind 类与组件默认 padding 的优先级取决于样式表顺序，不可靠。组件内联 `style={{ padding: 'var(--sgc-padding)' }}`，覆盖方只需 `style={{ '--sgc-padding': '...' }}` 或传 style，行为确定。`radius` 同理走 `style={{ borderRadius: radius }}`。

**D5：accent 经 CSS 变量 `--accent` + `color-mix` 派生**

传 `accent="#6677ff"` 时组件注入 `--accent`，边框/光晕/内高光全部 `color-mix(in srgb, var(--accent) N%, transparent)` 派生。状态视觉从“4 状态 × 8 字段手工颜色”塌缩为“4 状态 × 1 变量”。`color-mix` 在 Chromium 111+ / Safari 16.2+ 可用，项目目标环境（现代浏览器 + XWEB/Chrome 内核）满足；AssessmentCard 的 eliminated/notStarted 中性态不传 accent 即可。

**D6：hover 阴影同构 3 层，仅数值变化**

```
常态： 0 8px 32px rgba(0,0,0,0.35), 0 0 48px rgba(102,119,255,0),   inset 0 1px 0 rgba(255,255,255,0.08)
hover：0 8px 32px rgba(0,0,0,0.50), 0 0 48px rgba(102,119,255,0.10), inset 0 1px 0 rgba(255,255,255,0.12)
```

`box-shadow` 列表层数/结构不同时浏览器无法插值（演示页 v1 实测“瞬现”）。蓝紫光晕常态以 0 透明度潜伏，hover 平滑亮起。`soft` tone 与 accent 变体同样保持层数一致。

**D7：组件为纯 `div`，AchievementCard 顺带解绑 antd Card**

AchievementCard 现状是 antd `Card` + `glass-card` class，body padding 依赖 `styles.body`。迁移后直接 `SmokedGlassCard`，padding 由组件承担。不包 antd Card：玻璃卡片不需要 Card 的头部/网格语义，纯 div 最干净。

**D8：MemberCard 装饰整体删除，不做保留**

初版方案曾计划将 MemberCard 的顶部 3px 渐变高光条、头像渐变描边、hover 蓝紫外发光以 children 侧子元素保留（避免组件 prop 膨胀）。用户在演示评审中拍板：**这些装饰让 MemberCard 与全站其他卡片完全不一致，看着别扭，头像边框更是多余**——全部删除，MemberCard 与其他卡片一样使用纯 `SmokedGlassCard hoverable`。这一决定同时简化了批次 3：无需处理装饰迁移，只做内容与布局的搬移。

## Risks / Trade-offs

- [批次 2/4 页面视觉可感知变化（底色更实、hover 有浮起）] → 每批次单独合入，E2E 逐页截图对比（resources、lab-environment、Profile）
- [AssessmentCard token 化误改状态语义（被淘汰置灰、不可点等）] → 迁移只替换颜色来源，不改 `STYLES` 表的结构语义；4 状态逐一在 `/assessment` 与 Profile 考核 Tab 验证
- [`color-mix` 在极旧内核不可用导致 accent 变体边框失效] → accent 场景仅 AssessmentCard；失效时回退为中性烟色卡片（可接受），后续如发现 XWEB 实机问题再补静态色
- [组件滥用风险（把玻璃用在不该用的场景）] → spec 中写明使用规范：需要内容覆盖在动态背景上的容器/卡片才用；普通表单区块用 antd 常规组件
- [演示页删除后无对照] → 删除前在 #70 中贴定稿截图存档

## Migration Plan

1. 组件落地（TDD：先写组件单元测试）→ 批次 1 → 批次 2 → 批次 3 → 批次 4，每批次可独立合入验证
2. 删除 `globals.css` `.glass-card`（随批次 1 一起，全站仅 3 处引用）
3. 删除 `/glass-demo` 路由（迁移完成后）
4. 回滚策略：任一批次独立 revert 即可；组件本身无数据/接口依赖，纯前端展示层

## Open Questions

- `soft` tone 的数值已最终定稿并在演示页确认（半透明底 alpha 0.5、blur 20px、saturate 1.3）；批次 4 实施时如与 Profile 实页观感不符可微调（属 spec 允许范围）
