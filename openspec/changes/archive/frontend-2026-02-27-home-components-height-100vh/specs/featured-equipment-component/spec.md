## MODIFIED Requirements

### Requirement: 组件样式符合设计稿

系统 SHALL 按照设计稿规格渲染组件样式。

#### Scenario: 容器样式正确
- **WHEN** 组件渲染完成
- **THEN** 容器样式符合以下规格：
  - 高度：110vh（视口高度的 110%）
  - 宽度：100%
  - 背景：半透明黑色渐变 + 右侧CSS背景装饰图片（background-image）

#### Scenario: 按钮样式正确
- **WHEN** 按钮渲染完成
- **THEN** 按钮样式符合以下规格：
  - 背景：白色 (#FFFFFF)
  - 圆角：20px
  - 内边距：上下9px，左右15px
  - 包含右箭头图标
