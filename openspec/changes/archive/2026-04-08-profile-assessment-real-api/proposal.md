## Why

个人主页的"我的考核"部分目前使用 mock 数据，无法展示用户真实的考核进度和状态。后端考核 API 已经基本完成，需要将前端 mock 数据切换到真实接口，实现数据的实时更新和准确的考核进度展示。

## What Changes

- **移除 mock 数据依赖**：删除 `MockProfileService.getAssessments()` 的调用
- **新增考核时间列表 API 调用**：调用后端 `/api/v1/assessment-times` 接口获取用户的考核时间列表
- **新增考核进度 API 调用**：调用后端 `/api/v1/assessment-times/{id}/progress` 接口获取每个考核的答题进度
- **新增考核会话 API 调用**：对于限时考核，调用 `/api/v1/assessment-sessions/{assessmentTimeId}` 获取截止时间
- **数据格式转换**：将后端返回的 `AssessmentTimeDTO` 转换为前端 `Assessment` 类型
- **状态计算逻辑**：根据当前时间和考核时间计算考核状态（未开始/进行中/已结束）

## Capabilities

### New Capabilities

- `profile-assessment-api`: 个人主页考核数据获取能力，包括考核时间列表查询、进度查询、会话管理
- `assessment-status-calculation`: 考核状态计算能力，根据时间范围和答题进度计算考核状态

### Modified Capabilities

<!-- 无修改的现有能力 -->

## Impact

- **前端代码**：
  - `src/frontend/src/app/(public)/(other)/profile/page.tsx` - 修改数据加载逻辑
  - `src/frontend/src/types/profile.ts` - 可能需要调整 Assessment 类型定义
  - `src/frontend/src/apis/services/assessment-time.service.ts` - 已有服务，确认接口完整性
  - `src/frontend/src/mocks/services/profile.service.ts` - 移除考核相关的 mock 服务

- **后端 API 依赖**：
  - `GET /api/v1/assessment-times` - 考核时间列表（已实现）
  - `GET /api/v1/assessment-times/{id}/progress` - 考核进度（已实现）
  - `GET /api/v1/assessment-sessions/{assessmentTimeId}` - 考核会话（已实现）

- **用户体验**：
  - 考核数据从服务器实时获取，展示准确的进度信息
  - 支持限时考核的倒计时显示
  - 考核状态根据实际时间动态计算
