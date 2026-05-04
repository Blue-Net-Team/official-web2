## Why

主页各组件目前使用固定高度或内容自适应高度，导致页面视觉体验不一致。为了提供更好的全屏滚动体验和视觉一致性，需要将主页的每个主要组件高度调整为视口高度的 110%（110vh），使用户可以通过滚动逐个浏览每个内容模块，同时提供更好的观感。

## What Changes

- 将主页各组件容器高度调整为 110vh（视口高度的 110%）
- 受影响的组件包括：
  - TopContent
  - Competitions
  - AchievementAndResources
  - FeaturedEquipment
  - TeamVibe
  - DirectionIntroduce
  - RecruitmentProcess
- 移除主页容器的 `minHeight: '250vh'` 设置，因为各组件已使用 110vh

## Capabilities

### New Capabilities

- `home-components-fullscreen`: 主页各组件全屏视口高度展示能力

### Modified Capabilities

- `featured-equipment-component`: 调整组件高度为 110vh
- `team-vibe-component`: 调整组件高度为 110vh

## Impact

- 影响文件：
  - `src/app/(public)/(home)/page.tsx` - 移除 minHeight 设置
  - `src/components/Home/TopContent/` - 调整高度
  - `src/components/Home/Competitions/` - 调整高度
  - `src/components/Home/AchievementAndResources/` - 调整高度
  - `src/components/Home/FeaturedEquipment/` - 调整高度
  - `src/components/Home/TeamVibe/` - 调整高度
  - `src/components/Home/DirectionIntroduce/` - 调整高度
  - `src/components/Home/RecruitmentProcess/` - 调整高度
- 用户体验：页面滚动体验更加统一，每个模块占据完整视口并留有适当余量
