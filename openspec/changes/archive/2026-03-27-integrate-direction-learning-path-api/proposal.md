## Why

方向介绍页面的学习路径数据目前硬编码在前端 `data.ts` 中，`videoLink` 字段为空字符串。需要从后端动态获取视频链接，并使用 Next.js 服务端组件直接 await 获取数据，实现数据的动态管理。

## What Changes

- 新增 `direction.service.ts` API 服务层，封装方向学习路径接口调用
- 新增 `direction.dto.ts` 类型定义，与后端 DTO 保持一致
- 改造方向详情页服务端组件，从后端获取学习路径数据
- 配置 ISR 缓存策略，优化性能
- 添加错误处理和降级逻辑，确保 API 失败时使用静态数据

## Capabilities

### New Capabilities

- `direction-learning-path-service`: 方向学习路径 API 服务，封装与后端的交互逻辑

### Modified Capabilities

- `direction-detail-page`: 方向详情页需要改造为从后端获取学习路径数据

## Impact

**新增文件**：
- `src/apis/services/direction.service.ts` - API 服务层
- `src/apis/schema/direction.dto.ts` - 类型定义

**修改文件**：
- `src/app/(public)/(other)/direction/[slug]/page.tsx` - 添加服务端数据获取逻辑

**依赖关系**：
- 依赖后端 `GET /api/v1/directions/{slug}/learning-path` 接口
- 使用 `publicClient` 进行无需认证的 API 调用
- 遵循 Next.js 服务端组件规范
