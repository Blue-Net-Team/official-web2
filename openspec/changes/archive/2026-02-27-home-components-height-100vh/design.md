## Context

当前主页由多个独立组件构成，每个组件使用固定像素高度（780px），而非响应式的视口高度。这导致在不同屏幕尺寸下视觉体验不一致。本次变更将所有主页组件的高度从固定像素值调整为 `110vh`（视口高度的 110%），以提供更好的全屏滚动体验和观感。

当前各组件高度设置情况：
| 组件 | 当前高度 | 位置 |
|------|----------|------|
| TopContent | `height: 780px` | styles.module.css |
| Competitions | `height: 780px !important` | styles.module.css |
| AchievementAndResources | `height: 780px` | styles.module.css |
| FeaturedEquipment | `height: 780px` | styles.module.css |
| TeamVibe | `height: 780px` | styles.module.css |
| DirectionIntroduce | `height: '780px'` (inline style) | index.tsx |
| RecruitmentProcess | `height: 780px` | styles.module.css |

主页容器 `page.tsx` 当前设置了 `minHeight: '250vh'`，变更后需要移除此设置。

## Goals / Non-Goals

**Goals:**
- 将所有主页组件高度统一调整为 `110vh`
- 移除主页容器的固定 `minHeight` 设置
- 保持各组件内部布局和样式不变
- 提供一致的全屏滚动体验，每个组件略大于视口以获得更好观感

**Non-Goals:**
- 不修改组件内部布局逻辑
- 不修改组件的功能实现
- 不添加新的响应式断点逻辑

## Decisions

### 1. 高度设置方式选择

**决定**: 使用 CSS 模块中的 `height: 110vh` 替代固定像素值

**理由**:
- 保持现有代码风格一致性（大部分组件已使用 CSS 模块）
- `110vh` 提供比视口略大的高度，提供更好的观感
- 便于维护和统一修改

**替代方案**:
- 使用 `100vh`：视口高度刚好填满，但观感不够理想
- 使用 Tailwind CSS 的 `h-screen` 类：需要引入 Tailwind，与现有 Ant Design + CSS Modules 风格不一致
- 使用 inline style：DirectionIntroduce 组件当前使用此方式，但不符合项目其他组件的风格

### 2. DirectionIntroduce 组件样式迁移

**决定**: 将 DirectionIntroduce 组件的 inline style 迁移到 CSS 模块

**理由**:
- 与其他组件保持一致的风格
- 便于统一管理和修改
- inline style 不利于维护

### 3. 主页容器 minHeight 处理

**决定**: 移除 `minHeight: '250vh'` 设置

**理由**:
- 各组件使用 `110vh` 后，总高度由组件数量决定
- 固定 minHeight 会导致不必要的空白区域

## Risks / Trade-offs

- **内容溢出风险** → 组件内部已有适当的布局处理，必要时可添加 `overflow: auto`
- **移动端视口高度问题** → 移动端浏览器地址栏会影响 `110vh` 实际高度，但当前项目主要面向桌面端，暂不处理
- **小屏幕内容压缩** → 内容可能在小屏幕上显得拥挤，后续可考虑添加响应式断点
