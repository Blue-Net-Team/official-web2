## Context

前端项目使用 Next.js + React + TypeScript + Ant Design，主页需要新增一个团队氛围展示组件。根据 Pixso 设计稿 (item-id=12:100)，该组件需要展示"重新定义团队氛围"的主题，包含主标题、内容卡片（左侧文字区域 + 右侧图片区域）。

设计稿关键信息：
- 组件尺寸：1270x780px
- 主标题："重新定义团队氛围"，字号43px，白色，Microsoft YaHei Bold
- 内容卡片：
  - 尺寸：1177x512px
  - 圆角：36px（左上、左下）
  - 边框：3px 实线，颜色 #1E3D9A
  - 布局：左侧文字区域(721px) + 右侧图片区域(756px，带渐变遮罩)
- 左侧文字区域：
  - 内边距：36px 32px
  - 标题："队内氛围融洽，技术精湛"，字号35px，白色，Bold
  - 描述文字：两段，字号20px，白色，Regular
    - "团队氛围轻松融洽，弹性工作，无竞赛、论文等硬性指标。旨在培养学生学习更多新技术应用到工程实践"
    - "进入团队后，可以跟学长和老师学习行业前沿技术，共同实现项目落地，丰富简历内容"
- 右侧图片区域：
  - 使用 `team_vibe.jpg` 作为背景
  - 叠加从左到右的渐变遮罩（从黑色到透明），实现文字区域与图片的平滑过渡

## Goals / Non-Goals

**Goals:**
- 实现与设计稿一致的团队氛围组件
- 组件宽度100%，适配容器
- 遵循现有组件的编写规范（参考 FeaturedEquipment、AchievementAndResources）
- 响应式布局适配

**Non-Goals:**
- 不实现后端 API 对接（数据硬编码）
- 不实现图片轮播或多图展示
- 不实现国际化支持

## Decisions

### D1: 组件结构设计

**决策**: 创建独立的 `TeamVibe` 组件目录，包含 `index.tsx` 和 `styles.module.css`

**理由**:
- 与现有 `FeaturedEquipment`、`AchievementAndResources` 等组件保持一致的组织结构
- 便于后续扩展和维护
- CSS Module 避免样式冲突

### D2: 布局实现方式

**决策**: 使用 Ant Design 的 `Flex` 组件 + CSS Module 实现布局

**理由**:
- 与现有组件保持技术栈一致
- Flex 组件提供灵活的布局控制
- CSS Module 处理复杂样式（渐变遮罩、边框圆角等）

**实现要点**:
```tsx
<Flex vertical className={styles.container}>
  {/* 主标题 */}
  <h2 className={styles.mainTitle}>重新定义团队氛围</h2>
  
  {/* 内容卡片 */}
  <Flex className={styles.contentCard}>
    {/* 左侧文字区域 */}
    <Flex vertical className={styles.textArea}>
      <h3 className={styles.subTitle}>队内氛围融洽，技术精湛</h3>
      <p className={styles.description}>...</p>
      <p className={styles.description}>...</p>
    </Flex>
    
    {/* 右侧图片区域 - 通过CSS背景实现 */}
    <div className={styles.imageArea} />
  </Flex>
</Flex>
```

### D3: 渐变遮罩实现

**决策**: 右侧图片区域使用 CSS 伪元素或多层背景实现从左到右的渐变遮罩

**理由**:
- 设计稿要求图片左侧有渐变遮罩，与文字区域平滑过渡
- CSS `linear-gradient` 可以精确控制渐变方向和颜色

**实现方案**:
```css
.imageArea {
  background-image: 
    linear-gradient(to right, rgba(0,0,0,1) 0%, rgba(0,0,0,0) 100%),
    url('/path/to/team_vibe.jpg');
  background-size: cover;
  background-position: center;
}
```

### D4: 图片资源处理

**决策**: 使用 Next.js 的静态图片导入方式，通过 CSS background-image 引用

**理由**:
- `team_vibe.jpg` 已存在于 `src/assets/` 目录
- CSS 背景图适合装饰性图片，便于实现渐变遮罩效果
- 与 FeaturedEquipment 组件的实现方式保持一致

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 渐变遮罩在不同浏览器可能有差异 | 使用 CSS 标准渐变语法，测试主流浏览器 |
| 组件高度固定可能影响响应式布局 | 使用 min-height 和百分比布局适配不同屏幕 |
| 图片比例变化可能影响视觉效果 | 使用 background-size: cover 保持图片比例 |

## File Structure

```
src/components/Home/TeamVibe/
├── index.tsx           # 组件主体
└── styles.module.css   # 组件样式
```
