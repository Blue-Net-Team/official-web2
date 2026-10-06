## Context

全站卡片已通过 `SmokedGlassCard` 统一为烟色玻璃材质（深色渐变底色保底 + backdrop-filter 增强，Issue #70 修复形态）。考生考题链路是最后的遗留区：`QuestionDetail` 9 个文件约 55 处 `bg-white/[0.x]` 裸 Tailwind 透明玻璃，恰好是被禁用的缺陷配方。探索阶段产出临时评审页 `src/app/(public)/(other)/glass-demo/page.tsx`，用真实 `SmokedGlassCard` + 真实 `DarkVeil` 背景（与 assessment 页同参数 `hueShift={-130} speed={0.6} offsetY={0.2}`）验证了迁移观感，并暴露一个预期外问题：**0.08 细边框在光斑透色的 soft 面板上隐形**。

## Goals / Non-Goals

**Goals:**

- 考生考题链路容器级面板全部迁移到 `SmokedGlassCard`，消除最后的缺陷玻璃配方
- 内层格子在任何 DarkVeil 帧下稳定分层（边框 0.15）
- 改动收敛在"容器级 div"，内联点缀与 antd 原子件零改动

**Non-Goals:**

- 不修改 `SmokedGlassCard` 组件本身，不新增 tone 档位（评审考虑过更淡的 0.3 透明度档位，否决：为单页面开全站组件先例不值）
- 不做 accent 品牌色光晕升级（demo ④ 已展示 `#6677ff` accent 变体，留作后续独立增量）
- 不迁移管理端页面（其 `bg-white/[0.x]` 均为标签/按钮级点缀，非玻璃面板）
- 不覆盖任何 antd 组件样式
- 不改动考题页的布局结构、交互逻辑与数据流

## Decisions

### 决策 1：包组件而非下沉材质工具类

QuestionDetail 是纯 Tailwind 页面，`SmokedGlassCard` 是 CSS Modules 组件，但组件支持 `className` 透传 + `--sgc-padding` CSS 变量，可直接嵌入 Tailwind 布局。选择包组件（而非把材质复制成 Tailwind 工具类）以保持"全站唯一玻璃实现"的约束不被稀释。

### 决策 2：tone 映射（实施时按用户要求修正——全部 deep）

初始设计为题面 deep / 侧栏 soft 两档；实施评审时用户拍板：**考题页所有容器面板统一用 deep（浓烟）**，理由是 deep 的不透明实底在 DarkVeil 光斑下观感更稳、整页材质统一。最终映射：

```
题面主卡 + 侧栏全部面板 + 算法题/选择题 section
/ 编辑器容器 / 提交记录 / 判题结果 / 倒计时 / 队伍面板   → 全部 deep（默认档，不挂 hoverable）
```

注：这与全站 "deep=可交互卡 / soft=数据容器" 的一般规范不同，是考题页作为沉浸式全屏场景的特判（ DarkVeil 光斑会透过 soft 半透底导致面板被染色）。

### 决策 3：内边距归组件（用户拍板方案 B）

删除面板内部 Tailwind `p-5`/`p-7`，统一吃组件默认 24px。若个别面板原 padding 特殊（如 `p-7` 的算法题 section），用 `style={{'--sgc-padding': '28px'}}` 在组件层覆盖，不在内容里加回 padding。

### 决策 4：内层格子"去底留框"，边框 0.15（评审修正）

初始方案是 `border-white/[0.08]` 去底留框；真实 DarkVeil 背景下 soft 面板被光斑染红、底色对比骤降，0.08 框隐形（demo 实测）。修正为**只保留边框并加强至 0.15**，分层靠边框对比度而非底色。`bg-black/30` 凹槽（C 方案）保留给代码块/输入区这类"凹进"语义区域，不用于列表格。

三级处理规则：

| 级别 | 元素 | 处理 |
|------|------|------|
| 容器级 | 最外层面板 | 换 SmokedGlassCard（deep/soft） |
| 内层格子 | 题目导航格、提交记录格、用例格 | 去底留框，`border-white/[0.15]` |
| 内联点缀 | blockquote、行内代码、彩色标签、成员行、`bg-black/30` 代码块 | 零改动 |

### 决策 5：antd 零侵入

考题作答页的 antd 用法全是原子件（Select/Tag/Button/Upload/Collapse/Tooltip/Modal），无 List/Table 容器，样式由全局 ConfigProvider darkAlgorithm 驱动。questions 列表页的 Table 嵌在 SmokedGlassCard 内是已上线的既有先例，照此办理。

## Risks / Trade-offs

- **光斑遮挡**：smoke 实底会遮住面板后方的 DarkVeil 光斑（通透感下降）。这是 Issue #70"存在感优先于通透感"的既定取舍，已在 demo 中确认可接受。
- **内边距视觉差异**：组件 24px 与原 `p-5`(20px)/`p-7`(28px) 略有差异，迁移后需 Playwright 截图比对确认无挤压/空旷。
- **漏网容器**：`bg-white/[0.x]`  grep 命中 55 处，实施时需逐个判断是"容器级"还是"点缀级"，任务清单按文件逐一列出容器级目标以降低漏判。
- **临时 demo 页**：`glass-demo` 路由是公开页面，评审/实施后必须删除，避免泄漏到生产。
