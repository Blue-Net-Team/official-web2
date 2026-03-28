## Context

蓝网官网需要为三个技术方向（计算机视觉、嵌入式开发、结构设计）提供详情页面。设计稿已完成于 `docs/UI/directions.pen`，包含桌面端(1440px)和移动端(375px)两套设计。

当前状态：
- 主页已有 `DirectionIntroduce` 组件，包含三个方向卡片
- 卡片点击目标路由分别为 `/direction/cv`、`/direction/embed`、`/direction/struct`
- 这些路由页面尚未实现

约束：
- 使用现有技术栈：Next.js 15 + React 19 + TypeScript + Ant Design
- 遵循现有项目结构和代码规范
- 无后端 API 依赖，使用静态数据

## Goals / Non-Goals

**Goals:**
- 实现响应式方向详情页面，完美还原设计稿
- 支持动态路由 `/direction/[slug]`，根据 slug 渲染对应方向内容
- 各方向使用独立主题色系统
- 组件化设计，便于维护和复用

**Non-Goals:**
- 不涉及后端 API 开发
- 不修改主页 DirectionIntroduce 组件
- 不涉及用户认证或权限控制
- 不实现视频链接实际跳转功能（仅展示）

## Decisions

### 1. 路由结构：动态路由

**决定**：使用动态路由 `/direction/[slug]`

**理由**：
- 三个方向页面结构完全一致，仅内容和主题色不同
- 代码复用度高，维护成本低
- 数据集中管理，易于更新

### 2. 数据管理：静态 TypeScript 数据文件

**理由**：
- 方向内容相对稳定，无需频繁更新
- 无需后端 API 支持
- 类型安全，编译时检查

**数据结构**：
```typescript
// src/data/directions/types.ts
interface DirectionData {
  slug: 'cv' | 'embed' | 'struct';
  title: string;
  subtitle: string;
  themeColor: ThemeColors;
  techStack: TechItem[];
  learningPath: LearningStep[];
  careers: CareerItem[];
  recruitment: RecruitmentInfo;
}
```

### 3. 主题色实现：CSS 变量 + CSS Modules

**主题色配置**：
| 方向 | 主色 | 辅色 |
|------|------|------|
| 计算机视觉 (cv) | #8B5CF6 | #A78BFA |
| 嵌入式开发 (embed) | #10B981 | #34D399 |
| 结构设计 (struct) | #3B82F6 | #1E3D9A |

### 4. 组件拆分策略

```
src/components/Direction/
├── HeroSection/
│   ├── index.tsx
│   └── styles.module.css
├── TechStack/
│   ├── index.tsx
│   └── styles.module.css
├── LearningPath/
│   ├── index.tsx
│   └── styles.module.css
├── CareerSection/
│   ├── index.tsx
│   └── styles.module.css
├── RecruitmentInfo/
│   ├── index.tsx
│   └── styles.module.css
└── index.ts  # 统一导出
```

## Risks / Trade-offs

### 风险1：图片资源管理
- **风险**：设计稿中的图片需要手动导出和优化
- **缓解**：使用 Next.js Image 组件自动优化，优先使用 CSS 实现装饰效果

### 风险2：响应式适配复杂度
- **风险**：桌面端和移动端布局差异较大
- **缓解**：使用 Ant Design Grid 系统，移动优先的 CSS 编写策略

### 风险3：主题色一致性
- **风险**：多组件中主题色使用不一致
- **缓解**：通过 CSS 变量统一管理，在页面级别注入主题色变量
