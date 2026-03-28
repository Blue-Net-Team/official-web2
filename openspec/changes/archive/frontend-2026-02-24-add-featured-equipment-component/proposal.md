## Why

主页需要展示团队的精选装备信息，让访客了解实验室的先进设备资源。当前主页已有竞赛列表、成就资源等组件，但缺少专门的装备展示模块。

根据 Pixso 设计稿 `https://pixso.cn/app/design/EeEM-rHXfSZlPWwF0iD2IQ?item-id=12:75`，需要实现一个"精选装备"组件，展示"3D打印与3轴数铣"等设备信息。

## What Changes

- 新增 `src/components/Home/FeaturedEquipment/index.tsx` - 精选装备组件主体
- 新增 `src/components/Home/FeaturedEquipment/styles.module.css` - 组件样式
- 修改 `src/app/(public)/(home)/page.tsx` - 引入并调用精选装备组件

## Capabilities

### New Capabilities

- `featured-equipment-component`: 精选装备展示组件，包含设备图标、标题、描述文字和"浏览更多团队装备"按钮，容器右侧通过CSS背景显示装饰图片

### Modified Capabilities

无

## Impact

- 新增文件：`src/components/Home/FeaturedEquipment/index.tsx`、`src/components/Home/FeaturedEquipment/styles.module.css`
- 修改文件：`src/app/(public)/(home)/page.tsx`
- 新增静态资源：设备图标图片（如需要）
