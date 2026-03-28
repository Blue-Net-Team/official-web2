## Context

前端项目使用 Next.js + React + TypeScript + Ant Design，主页需要新增一个精选装备展示组件。根据 Pixso 设计稿 (item-id=12:75)，该组件需要展示"3D打印与3轴数铣"设备信息，包含图标、标题、描述和跳转按钮。

设计稿关键信息：
- 组件尺寸：1270x780px
- 背景：渐变色（从透明到半透明紫蓝色 #3329CE）
- 内容容器：圆角72px，紫色边框(#2F27B0, 3px)
- 容器右侧装饰：CSS背景图片（非独立div元素），类似主页的bg1/bg2实现方式
- 按钮：白色背景，圆角20px，带右箭头图标

## Goals / Non-Goals

**Goals:**
- 实现与设计稿一致的精选装备组件
- 组件可复用，支持配置不同的装备信息
- 遵循现有组件的编写规范（参考 AchievementAndResources）
- 响应式布局适配

**Non-Goals:**
- 不实现后端 API 对接（数据硬编码）
- 不实现多装备轮播（当前仅展示单个装备）
- 不实现国际化支持

## Decisions

### D1: 组件结构设计

**决策**: 创建独立的 `FeaturedEquipment` 组件目录，包含 `index.tsx` 和 `styles.module.css`

**理由**:
- 与现有 `AchievementAndResources`、`Competitions` 等组件保持一致的组织结构
- 便于后续扩展和维护
- CSS Module 避免样式冲突

### D2: 布局实现方式

**决策**: 使用 Ant Design 的 `Flex` 组件 + CSS Module 实现布局

**理由**:
- 与现有组件保持技术栈一致
- Flex 组件提供灵活的布局控制
- CSS Module 处理复杂样式（渐变背景、边框等）

**实现要点**:
```tsx
<Flex className={styles.container}>
  <Flex vertical className={styles.contentArea}>
    {/* 图标 */}
    {/* 标题 */}
    {/* 描述 */}
    {/* 按钮 */}
  </Flex>
  {/* 右侧装饰通过CSS背景实现，无需独立div */}
</Flex>
```

**CSS背景实现**:
```css
.container {
  background-image: url('/path/to/decoration.png');
  background-position: right center;
  background-repeat: no-repeat;
  background-size: auto 100%;
}
```

### D3: 图片资源处理

**决策**:
- 设备图标：使用 Next.js 的 `Image` 组件加载
- 容器装饰图：使用 CSS `background-image` 属性（参考主页bg1/bg2实现）

**理由**:
- CSS背景图适合装饰性图片，不占用布局空间
- 与主页背景实现方式一致，便于维护
- Next.js Image 组件用于需要交互或语义化的图片

### D4: 按钮交互

**决策**: 按钮使用 Ant Design 的 `Button` 组件，点击跳转到装备详情页（路由待定）

**理由**:
- 保持 UI 组件一致性
- 内置加载状态和交互效果
- 便于后续添加路由跳转逻辑

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 设计稿中的图片资源需要从 Pixso 导出 | 使用 Pixso MCP 工具获取图片，或使用占位图 |
| 渐变背景在不同浏览器可能有差异 | 使用 CSS 标准渐变语法，测试主流浏览器 |
| 组件高度固定可能影响响应式布局 | 使用 min-height 和百分比布局适配不同屏幕 |

## File Structure

```
src/components/Home/FeaturedEquipment/
├── index.tsx           # 组件主体
└── styles.module.css   # 组件样式

src/assets/
├── equipment-icon.png  # 设备图标（新增）
└── equipment-bg.png    # 装饰图片（新增）
```
