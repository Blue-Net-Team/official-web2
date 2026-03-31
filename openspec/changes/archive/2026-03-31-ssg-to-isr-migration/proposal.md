## Why

Home、Competitions、Lab Environment 三个页面使用 Server Component 获取后端数据，但没有设置 `revalidate`，属于纯 SSG——build 后内容固定，后端数据变更无法反映到页面上。需要为这些页面引入 ISR（增量静态再生成），使页面能按配置的时间间隔自动刷新，同时保持静态页面的性能优势。

## What Changes

- 为 Home、Competitions、Lab Environment 三个 SSG 页面添加 `revalidate` 配置，从纯 SSG 升级为 SSG + ISR
- 在前端配置中新增全局默认 ISR 刷新间隔配置项，各页面可覆盖
- 方向详情页已有 `revalidate = 3600`，改为读取统一配置

## Capabilities

### New Capabilities
- `frontend-isr-config`: 前端 ISR 全局配置能力，提供统一的 revalidate 时间配置，各页面可按需覆盖

### Modified Capabilities
- `frontend-home-components-fullscreen`: Home 页面的 CompetitionsTable 组件需添加 revalidate
- `competitions-page`: 竞赛列表页需添加 revalidate
- `frontend-lab-environment-page`: 实验室环境页的场地和设备组件需添加 revalidate
- `frontend-direction-detail-page`: 方向详情页的硬编码 revalidate=3600 改为读取统一配置

## Impact

- **前端页面文件**: `(home)/page.tsx`、`competitions/page.tsx`、`lab-environment/page.tsx`、`direction/[slug]/page.tsx`
- **配置文件**: `next.config.ts` 新增 ISR 相关环境变量或配置
- **部署行为**: 从纯静态变为 ISR，`output: 'standalone'` 已支持，无需变更
- **无 breaking change**: 纯粹的增量改进，不影响现有功能和 API
