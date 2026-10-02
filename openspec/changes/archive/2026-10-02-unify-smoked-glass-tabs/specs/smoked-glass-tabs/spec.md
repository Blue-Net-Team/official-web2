# Spec: smoked-glass-tabs

## ADDED Requirements

### Requirement: 统一 Tab 组件
站点 SHALL 提供唯一的烟色玻璃胶囊 Tab 组件 `SmokedGlassTabs`，供所有公开页面替换自研 Tab 实现。组件 SHALL 包装 antd `Tabs` 并保留其 API（`items` / `activeKey` / `onChange`），另提供 `accent` 主题色属性。

#### Scenario: 组件 API 与 antd Tabs 对齐
- **WHEN** 开发者在任意页面引入 `SmokedGlassTabs` 并传入 `items`、`activeKey`、`onChange`
- **THEN** 组件按受控方式渲染 Tab 并触发 `onChange` 回调
- **AND** 键盘方向键切换、焦点可见性等 antd 原生可访问性行为保持可用

#### Scenario: 主题色定制
- **WHEN** 开发者传入 `accent="#fa8c16"`
- **THEN** 激活胶囊的光晕与焦点描边由该色经 `color-mix` 派生
- **AND** 未传 `accent` 时默认使用蓝紫色 `#6677ff`

### Requirement: 烟色玻璃胶囊容器
Tab 导航 SHALL 渲染为烟色玻璃胶囊容器，材质与 `SmokedGlassCard` deep 配方同源（深色渐变保底 + backdrop-filter 增强），禁止使用无底色纯磨砂配方。

#### Scenario: 光效背景下的可读性
- **WHEN** 组件渲染在 DarkVeil 动态光效背景上
- **THEN** 容器呈现深色渐变底色与半透明高光边框，背景高亮区域经过时容器形状始终可辨

### Requirement: 滑动胶囊指示器
Tab 切换 SHALL 通过独立的浓烟玻璃胶囊指示器平移实现，指示器在 Tab 按钮文字之下、容器背景之上。

#### Scenario: 指示器平移动画
- **WHEN** 用户点击另一个 Tab
- **THEN** 胶囊指示器从当前 Tab 位置平移到新 Tab 位置
- **AND** 位移与宽度变化使用类 sigmoid 缓动（无过冲），时长约 0.35s

#### Scenario: 指示器几何跟随激活项
- **WHEN** 激活 Tab 的宽度发生变化（徽标计数位数变化、窗口缩放、字体加载完成）
- **THEN** 指示器位置与宽度自动重新计算并平滑调整

#### Scenario: 文字高亮颜色过渡
- **WHEN** Tab 激活状态切换
- **THEN** 新旧 Tab 文字颜色在约 0.3s 内渐变（暗淡 ↔ 纯白）
- **AND** 激活 Tab 文字颜色 MUST 为纯白，保证浓烟胶囊上的可读性

### Requirement: 颜色由局部主题 token 控制
组件 SHALL 通过局部 `ConfigProvider` 的 Tabs token 控制文字颜色（未激活 `rgba(255,255,255,0.55)`、悬浮 `rgba(255,255,255,0.9)`、激活 `#ffffff`），材质与几何由 CSS Module 承载；禁止再使用全局 token 的颜色覆盖或高特异性选择器军备竞赛。

#### Scenario: 独立于全局主题
- **WHEN** 全局 ThemeProvider 将 Tabs 选中色配置为橙色 `#fa8c16`
- **THEN** `SmokedGlassTabs` 内激活 Tab 文字仍为纯白
- **AND** antd 默认 ink bar 与底部边框线在组件内不可见
