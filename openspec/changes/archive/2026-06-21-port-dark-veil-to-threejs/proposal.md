## Why

目前除首页外，大部分公开页面使用静态 CSS 径向渐变或手写装饰组件作为背景，视觉动效较弱。Reactbits 的 `DarkVeil` 是一个基于 WebGL shader 的动态暗色背景，与现有首页 `ColorBends` 风格互补，可以提升这些页面的科技感。

为了与项目现有技术栈保持一致（已有 `three` 而无 `ogl`），并避免引入额外依赖，决定将 `DarkVeil` 移植为 `three.js` 版本，再批量应用到指定公开页面。

## What Changes

- 将 Reactbits `DarkVeil` 从 `ogl` 移植为 `three.js` 实现，放入 `src/components/Reactbits/DarkVeil/`
- 保持原始 GLSL fragment shader 不变，确保视觉效果一致
- 为 `DarkVeil` 添加可复用的全屏背景包装组件（或统一使用 `fixed inset-0` 模式）
- 在第 1、2、5 组公开页面中替换原有静态渐变/装饰背景：
  - 第 1 组（深色底 + 径向渐变）：`resources`、`achievements`、`assessment`、`assessment/[timeId]/questions`、`assessment/[timeId]/questions/[questionId]`、`enroll`、`members`、`profile`
  - 第 2 组（纯黑/深色底 + 动态装饰）：`competitions`、`lab-environment`
  - 第 5 组（黑底 + 局部渐变）：`change-password`、`members/[id]`
- 增加临时对照页面，用于验证 three.js 版与原版 `ogl` 视觉效果一致
- 补充性能优化：不可见时暂停 rAF、页面卸载时清理 WebGL context

## Capabilities

### New Capabilities

- `dark-veil-background`: 基于 three.js 的动态暗色 WebGL 背景组件

### Modified Capabilities

- 无新增业务功能，仅调整上述公开页面的背景渲染方式

## Impact

- **前端依赖**：不新增 `ogl` 等第三方库，复用现有 `three`
- **前端页面**：替换约 10 个公开页面的背景实现
- **性能**：新增 WebGL 动画背景，需验证低配置设备和移动端表现
- **兼容性**：背景组件为 client component，server component 页面可直接渲染
