## Context

项目已有的动态背景：
- `src/frontend/src/components/Reactbits/ColorBends/ColorBends.tsx`：基于 `three.js` 的 WebGL shader 背景，用于首页 `HomeBackground.tsx`
- `src/frontend/src/app/(public)/(home)/HomeBackground.tsx`：在首页以 `fixed inset-0 z-0` 方式挂载 `ColorBends`

Reactbits `DarkVeil` 官方实现使用 `ogl` 库，核心是一段 CPPN（Compositional Pattern-Producing Network）fragment shader，支持 `hueShift`、`speed`、`warpAmount` 等参数。项目 `package.json` 已依赖 `three`（`^0.167.1`），但没有 `ogl`。

目标页面分组（来自之前的背景梳理）：

| 分组 | 背景特征 | 涉及页面 |
|---|---|---|
| 第 1 组 | `#0a0a0a` + 固定蓝/橙/紫径向渐变 | `resources`、`achievements`、`assessment`、`assessment/[timeId]/questions`、`assessment/[timeId]/questions/[questionId]`、`enroll`、`members`、`profile` |
| 第 2 组 | 纯黑/`#0a0a0f` + 手写动态装饰球 | `competitions`、`lab-environment` |
| 第 5 组 | 黑底 + 局部/内联径向渐变 | `change-password`、`members/[id]` |

## Goals / Non-Goals

**Goals:**
- 用 `three.js` 复刻 `DarkVeil` 的视觉效果
- 保持原始 fragment shader 不变
- 将 DarkVeil 应用到第 1、2、5 组公开页面，替换原有背景
- 通过临时对照页验证 three.js 版与官方 `ogl` 版效果一致
- 确保组件在卸载时正确释放 WebGL 资源

**Non-Goals:**
- 不修改首页 `ColorBends` 背景
- 不修改登录/忘记密码/方向详情/管理后台等已有明确背景风格的页面
- 不引入 `ogl` 新依赖
- 不做后端改动

## Decisions

### 1. 移植策略：three.js 替换 ogl

**决定**：不安装 `ogl`，将 `DarkVeil` 改写为 `three.js` 版本。

**理由**：
- 项目已有 `three` 和 `@types/three`，复用可减少依赖
- 与现有 `ColorBends` 技术栈一致，便于统一维护和性能优化
- 原始 fragment shader 可以原样复用，移植风险集中在 WebGL 胶水层

**对应关系**：

| ogl 原 API | three.js 等价实现 |
|---|---|
| `Renderer` | `THREE.WebGLRenderer` |
| `Program` | `THREE.ShaderMaterial` |
| `Mesh` + `Triangle` | `THREE.Mesh` + `THREE.PlaneGeometry(2, 2)` 或自定义 fullscreen triangle geometry |
| `Vec2` | `THREE.Vector2` |
| `renderer.setSize(w, h)` | `renderer.setSize(w, h, false)` |
| `program.uniforms.uX.value` | `material.uniforms.uX.value` |

**uniform 映射**：
- `uResolution`: `THREE.Vector2(parent.clientWidth, parent.clientHeight)`
- `uTime`: `number`，每帧按 `(performance.now() - start) / 1000 * speed` 更新
- `uHueShift`: `number`
- `uNoise`: `number`
- `uScan`: `number`
- `uScanFreq`: `number`
- `uWarp`: `number`

**uResolution / DPR 处理**：
- `ogl` 原代码中 `uResolution` 使用父元素 CSS 像素尺寸，drawing buffer 通过 `renderer.setSize(w * resolutionScale, h * resolutionScale)` 放大
- three.js 中同步该约定：`renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))`，并将 `uResolution` 设为 CSS 尺寸而非 drawing buffer 尺寸
- 若验证阶段发现缩放比例不一致，优先调整 `renderer.setSize` 参数而非修改 shader

### 2. 组件封装与使用方式

**决定**：新增 `DarkVeil` 组件，使用时外层包裹 `fixed inset-0 z-0` 容器。

**使用示例**：

```tsx
<div className="min-h-screen relative overflow-x-hidden">
  <div className="fixed inset-0 z-0">
    <DarkVeil hueShift={180} speed={0.3} />
  </div>
  <main className="relative z-1">...</main>
</div>
```

- `DarkVeil` 组件内部返回 `<canvas>`，CSS 设置 `width: 100%; height: 100%; display: block;`
- 因为 shader 输出不透明（alpha=1），页面根元素原有 `bg-[#0a0a0a]` 可以保留作为 fallback，或被移除
- 内容层必须保持 `relative z-1`（或更高），确保浮在背景之上

### 3. 效果一致性验证

**决定**：在实现后创建一个临时对照页面，并排渲染 `ogl` 原版与 `three.js` 移植版。

**验证步骤**：
1. 临时安装 `ogl` 用于对照（或直接引用原始 `DarkVeil.jsx`）
2. 创建临时路由（如 `/__verify/dark-veil`，开发环境可见即可）
3. 左右两个等尺寸容器，使用相同 props：`hueShift={180}`、`speed={0.5}`、`warpAmount={0.2}` 等
4. 同步 `uTime` 起点，确保两帧时间一致
5. 使用 Playwright 截图对比；如果视觉上无法区分，则认为移植成功
6. 验证完成后删除临时路由和 `ogl` 依赖

### 4. 性能与资源清理

**决定**：在 three.js 版中加入与 `ColorBends` 类似的资源清理。

**措施**：
- `useEffect` return 中取消 `requestAnimationFrame`
- 移除 resize 监听
- 调用 `geometry.dispose()`、`material.dispose()`、`renderer.dispose()`、`renderer.forceContextLoss()`
- 可选：使用 `IntersectionObserver`，当背景不在视口内时暂停 rAF，减少低电量设备消耗
- 限制 DPR 最大为 2，避免 4K/Retina 屏过度消耗

## Risks / Trade-offs

- **移植偏差风险**：`ogl` 与 `three.js` 在 DPR/视口处理上可能存在差异，可能导致画面缩放或清晰度不同。通过对照页截图验证后可消除。
- **性能风险**：DarkVeil shader 计算量较大，低端设备可能出现发热或掉帧。通过 IntersectionObserver 暂停和限制 DPR 缓解。
- **视觉一致性风险**：首页 `ColorBends` 与其他页 `DarkVeil` 风格不同，需确认设计侧接受这种差异化。
- **SSR/ hydration 风险**：`DarkVeil` 必须标记为 `'use client'`，server component 页面可直接使用，不影响 ISR 配置。
- **维护风险**：移植后的组件需要人工跟随 Reactbits 官方更新。由于项目已有 `ColorBends` 移植先例，此风险可控。
