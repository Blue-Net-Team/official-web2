## ADDED Requirements

### Requirement: 精选装备组件渲染

系统 SHALL 在主页渲染精选装备组件，展示设备图标、标题、描述文字和操作按钮。

#### Scenario: 组件正常渲染
- **WHEN** 用户访问主页
- **THEN** 系统渲染精选装备组件，包含以下元素：
  - 设备图标（52x52px）
  - 标题文字"3D打印与3轴数铣"
  - 描述文字
  - "浏览更多团队装备"按钮
  - 容器右侧CSS背景装饰图片

### Requirement: 组件样式符合设计稿

系统 SHALL 按照设计稿规格渲染组件样式。

#### Scenario: 容器样式正确
- **WHEN** 组件渲染完成
- **THEN** 容器样式符合以下规格：
  - 宽度：1084px
  - 圆角：72px
  - 边框：3px 实线 #2F27B0
  - 背景：半透明黑色 + 右侧CSS背景装饰图片（background-image）

#### Scenario: 按钮样式正确
- **WHEN** 按钮渲染完成
- **THEN** 按钮样式符合以下规格：
  - 背景：白色 (#FFFFFF)
  - 圆角：20px
  - 内边距：上下9px，左右15px
  - 包含右箭头图标

### Requirement: 浏览更多按钮交互

系统 SHALL 为"浏览更多团队装备"按钮提供点击交互。

#### Scenario: 按钮点击响应
- **WHEN** 用户点击"浏览更多团队装备"按钮
- **THEN** 系统触发导航事件（具体路由待后续实现）

### Requirement: 组件可配置

系统 SHALL 支持通过 props 配置组件内容。

#### Scenario: 自定义装备信息
- **WHEN** 传入自定义的装备信息 props
- **THEN** 组件渲染对应的图标、标题和描述

### Requirement: 响应式布局

系统 SHALL 在不同屏幕尺寸下保持良好的显示效果。

#### Scenario: 小屏幕适配
- **WHEN** 屏幕宽度小于容器宽度
- **THEN** 组件自适应缩放，保持内容可读性
