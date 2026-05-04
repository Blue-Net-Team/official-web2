## Context

### 当前状态
- 方向详情页 `page.tsx` 是 Next.js 服务端组件
- 学习路径数据硬编码在 `data.ts` 中
- `LearningPath` 组件是客户端组件（'use client'），通过 props 接收数据

### 约束条件
- 使用 Next.js 服务端组件规范
- 使用 `publicClient` 进行无需认证的 API 调用
- 需要支持 ISR（增量静态再生）

## Goals / Non-Goals

**Goals:**
- 创建 API 服务层封装后端接口调用
- 改造服务端组件从后端获取学习路径数据
- 配置合理的缓存策略
- 添加错误处理和降级逻辑

**Non-Goals:**
- 不修改 `LearningPath` 客户端组件
- 不实现管理后台界面
- 不修改静态生成参数（`generateStaticParams`）

## Decisions

### 1. 数据获取策略

**决策**：使用 ISR（增量静态再生）+ 服务端获取

**理由**：
- 学习路径数据变更不频繁，适合缓存
- ISR 兼顾性能与数据时效性
- 服务端获取保证 SEO 友好

**配置**：
```typescript
export const revalidate = 3600; // 每小时重新验证
```

### 2. 数据合并策略

**决策**：保留静态基础数据，仅动态获取视频链接

**理由**：
- 静态数据（标题、步骤序号）变更频率极低
- 仅视频链接需要动态管理
- API 失败时可降级到静态数据

**实现**：
```typescript
// 合并静态数据与动态视频链接
learningPathWithVideos = direction.learningPath.map((step) => {
  const videoData = videoResponse.data.learningPath.find(v => v.step === step.step);
  return { ...step, videoLink: videoData?.videoLink || undefined };
});
```

### 3. 错误处理策略

**决策**：API 失败时使用静态数据降级

**理由**：
- 保证页面始终可用
- 避免因后端问题导致页面异常
- 静默降级，不影响用户体验

## Risks / Trade-offs

**[风险] 后端接口未就绪**
- 前端开发可能被阻塞
- 缓解措施：先定义接口契约，使用 Mock 数据进行前端开发

**[风险] 网络请求失败**
- 页面可能显示异常
- 缓解措施：降级到静态数据，保证可用性

**[风险] 缓存不一致**
- 数据更新后前端可能显示旧数据
- 缓解措施：设置合理的 revalidate 时间，或提供手动刷新机制

## Migration Plan

### 部署步骤
1. 创建 API 服务层和类型定义
2. 改造方向详情页组件
3. 本地测试验证
4. 部署前端代码
5. 验证页面功能正常

### 回滚策略
1. 移除服务端数据获取逻辑
2. 恢复使用静态数据
