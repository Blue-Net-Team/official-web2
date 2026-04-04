## Context

当前后端已实现 `GET /api/v1/assessment-times` 接口，返回考生可见的考核时间列表（按方向+年级过滤）。前端导航栏已为考生角色提供"考核中心"入口（路由到 `/assessment`），但该页面尚未创建。

设计稿 `docs/UI/assessment time.pen` 定义了页面视觉规范：暗色主题、玻璃拟态卡片、三种状态样式（蓝色进行中/灰色未开始/绿色已结束）。

现有 `Profile/AssessmentList` 组件使用 mock 数据，新页面将使用真实后端数据。

技术约束：
- 前端使用 Next.js 15 App Router + React 19 + Ant Design 6 + CSS Modules
- 暗色主题：背景 #000000，卡片 rgba(255,255,255,0.03)，边框 rgba(255,255,255,0.06)
- 认证：HttpOnly Cookie + CSRF Token
- 后端 DDD 四层架构：API → Application → Domain → Infrastructure

## Goals / Non-Goals

**Goals:**
- 考生登录后可查看自己方向和年级的考核时间安排
- 每张卡片显示考核状态、时间范围、限时信息、答题进度
- 后端同时提供列表内嵌进度和独立进度查询两种方式
- 页面视觉与设计稿一致

**Non-Goals:**
- 分数展示（等评审系统完成后接入）
- 答题/查看详情的跳转逻辑（等考核详情页实现）
- 管理员视角的考核时间管理（已由 admin 端实现）

## Decisions

### 1. 进度数据通过扩展 DTO 内嵌返回

**选择**: 在现有 `AssessmentTimeDTO` 中新增 `totalQuestions` 和 `completedQuestions` 字段

**替代方案**: 前端获取列表后再逐个调用进度接口
**理由**: 避免前端 N+1 请求，一次查询返回所有数据。考核时间列表通常不超过 10 条，JOIN 查询性能开销可忽略。

### 2. 同时提供独立进度接口

**选择**: 新增 `GET /api/v1/assessment-times/{id}/progress`
**理由**: 后续考核详情页、实时刷新等场景可复用，无需重新获取完整列表。

### 3. 考核状态由前端计算

**选择**: 前端根据 `startTime`/`endTime` 与当前时间对比计算 status
**替代方案**: 后端计算 status 并返回
**理由**: 状态是纯时间比较，无需后端计算；前端可以实时更新（如倒计时），无需轮询后端。

### 4. Repository 层使用 MyBatis-Plus LambdaQueryWrapper 实现计数

**选择**: 在 `AssessmentQuestionRepositoryImpl` 和 `AssessmentAnswerRepositoryImpl` 中使用 `LambdaQueryWrapper.count()` 方法
**替代方案**: 编写自定义 SQL / Mapper XML
**理由**: 简单计数查询不需要自定义 SQL，MyBatis-Plus 内置方法足够。

### 5. 前端页面作为客户端组件实现

**选择**: `'use client'` 组件，使用 `useEffect` + `useState` 获取数据
**理由**: 需要认证状态检查、客户端时间比较、交互按钮；不符合 ISR 静态页面模式。

## Risks / Trade-offs

- **[DTO 扩展向后兼容性]** → 新增字段（totalQuestions, completedQuestions）是可选的，不影响现有客户端
- **[计数查询性能]** → 当题目数量很大时，JOIN 统计可能有性能影响。缓解：考核时间通常不超过 10 条，每条题数有限，可接受
- **[前端时间判断准确性]** → 依赖客户端本地时间，用户修改系统时间可能导致状态判断错误。缓解：后端 startTime/endTime 使用服务器时间，前端仅用于 UI 展示，实际考核入口仍由后端时间控制
