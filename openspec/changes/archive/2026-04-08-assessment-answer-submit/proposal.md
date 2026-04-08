## Why

考核系统目前只有考题目录展示功能，缺少答题页面和答案提交流程。考生无法在线作答，管理员无法收集和评审答案。需要实现答题页面，优先支持文件上传题型的完整答题流程。

## What Changes

- **修改**文件上传接口：`uploadAssessmentWork` 参数从 `answerId` 改为 `questionId`，先上传文件获得 fileId，再创建答案关联文件
- **新增**答案创建 API：`POST /api/v1/assessment-answers`，接收 questionId 和 fileId 创建答案记录
- **新增**答案查询 API：`GET /api/v1/assessment-answers?questionId=X`，用户重新进入时恢复答题状态
- **新增**题目详情 API：`GET /api/v1/assessment-questions/{id}`，用户端返回题目 content（含描述文本）
- **新增**前端答题页面路由 `/assessment/[timeId]/questions/[questionId]`
- **新增**前端文件上传组件、倒计时轮盘组件、答题状态管理
- **新增**前端答案 API 服务（assessmentAnswerService）和文件上传服务扩展

## Capabilities

### New Capabilities
- `assessment-answer-api`: 答案创建、查询的后端 API 完整链路（Controller → Service → DomainService → Repository）
- `assessment-question-detail-api`: 用户端题目详情查询接口，返回 content 供答题页展示
- `assessment-answer-page`: 前端答题页面，支持文件上传题型的作答流程，含倒计时轮盘

### Modified Capabilities
- `backend-file-upload-handler`: 上传作品接口参数从 answerId 改为 questionId，权限校验逻辑相应调整

## Impact

- **后端 API**：新增 3 个接口，修改 1 个接口参数
- **后端代码**：修改 FileUploadController、FileService、FileServiceImpl 共 3 个文件；新增 AssessmentAnswerController、AssessmentAnswerService、AssessmentAnswerServiceImpl、CreateAnswerRequestDTO、AssessmentAnswerDTO 共 5 个文件
- **前端页面**：新增 `[questionId]` 动态路由页面 + 样式文件
- **前端服务**：新增 assessmentAnswerService，扩展 fileService
- **前端组件**：新增文件上传组件、倒计时轮盘组件
- **前端状态**：可能需要新增 assessment store 管理答题状态
