## Context

当前个人主页使用 `MockProfileService` 提供数据，与后端 API 结构完全对应（Mock 服务注释中已标注对应的后端 API 路径）。需要将 Mock 调用替换为真实 API 调用。

现有前端架构：
- API 客户端：`src/apis/client.ts`（`apiClient` 自动携带 Bearer Token）
- 服务层模式：`src/apis/services/*.service.ts`
- 类型定义：`src/apis/schema/type.ts` + `src/types/*.ts`

## Goals / Non-Goals

**Goals:**
- 创建 `UserService` 封装用户相关 API 调用
- 将个人主页从 Mock 数据切换到真实 API
- 根据用户角色控制字段可编辑性（MEMBER+ 可修改更多字段）
- 保持现有 UI/UX 不变

**Non-Goals:**
- 不修改后端 API
- 不修改 UI 样式
- 不实现考核列表（后端暂无对应 API）

## Decisions

### Decision 1: API 服务层设计

**选择**: 创建 `src/apis/services/user.service.ts`，参考现有 `auth.service.ts` 模式

**理由**:
- 与现有代码风格一致
- 便于测试和维护
- 统一错误处理

**API 方法映射**:
```typescript
// UserService
getUserInfo(): Promise<UserInfo>
updateProfile(data: UpdateProfileRequest): Promise<void>
getTabCounts(): Promise<TabCounts>

// UserExperienceService（可合并到 UserService 或单独创建）
getExperiences(type?: string): Promise<Experience[]>
createExperience(data: CreateExperienceRequest): Promise<Experience>
updateExperience(id: string, data: UpdateExperienceRequest): Promise<Experience>
deleteExperience(id: string): Promise<void>
```

### Decision 2: 权限控制

**选择**: 在组件层根据 `authStore.userInfo.roleName` 判断可编辑字段

**理由**:
- 后端已有权限验证（双重保障）
- 前端即时响应用户体验更好
- 避免用户修改后才发现无权限

**权限矩阵**:
| 字段 | CANDIDATE | MEMBER+ |
|------|-----------|---------|
| username | 禁用 | 可编辑 |
| nickname | 可编辑 | 可编辑 |
| bio | 可编辑 | 可编辑 |
| gender | 禁用 | 可编辑 |
| college | 禁用 | 可编辑 |
| major | 禁用 | 可编辑 |
| direction | 禁用 | 可编辑 |

### Decision 3: 页面数据获取

**选择**: 保持服务端组件架构，使用 API 调用获取初始数据

**理由**:
- Next.js App Router 推荐模式
- SEO 友好
- 首屏渲染更快

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 后端 API 未完全实现 | 逐步接入，已确认 API 可用后再切换 |
| 类型不匹配 | 更新前端类型定义与后端 DTO 对齐 |
| 401 错误处理 | `apiClient` 已自动处理，跳转登录页 |
