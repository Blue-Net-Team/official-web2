# 招新流程区块组件实现

## 背景

首页需要新增一个"招新流程"区块，用于展示团队招新的三个主要步骤：报名加入、参加考核、正式录用。该区块将放置在 `DirectionIntroduce` 组件下方，帮助潜在申请者了解加入团队的流程。

## 设计稿来源

- **主容器设计稿**: https://pixso.cn/app/design/EeEM-rHXfSZlPWwF0iD2IQ?item-id=29:22
- **流程卡片设计稿**: https://pixso.cn/app/design/EeEM-rHXfSZlPWwF0iD2IQ?item-id=29:25

## 目标

实现一个展示招新流程的React组件，包含：
1. 主标题区域
2. 三个流程卡片（报名加入、参加考核、正式录用）
3. 卡片之间的箭头连接指示器
4. 响应式布局支持

## 组件结构

```
RecruitmentProcess/
├── index.tsx                 # 主组件
├── styles.module.css         # 样式文件
└── ProcessCard/
    ├── index.tsx             # 流程卡片子组件
    └── styles.module.css     # 卡片样式
```

## 设计规范

### 主容器 (content8--招新流程)
- 宽度: 1270px
- 高度: 780px
- 内边距: 70.5px 112px (垂直 水平)
- 布局: 垂直Flex布局，间距20px

### 流程卡片 (报名流程卡片/考核流程卡片)
- 尺寸: 260px × 340px
- 圆角: 36px
- 边框: 1px solid rgba(232, 104, 53, 1) - 橙色边框
- 内边距: 22px 24px
- 布局: 垂直Flex，间距9px

### 卡片内容结构
1. **头部区域**: 图标(32×32) + 标题(20px Bold)
2. **描述文本**: 16px Regular，白色字体
3. **第一个卡片额外包含**: "立即加入"按钮（白色背景，圆角64px）

### 箭头连接指示器
- 尺寸: 64px × 64px
- 双箭头图标，橙色渐变效果

### 颜色规范
- 主色调: #E86835 (橙色)
- 文字颜色: #FFFFFF (白色)
- 按钮背景: #FFFFFF (白色)
- 按钮文字: #000000 (黑色)

### 字体规范
- 标题: Microsoft YaHei, 20px, Bold
- 描述: Microsoft YaHei, 16px, Regular
- 按钮: Microsoft YaHei, 14px, Bold

## 内容文案

### 卡片1 - 报名加入
- 标题: 报名加入
- 描述: 报名现已启动，每学年第一学期开始招新，不限制专业，仅对大一和大二的同学开放。
- 按钮: 立即加入 →

### 卡片2 - 参加考核
- 标题: 参加考核
- 描述: 考核流程将会在报名结束后的两周内陆续启动，每个方向的考核时间和轮次都有差异，难度大体相同

### 卡片3 - 正式录用
- 标题: 正式录用
- 描述: 考核流程将会在报名结束后的两周内陆续启动，每个方向的考核时间和轮次都有差异，难度大体相同

## 交互行为

1. **立即加入按钮**: 点击后跳转至报名页面
2. **卡片悬停效果**: 可选的悬停动画提升用户体验

## 放置位置

该组件应插入到 `src/app/(public)/(home)/page.tsx` 中 `<DirectionIntroduce />` 组件的下方。

## 参考实现

参考现有 `DirectionIntroduce` 组件的目录结构和实现方式：
- 路径: `src/components/Home/DirectionIntroduce/`
- 模式: 主组件 + 子组件(Card) + CSS Modules

## 依赖

- React 18+
- Next.js 14+
- CSS Modules
- 项目现有样式体系
