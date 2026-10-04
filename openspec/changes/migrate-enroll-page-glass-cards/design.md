## Context

报名页（`src/frontend/src/app/(public)/(other)/enroll/page.tsx`）由 5 个卡片区块组成，全部使用旧磨砂玻璃配方（半透明底色 + backdrop-filter、无深色渐变保底），其中 `EnrollForm` 主卡片（`bg-[rgba(20,20,30,0.6)] + backdrop-blur-[20px]`）正是 `SmokedGlassCard` 头注释中 Issue #70 证伪的缺陷形态。全站已有唯一玻璃组件 `SmokedGlassCard`（`src/frontend/src/components/SmokedGlassCard/`），其 CSS 头注释明确禁止新增玻璃场景时绕过该组件。

现状速览：

| 区块 | 现状样式 | 问题 |
|------|---------|------|
| `EnrollForm` | `rgba(20,20,30,0.6)` + blur + 顶部渐变条 + 四角括号 | 缺陷形态 + 冗余装饰 |
| `DirectionSidebar` | `rgba(20,20,30,0.6)` + translate-x hover + 选中态整套渐变 | 缺陷形态 + 非组件动画 |
| `EnrollFormDownloadCard` | `bg-white/[0.03]` | 无深色保底 |
| `ConsultationQrcode` | `bg-white/[0.03]` | 无深色保底 |

## Goals / Non-Goals

**Goals:**
- 报名页 5 个卡片区块全部迁移至 `SmokedGlassCard`，消除旧磨砂配方
- 方向卡片交互状态改用 accent 主题色语言：默认烟色 → hover 边框/文字主题色高亮+浮起 → 选中主题色加强
- 主卡片删除装饰元素（顶部渐变条、四角括号线），视觉层级收敛到玻璃材质本身
- `SmokedGlassCard` 组件零改动，纯消费方迁移

**Non-Goals:**
- 不改 `SmokedGlassCard` 组件本身（不加 `selected` prop、不改 hover 动画）
- 不迁移报名页以外的页面（assessment、resources 等页面的旧配方不在本次范围）
- 不改动表单功能、校验逻辑、API 集成

## Decisions

### D1: 方向卡片 hover 文字高亮用消费方 CSS，不扩展组件

hover 时标题文字变主题色，通过 `DirectionSidebar` 自己的 CSS Module 实现：

```css
.directionItem:hover .directionName {
  color: var(--accent);
}
```

`SmokedGlassCard` 已将 `accent` 写入内联样式 `--accent` CSS 变量，消费方可直接引用。

- **备选**：给 `SmokedGlassCard` 加 `hoverAccentText` 之类的 prop → 拒绝。为单一页面动全站组件，破坏通用性；方向性样式（per-direction 颜色映射、选中层级）本就属于 sidebar 的模块职责。

### D2: 方向卡片选中态在组件 className 上叠加，而非组件内建

选中态（主题色实线边框 + 主题色文字 + 淡渐变底色 + glow）通过 `SmokedGlassCard` 的 `className` 透传叠加 Tailwind 类实现，与现状的三方向条件类同构。

- **备选**：给组件加 `selected` prop → 拒绝。选中语义是方向卡片特有的，组件保持「卡片/容器 + accent」的最小模型。

### D3: hover 动画采用组件自带 translateY，删除 translate-x

用户确认横向平移"无关紧要"，直接用 `hoverable` 的 `translateY(-4px)`，同时获得组件的边框/光晕 hover 过渡（与 accent color-mix 同构，平滑插值）。

### D4: 两张侧栏小卡片用 `tone="soft"`

报名表下载与咨询群二维码是纯数据容器，符合组件注释中「要容器：`tone="soft"`」的标准场景。

### D5: 主卡片装饰元素删除而非迁移

顶部三色渐变条与四角括号线是旧设计的装饰遗留，与"视觉层级由玻璃材质与 accent 表达"的新语言冲突，直接删除。入场动画（`fadeInUp`、`fadeInLeft`、`slideIn`）保留，通过 `className` 透传。

## Risks / Trade-offs

- [选中态 Tailwind 叠加类与组件 `.accent` 类的 border 优先级冲突] → 选中态类使用具体色值（如 `border-[#6677ff]`）+ `!` 不必要，CSS Module 中 `.accent` 的 border-color 特异性为单类，Tailwind 叠加类同为单类但后声明者胜；实现时在 `className` 中叠加并验证实际渲染结果，如有覆盖问题则选中态样式也移入 CSS Module 并以属性选择器提高优先级。
- [`--accent` 变量在 hover 文字联动中的浏览器兼容] → color-mix 与 CSS 变量均为现代浏览器基线，`SmokedGlassCard` 已在用，无新增风险。
- [微信 XWEB 等 backdrop-filter 失效环境] → 迁移后组件的深色渐变保底恰好修复该风险，是本次变更的收益而非新增风险。

## Migration Plan

纯前端静态变更，随前端部署上线，无数据迁移。回滚 = revert 提交。

验证方式：报名页桌面端与移动端断点目视检查（背景 DarkVeil 上的 5 个卡片区块材质、hover/选中态、入场动画），重点确认 backdrop-filter 关闭（DevTools 模拟）时卡片仍有清晰形体。
