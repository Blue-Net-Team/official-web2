## ADDED Requirements

### Requirement: 主页组件视口高度统一

系统 SHALL 将主页所有主要组件的高度设置为视口高度的 110%（110vh），以提供一致的全屏滚动体验和更好的观感。

#### Scenario: 组件高度为视口高度
- **WHEN** 用户访问主页
- **THEN** 所有主页组件（TopContent、Competitions、AchievementAndResources、FeaturedEquipment、TeamVibe、DirectionIntroduce、RecruitmentProcess）的高度均为 110vh

#### Scenario: 组件在不同屏幕尺寸下自适应
- **WHEN** 用户在不同屏幕尺寸下访问主页
- **THEN** 每个组件的高度自动适应当前视口高度的 110%

### Requirement: 主页容器移除固定最小高度

系统 SHALL 移除主页容器的固定 minHeight 设置，让页面高度由组件自然决定。

#### Scenario: 主页容器无固定最小高度
- **WHEN** 主页渲染完成
- **THEN** 主页容器不设置 minHeight 属性，页面总高度由各组件高度累加得出

### Requirement: 组件内部布局保持不变

系统 SHALL 在调整组件高度时保持各组件内部布局和功能不变。

#### Scenario: 组件内部样式不受影响
- **WHEN** 组件高度调整为 110vh
- **THEN** 组件内部的文字、图片、按钮等元素的位置和样式保持不变
