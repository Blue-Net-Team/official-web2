## 1. 后端 - 扩展 DTO 与 Converter

- [x] 1.1 在 `AssessmentTimeDTO` 中新增 `totalQuestions`（Integer）和 `completedQuestions`（Integer）字段，添加 `@Schema` 注解
- [x] 1.2 在 `AssessmentTimeConverter` 中添加新字段的映射逻辑（需要 totalQuestions 和 completedQuestions 参数）

## 2. 后端 - Repository 层新增计数方法

- [x] 2.1 在 `AssessmentQuestionRepository` 接口新增 `int countByAssessmentTimeId(Long assessmentTimeId)` 方法
- [x] 2.2 在 `AssessmentQuestionRepositoryImpl` 中使用 `LambdaQueryWrapper` 实现题目计数
- [x] 2.3 在 `AssessmentAnswerRepository` 接口新增 `int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId)` 方法
- [x] 2.4 在 `AssessmentAnswerRepositoryImpl` 中通过 JOIN 查询实现用户已完成题目计数（answer JOIN question WHERE question.assessment_time_id = ? AND answer.user_id = ?）

## 3. 后端 - Service 层填充进度数据

- [x] 3.1 在 `AssessmentTimeServiceImpl.listAssessmentTimesForUser()` 中，查询到 AssessmentTimeVO 列表后，为每个 VO 补充 totalQuestions 和 completedQuestions 数据
- [x] 3.2 确保现有 `listAssessmentTimes()`（管理端）不受影响，仅用户端接口填充进度

## 4. 后端 - 新增进度查询接口

- [x] 4.1 创建 `AssessmentProgressDTO`（字段：assessmentTimeId, totalQuestions, completedQuestions）
- [x] 4.2 在 `AssessmentTimeService` 接口新增 `AssessmentProgressDTO getAssessmentProgress(Long assessmentTimeId)` 方法
- [x] 4.3 在 `AssessmentTimeServiceImpl` 中实现进度查询逻辑
- [x] 4.4 在 `AssessmentTimeController` 中新增 `GET /api/v1/assessment-times/{id}/progress` 端点，使用 `@RequiresPermission(access = AccessLevel.AUTHENTICATED)`

## 5. 前端 - 类型定义与 API 服务

- [x] 5.1 创建 `src/types/assessment.ts`，定义 `AssessmentTimeDTO`（id, direction, epoch, grade, startTime, endTime, timeLimit, timeLimitMinutes, totalQuestions, completedQuestions）和 `AssessmentProgressDTO` 类型，以及 `AssessmentStatus` 类型
- [x] 5.2 创建 `src/apis/services/assessment-time.service.ts`，实现 `getAssessmentTimes()` 和 `getAssessmentProgress(id)` 方法

## 6. 前端 - 考核时间列表页面

- [x] 6.1 创建 `src/app/(public)/(other)/assessment/page.tsx` 客户端组件，实现页面结构（标题、方向标签、卡片列表）
- [x] 6.2 实现认证检查逻辑：未登录时重定向到 `/login`
- [x] 6.3 实现考核状态计算函数：根据 startTime/endTime 与当前时间对比返回 'not-started' | 'in-progress' | 'ended'
- [x] 6.4 实现考核时间卡片组件：包含图标、标题、副标题、状态徽章、时间信息、限时信息、进度条、操作按钮
- [x] 6.5 实现三种状态卡片的样式变体（蓝色进行中、灰色未开始、绿色已结束）
- [x] 6.6 实现空状态展示（无考核安排时）
- [x] 6.7 创建 `src/app/(public)/(other)/assessment/styles.module.css`，参照设计稿暗色主题样式
