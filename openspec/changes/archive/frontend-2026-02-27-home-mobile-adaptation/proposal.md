## Why

当前主页UI设计文件宽度固定为1270px，未能占满浏览器视口，且所有组件均使用固定像素布局，在移动端设备上显示效果不佳。随着移动设备访问量的增加，需要为主页提供完整的移动端适配，确保在不同屏幕尺寸下都能提供良好的用户体验。

## What Changes

- 修正UI设计文件 `docs/UI/home/Index.html` 的宽度问题，使其占满浏览器视口
- 为所有主页组件添加响应式布局支持
- 实现移动端导航栏适配（汉堡菜单或折叠导航）
- 调整字体大小、间距、卡片布局以适应小屏幕
- 优化图片和背景在不同设备上的显示效果
- 添加媒体查询断点，支持平板和手机设备

## Capabilities

### New Capabilities

- `home-responsive-layout`: 主页响应式布局能力，包括所有子组件的移动端适配方案

### Modified Capabilities

无（这是新增能力，不修改现有功能需求）

## Impact

**受影响的文件：**
- `docs/UI/home/Index.html` - UI设计文件宽度修正
- `src/app/(public)/(home)/page.tsx` - 主页容器响应式适配
- `src/app/(public)/(home)/styles.module.css` - 背景图响应式适配
- `src/components/Home/TopContent/` - 英雄区域移动端适配
- `src/components/Home/Competitions/` - 竞赛卡片移动端适配
- `src/components/Home/DirectionIntroduce/` - 方向介绍移动端适配
- `src/components/Home/RecruitmentProcess/` - 招新流程移动端适配
- `src/components/Home/AchievementAndResources/` - 成果资源移动端适配
- `src/components/Home/FeaturedEquipment/` - 特色设备移动端适配
- `src/components/Home/TeamVibe/` - 团队氛围移动端适配
- `src/components/PublicNavbar/` - 导航栏移动端适配

**技术依赖：**
- Ant Design Grid系统（已集成）
- CSS媒体查询
- CSS变量实现动态尺寸

**兼容性考虑：**
- 需要支持主流移动设备尺寸（320px - 768px）
- 平板设备尺寸（768px - 1024px）
- 桌面设备尺寸（1024px以上）
