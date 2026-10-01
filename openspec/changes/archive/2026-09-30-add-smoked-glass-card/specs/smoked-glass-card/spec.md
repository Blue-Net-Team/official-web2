## ADDED Requirements

### Requirement: 烟色玻璃卡片组件

系统 SHALL 提供共享组件 `SmokedGlassCard`（`src/components/SmokedGlassCard/`），渲染为纯 `div`，样式经 CSS Module 定义，作为全站唯一的玻璃质感卡片/面板实现。

#### Scenario: 渲染基本卡片

- **WHEN** 开发者以 `<SmokedGlassCard>内容</SmokedGlassCard>` 渲染
- **THEN** 输出带烟色玻璃质感的卡片：深色渐变底色（`#1b1b24 → #12121a`）+ 顶部白色高光层 + 1px 边框 + 3 层同构阴影（投影、蓝紫光晕、内高光）+ `backdrop-filter: blur(12px) saturate(1.2)`
- **AND** 默认圆角为 16px、默认内边距为 24px

### Requirement: 浓淡两档 tone

组件 SHALL 提供 `tone?: 'deep' | 'soft'` 参数（默认 `'deep'`）：`'deep'` 为浓烟（不透明深色底 + 投影，用于物体卡片）；`'soft'` 为淡烟（半透明深色底 `rgba(30,30,40,0.5) → rgba(20,20,28,0.5)`、blur 20px、无边框投影，用于面板/容器）。底色透明度 MUST 保持在半透明区间（约 0.5）：过高则磨砂感消失退化为实色板，过低则失去降级保底。

#### Scenario: tone 为 soft 的面板形态

- **WHEN** 开发者传入 `tone="soft"`
- **THEN** 卡片渲染为淡烟面板：半透明深色渐变底 + 淡边框（`rgba(255,255,255,0.07)`），无投影
- **AND** 在 `backdrop-filter` 失效的环境中仍因半透明底色而保持可辨识

### Requirement: hoverable 悬浮反馈（仅 card）

组件 SHALL 提供 `hoverable?: boolean` 参数（默认 `false`），且仅对 `tone='deep'` 生效：`tone='soft'` 为数据容器，传入 `hoverable` MUST NOT 产生任何交互效果。为 `true` 时，鼠标悬停 SHALL 触发：上移 4px、边框提亮、阴影加深、蓝紫光晕从 0 透明度平滑亮起；常态与悬停状态的 `box-shadow` MUST 保持相同层数与结构（仅数值变化），保证过渡动画可平滑插值。

#### Scenario: 悬停动画平滑渐变

- **WHEN** 用户对 `hoverable` 卡片执行鼠标悬停
- **THEN** 位移、阴影、光晕在 0.3s 内平滑过渡，无瞬现/跳变

#### Scenario: 非 hoverable 卡片无悬停反馈

- **WHEN** 用户悬停未开启 `hoverable` 的卡片
- **THEN** 卡片无任何位移、阴影或背景变化

#### Scenario: panel 传入 hoverable 不生效

- **WHEN** 开发者以 `tone="soft" hoverable` 渲染
- **THEN** 面板渲染为纯数据容器，悬停无任何位移、阴影或背景变化

### Requirement: 圆角与内边距可配置

组件 SHALL 提供 `radius?: number` 参数（默认 `16`，单位 px）控制圆角；内边距 MUST 经 CSS 变量 `--sgc-padding`（默认 `24px`）提供，调用方覆盖时行为确定，不依赖样式表顺序。

#### Scenario: 自定义圆角与内边距

- **WHEN** 开发者传入 `radius={24}` 与 `style={{ '--sgc-padding': '32px' }}`
- **THEN** 卡片圆角为 24px，内边距为 32px

### Requirement: accent 主题色 token

组件 SHALL 提供 `accent?: string` 参数。传入时组件 MUST 将值注入 CSS 变量 `--accent`，并以 `color-mix(in srgb, var(--accent) N%, transparent)` 派生边框色、外发光与内高光；未传入时 MUST 渲染为中性烟色。

#### Scenario: 主题色卡片

- **WHEN** 开发者传入 `accent="#6677ff"`
- **THEN** 卡片边框、阴影光晕、顶部内高光呈现该颜色的派生透明色
- **WHEN** 同时开启 `hoverable`
- **THEN** 悬停时主题色光晕平滑增强，层数与常态保持一致

### Requirement: XWEB 降级保底

卡片 MUST 以深色渐变底色作为存在感的保底来源，`backdrop-filter` 仅作为增强层；在 `backdrop-filter` 不生效的环境（如微信 Android XWEB 内核）中，卡片 SHALL 仍呈现为完整的有形深色卡片，不依赖 `@supports` 回退规则。

#### Scenario: 模糊失效时卡片保持有形

- **WHEN** 渲染环境不支持或未启用 `backdrop-filter`
- **THEN** 卡片仍显示深色渐变底色、边框与阴影，与背景的对比度足以辨识卡片边界

### Requirement: 使用规范

项目 SHALL 仅在内容需要覆盖于暗色动态背景（DarkVeil）之上的卡片或面板场景使用 `SmokedGlassCard`；普通表单区块、控件条 SHALL 使用 antd 常规组件。全站 MUST NOT 新增无底色纯磨砂玻璃形态（与旧版 `.glass-card` 等价的配方）。

#### Scenario: 违规形态不进入代码库

- **WHEN** 代码评审发现新增 `rgba(255,255,255,0.06)` 无底色 + blur 的卡片配方
- **THEN** 评审 SHOULD 要求改用 `SmokedGlassCard`
