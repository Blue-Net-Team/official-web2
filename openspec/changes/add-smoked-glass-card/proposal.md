## Why

Issue #70 反馈竞赛、成就页面的卡片是“透明玻璃、无厚度感”。根因是全站存在 4 种各自为政的玻璃卡片实现，其中共享类 `.glass-card`（`globals.css`）是无底色的纯透明配方：`rgba(255,255,255,0.06)` 叠加 `blur(24px)`，在纯黑背景上模糊无物、阴影不可见；在微信 Android XWEB 内核中 `backdrop-filter` 支持不稳，卡片直接退化成一层薄纱。需要一个以深色渐变底色保底、模糊仅作增强层的“烟色玻璃”统一卡片组件，一次性收敛散落实现。

## What Changes

- 新增共享组件 `SmokedGlassCard`（烟色玻璃卡片，纯 `div`），位于 `src/components/SmokedGlassCard/`，样式使用 CSS Module（与 `feb2db4b` 的 Tailwind 迁移方向的关系见 design.md）
- 组件 API：`tone?: 'deep' | 'soft'`（默认 `'deep'`：浓=物体卡片；`'soft'`：淡=面板/容器，纯数据容器）、`hoverable?: boolean`（默认 `false`，**仅对 `'deep'` 生效**——可点击交互只属于卡片，容器/面板不提供 hover 反馈）、`radius?: number`（默认 `16`，px）、`accent?: string`（可选主题色 token，经 CSS 变量 `--accent` 驱动边框/光晕/内高光）、`className`/`style`/`children`
- 深色渐变底色保底（`#1b1b24 → #12121a`），`backdrop-filter` 仅为增强层：模糊失效时（XWEB）依然是有形卡片，天然优雅降级，无需 `@supports` 回退
- hover 阴影采用同构 3 层结构（常态与 hover 仅数值不同），保证 `box-shadow` 可平滑插值，修复阴影“瞬现”问题
- 批次迁移：
  - 批次 1（修 #70）：`CompetitionCard`、`AchievementCard`、`AchievementStats`
  - 批次 2：`resources` 资源卡片、`lab-environment` 的 `EquipmentCard`/`VenueCard`
  - 批次 3：`MemberCard` **全面替换**为统一烟色玻璃样式（`hoverable`），删除其个性化装饰——顶部 3px 渐变高光条、头像渐变描边、hover 蓝紫外发光，与全站卡片视觉统一；`AssessmentCard` 状态视觉 token 化（4 状态 × `--accent` 单变量，替代现状 4 状态 × 8 字段矩阵）
  - 批次 4：Profile 页 6 处磨砂面板（`ProfileTabs` 除外，后续单独改造）
- 删除 `globals.css` 中的 `.glass-card`（全站仅批次 1 的 3 处引用，删后无残留）
- 明确不设“纯磨砂（无底色玻璃）”组件：其与旧版 `.glass-card` 配方几乎一致（已在 `/glass-demo` 对照验证），XWEB 下降级即失效，属于已证缺陷形态
- 删除设计验证页 `/glass-demo`（样式迁入组件 module.css）

## Capabilities

### New Capabilities

- `smoked-glass-card`: 共享烟色玻璃卡片组件的 API、视觉配方（浓淡两档、hover 反馈、圆角、accent 主题色）、XWEB 降级行为及使用规范（何时用、何时不用）

### Modified Capabilities

（无。现有 spec 均未定义玻璃卡片视觉需求；`assessment-card-visual-state` 的对外状态视觉要求不变，token 化属实现方式演进。）

## Impact

- **代码**：`src/components/SmokedGlassCard/`（新增）；批次 1-4 共约 13 处卡片/面板文件改造；`src/app/globals.css` 删除 `.glass-card` 约 10 行
- **页面**（E2E 验证路线）：`/competitions` → `/achievements` → `/resources` → `/lab-environment` → `/members` → `/assessment` → `/profile`
- **Issue**：关闭 #70（拟态玻璃卡片样式不统一）
- **不动**：`Members.tsx` 筛选控件条（控件语义）、`ProfileTabs`（待单独改造）、admin 后台（浅色 antd 主题，另一体系）、首页组件（无玻璃卡片）
- **风险**：批次 2/4 的视觉会有可感知变化（底色更实、hover 反馈更明显），需在 E2E 中逐页确认
