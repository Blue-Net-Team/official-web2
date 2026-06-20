## 1. 组件移植

- [x] 1.1 创建 `src/components/Reactbits/DarkVeil/DarkVeil.tsx`，用 `three.js` 实现 `DarkVeil`
- [x] 1.2 创建 `src/components/Reactbits/DarkVeil/DarkVeil.css`，设置 canvas 全屏样式
- [x] 1.3 原样复制官方 fragment shader 和 vertex shader，保持视觉效果一致
- [x] 1.4 实现 uniform 更新（`uTime`、`uHueShift`、`uNoise`、`uScan`、`uScanFreq`、`uWarp`）
- [x] 1.5 实现 ResizeObserver / resize 监听，正确处理 DPR 和 `uResolution`
- [x] 1.6 实现卸载清理：取消 rAF、dispose geometry/material/renderer、forceContextLoss

## 2. 效果验证

- [x] 2.1 临时安装 `ogl` 并复制官方 `DarkVeil.jsx` 作为对照
- [x] 2.2 创建临时对照页面（如 `/verify/dark-veil`），左右并排渲染原版与 three.js 版
- [x] 2.3 使用相同 props 并同步 `uTime` 起点
- [x] 2.4 使用 Playwright 对两个 canvas 截图并肉眼/像素对比
- [x] 2.5 如发现偏差，调整 three.js 端 `setSize`/`setPixelRatio`/`uResolution` 映射
- [x] 2.6 验证通过后删除临时页面和 `ogl` 依赖

## 3. 页面集成（第 1 组：深色底 + 径向渐变）

- [x] 3.1 更新 `resources/page.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.2 更新 `achievements/page.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.3 更新 `assessment/page.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.4 更新 `assessment/[timeId]/questions/page.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.5 更新 `components/Assessment/QuestionDetail/index.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.6 更新 `enroll/page.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.7 更新 `members/page.tsx`，替换固定径向渐变层为 `DarkVeil`
- [x] 3.8 更新 `profile/page.tsx` 及 `profile/styles.module.css`，替换固定径向渐变层为 `DarkVeil`

## 4. 页面集成（第 2 组：纯黑/深色底 + 动态装饰）

- [x] 4.1 更新 `competitions/page.tsx`，移除 `BackgroundDecorations`，替换为 `DarkVeil`
- [x] 4.2 更新 `lab-environment/page.tsx`，移除 `BackgroundDecorations`，替换为 `DarkVeil`

## 5. 页面集成（第 5 组：黑底 + 局部/内联渐变）

- [x] 5.1 更新 `change-password/page.tsx`，替换内联径向渐变背景为 `DarkVeil`
- [x] 5.2 更新 `members/[id]/page.tsx`，替换内联径向渐变背景为 `DarkVeil`

## 6. 回归验证

- [x] 6.1 运行 `pnpm build` 检查 TypeScript 编译和 Next.js 构建
- [x] 6.2 检查所有受影响页面的 z-index 层级，确保内容浮于背景之上
- [x] 6.3 在桌面端和移动端分别预览，确认无文字可读性问题
- [x] 6.4 检查低配置设备/浏览器控制台的 WebGL 性能表现
- [x] 6.5 确认无新增未使用的 CSS module 或组件

## 7. 清理与归档

- [x] 7.1 删除不再使用的旧背景 CSS / 装饰组件引用（保留组件文件本身，避免破坏其他页面）
- [x] 7.2 确认 `package.json` 中没有遗留 `ogl`
- [x] 7.3 更新 `/opsx:archive` 所需信息，准备归档
