## Why

当前个人主页（`/profile`）使用 Mock 数据（`MockProfileService`），无法展示真实用户信息。后端已实现完整的用户信息和经历管理 API，前端需要接入这些真实接口以提供实际功能。

## What Changes

- 创建 `src/apis/services/user.service.ts` - 用户信息 API 服务层
- 修改 `src/app/(public)/(other)/profile/page.tsx` - 从 Mock 切换到真实 API
- 修改 `src/components/Profile/ProfileInfo/index.tsx` - 使用真实 API 更新用户信息
- 修改 `src/components/Profile/ExperienceSection/index.tsx` - 使用真实 API 管理经历
- 更新 `src/apis/schema/type.ts` - 添加缺失的类型定义（与后端 DTO 对齐）
- 根据用户角色动态控制字段的可编辑性（MEMBER 及以上可修改更多字段）

### 后端 API 对接清单

| API | 方法 | 用途 |
|-----|------|------|
| `/api/v1/user/info` | GET | 获取当前用户信息 |
| `/api/v1/user/info` | PUT | 更新用户信息 |
| `/api/v1/user/tab-counts` | GET | 获取 Tab 计数 |
| `/api/v1/user/experiences` | GET | 获取经历列表（支持 type 过滤） |
| `/api/v1/user/experiences` | POST | 创建经历 |
| `/api/v1/user/experiences/{id}` | PUT | 更新经历 |
| `/api/v1/user/experiences/{id}` | DELETE | 删除经历 |

### 暂不实现

- **考核列表**：后端暂无对应的用户考核 API，继续使用 Mock 数据

## Capabilities

### New Capabilities

- `user-profile-api`: 用户信息 API 服务层，封装用户信息获取、更新等操作
- `user-experience-api`: 用户经历 API 服务层，封装经历 CRUD 操作

### Modified Capabilities

无现有能力修改。

## Impact

- **前端文件**：
  - `src/apis/services/user.service.ts`（新增）
  - `src/apis/schema/type.ts`（更新类型）
  - `src/app/(public)/(other)/profile/page.tsx`
  - `src/components/Profile/ProfileInfo/index.tsx`
  - `src/components/Profile/ExperienceSection/index.tsx`
  - `src/types/profile.ts`（可能需要更新）

- **依赖**：使用现有的 `apiClient`（自动携带认证 Token）

- **用户体验**：用户将看到和修改真实的个人信息
