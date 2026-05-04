## Why

主页需要展示团队氛围信息，让访客了解团队的文化和工作环境，吸引潜在成员加入。当前主页缺少专门的团队氛围展示模块。

根据 Pixso 设计稿 `https://pixso.cn/app/design/EeEM-rHXfSZlPWwF0iD2IQ?item-id=12:100`，需要实现一个"团队氛围"组件，展示"重新定义团队氛围"的主题内容。

## What Changes

- 新增 `src/components/Home/TeamVibe/index.tsx` - 团队氛围组件主体
- 新增 `src/components/Home/TeamVibe/styles.module.css` - 组件样式
- 修改 `src/app/(public)/(home)/page.tsx` - 在 FeaturedEquipment 下方引入并调用团队氛围组件

## Capabilities

### New Capabilities

- `team-vibe-component`: 团队氛围展示组件，包含主标题、内容卡片（左侧文字描述区域 + 右侧团队照片），展示团队文化和工作环境

### Modified Capabilities

无

## Impact

- 新增文件：`src/components/Home/TeamVibe/index.tsx`、`src/components/Home/TeamVibe/styles.module.css`
- 修改文件：`src/app/(public)/(home)/page.tsx`
- 使用现有静态资源：`src/assets/team_vibe.jpg`
