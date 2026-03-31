## Context

前端有 3 个 Server Component 页面在 build 时获取后端数据，但未设置 `revalidate`，导致 build 后内容固定：

| 页面 | 路径 | 获取的数据 | 当前状态 |
|------|------|-----------|---------|
| Home | `(home)/page.tsx` | 竞赛列表 | 纯 SSG |
| Competitions | `competitions/page.tsx` | 竞赛列表 | 纯 SSG |
| Lab Environment | `lab-environment/page.tsx` | 场地 + 设备 | 纯 SSG |
| Direction/[slug] | `direction/[slug]/page.tsx` | 学习路径 | ISR (3600s 硬编码) |

当前 `next.config.ts` 使用 `output: 'standalone'`，已支持 ISR。

## Goals / Non-Goals

**Goals:**
- 为所有数据获取型 SSG 页面统一添加 ISR 支持
- 提供全局默认 revalidate 时间配置，各页面可按需覆盖
- 统一管理 revalidate 值，避免各页面硬编码不一致
- 保持现有 `output: 'standalone'` 部署方式不变

**Non-Goals:**
- 不引入 On-Demand Revalidation（后续可扩展）
- 不改变页面渲染策略（不改为 SSR）
- 不修改后端 API

## Decisions

### 决策 1：使用环境变量 + 常量文件统一管理 revalidate 值

**方案**: 在 `src/frontend/src/config/isr.ts` 中定义 revalidate 常量，支持通过环境变量覆盖。

```typescript
// src/frontend/src/config/isr.ts
export const ISR = {
  default: Number(process.env.NEXT_PUBLIC_ISR_REVALIDATE) || 3600,
  home: Number(process.env.NEXT_PUBLIC_ISR_HOME) || 3600,
  competitions: Number(process.env.NEXT_PUBLIC_ISR_COMPETITIONS) || 3600,
  labEnvironment: Number(process.env.NEXT_PUBLIC_ISR_LAB) || 3600,
  direction: Number(process.env.NEXT_PUBLIC_ISR_DIRECTION) || 3600,
}
```

**理由**: Next.js 的 `export const revalidate` 必须是静态值（不能是运行时变量），但环境变量在 build 时注入，满足静态要求。集中管理比分散在各页面更易维护。

**备选方案**: 
- ❌ 在 `next.config.ts` 中配置 → Next.js 不支持全局 revalidate 配置
- ❌ 使用 `fetch` 的 `next: { revalidate }` 选项 → 需要每个 fetch 调用都设置，不如页面级别统一

### 决策 2：页面级 `export const revalidate`

**方案**: 每个页面文件顶部导出 `revalidate` 常量：

```typescript
import { ISR } from '@/config/isr'
export const revalidate = ISR.home
```

**理由**: 这是 Next.js 推荐的 ISR 配置方式，与现有 `direction/[slug]` 页面一致。`export const revalidate` 支持的环境变量在 build 时求值，ISR 能正常工作。

### 决策 3：所有页面使用相同默认值 3600s（1 小时）

**理由**: 这些页面展示的内容（竞赛、场地、设备）更新频率不高，1 小时是合理的平衡点。通过环境变量可按页面单独调整。

## Risks / Trade-offs

- **[数据延迟]** ISR 最多延迟 `revalidate` 秒后更新 → 对于展示型内容完全可接受
- **[环境变量注入时机]** `process.env` 在 `export const revalidate` 中使用时，必须在 build 时可用 → 通过 `.env` 或构建环境注入，无问题
- **[standalone 模式兼容]** `output: 'standalone'` 完全支持 ISR，无需额外配置 → 无风险
