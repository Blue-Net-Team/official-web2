## 1. 主表单卡片迁移

- [x] 1.1 `EnrollForm.tsx`：外层 div 替换为 `<SmokedGlassCard>`（deep 档），通过 `className`/`style` 透传 `w-full max-w-[600px]`、`overflow-hidden`、`animate-[fadeInUp...]` 等布局与动画类
- [x] 1.2 删除主卡片的顶部三色渐变条与四角括号线共 5 个装饰元素
- [x] 1.3 确认内部表单结构（头像上传区、各 Form.Item、提交按钮）渲染与样式不变

## 2. 方向选择卡片迁移

- [x] 2.1 `DirectionSidebar.tsx`：方向卡片替换为 `<SmokedGlassCard hoverable accent={方向主题色}>`，删除 `bg-[rgba(20,20,30,0.6)]`、translate-x hover 类与手写 hover 阴影
- [x] 2.2 在 `enroll/styles.module.css`（或 sidebar 局部模块）中添加 hover 文字高亮：`.directionItem:hover` 下标题文字引用 `var(--accent)` 变为主题色
- [x] 2.3 选中态通过 `className` 叠加保留：三方向主题色边框 + 主题色文字 + 淡渐变底色 + glow，验证与组件 `.accent` 边框无优先级冲突
- [x] 2.4 确认默认选中第一项、点击切换、表单意向方向同步更新等行为不变

## 3. 侧栏数据容器迁移

- [x] 3.1 `EnrollFormDownloadCard/index.tsx`：外层 div 替换为 `<SmokedGlassCard tone="soft">`，保留内部下载按钮与提示文案结构
- [x] 3.2 `ConsultationQrcode/index.tsx`：外层 div 替换为 `<SmokedGlassCard tone="soft">`，保留 Popover 交互与二维码列表渲染

## 4. 验证

- [x] 4.1 `npx tsc --noEmit` 通过（仅存在与本次无关的预存 tabs-demo 陈旧类型报错）；4 个迁移组件外层无旧玻璃配方类。`EnrollForm` 内部头像子面板与 `MobileDirectionSelector` 的 `bg-white/[0.03]` 属变更范围外的内部小节/移动端选择器，保持原样
- [x] 4.2 Playwright 目视验证桌面端与移动端（390px）：5 个卡片区块材质、hover/选中态主题色高亮、入场动画正常；方向切换点击生效
- [x] 4.3 Playwright 注入 `backdrop-filter: none !important` 验证：卡片仍保持深色渐变形体，保底生效
