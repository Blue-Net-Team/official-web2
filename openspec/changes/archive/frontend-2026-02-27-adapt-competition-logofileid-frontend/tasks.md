## Tasks

### Task 1: 更新类型定义

**File:** `src/apis/schema/type.ts`

- [x] 在 `CompetitionBriefDTO` 接口中添加 `logoFileId: number | null` 字段
- [x] 在 `logoUrl` 字段上添加 JSDoc 注释 `@deprecated 请使用 logoFileId`

### Task 2: 更新竞赛卡片组件

**File:** `src/components/Home/CompetitionCard/index.tsx`

- [x] 修改图片URL构建逻辑，使用 `logoFileId` 替代 `logoUrl`
- [x] 更新条件判断，从 `competition.logoUrl` 改为 `competition.logoFileId`
- [x] 图片src改为 `/api/v1/file/download/${competition.logoFileId}`

### Task 3: 验证和测试

- [x] 运行 TypeScript 类型检查确保无错误
- [x] 手动测试竞赛列表页面正常显示
- [x] 验证有Logo和无Logo的竞赛都正确显示
