## Context

考核系统已有考题管理（CRUD）和考题目录展示功能。当前考生能看到题目列表和作答状态，但无法进入答题页面作答。

现有后端代码中，`uploadAssessmentWork` 接口需要 `answerId` 参数（先有答案再上传文件），这与"先上传文件再创建答案"的合理流程相悖。答案实体（`AssessmentAnswer`）和仓储层（`AssessmentAnswerRepository`）已有基础结构，但缺少 Controller 层 API 暴露。

前端仅有考题目录页（`/assessment/[timeId]/questions`），需要新增答题页（`/assessment/[timeId]/questions/[questionId]`）。

## Goals / Non-Goals

**Goals:**
- 实现文件上传题的完整答题流程：查看题目 → 上传文件 → 提交答案
- 支持"先上传文件，再创建答案"的流程
- 支持限时考核的倒计时功能
- 支持重新进入答题页时恢复已上传状态
- 考题目录页题目行可点击跳转到答题页

**Non-Goals:**
- 单选题、多选题、算法题的答题页面（后续迭代）
- 孤儿文件清理机制
- 答案评分功能
- 批量提交/导出答案

## Decisions

### D1: 文件上传流程改为先上传后创建答案

**选择**：`POST /file/upload/assessment/work` 接收 `questionId`（而非 `answerId`），上传成功返回 `fileId`，然后前端调用 `POST /assessment-answers` 传入 `questionId + fileId` 创建答案记录。

**理由**：
- 用户操作流程更自然：选文件 → 上传 → 确认提交
- 前端可以在上传成功后预览，用户确认后才真正创建答案
- 减少空答案记录的产生

**替代方案**：先创建空答案再上传文件（当前代码暗示的流程）
- 缺点：上传失败会留下空答案记录；前端需要两步才能完成提交

### D2: 答案创建与文件关联在同一事务中

**选择**：`AssessmentAnswerService.createAnswer()` 在一个事务中完成：校验权限 → 创建 answer 记录（含 fileId） → 设置 submitTime。

**理由**：answer 创建本身很轻量，不需要拆分事务。fileId 在创建时直接写入 answer。

### D3: 前端路由使用 [questionId] 动态路由

**选择**：答题页路由为 `/assessment/[timeId]/questions/[questionId]`

**理由**：与 Next.js App Router 的文件系统路由匹配，URL 语义清晰，支持直接链接分享。

### D4: 限时考核使用「考核会话」机制

**选择**：新建 `tb_assessment_session` 表记录用户首次进入考核的时间，后端计算 deadline 并返回给前端。前端根据 deadline 倒计时。

**计时规则**：
- 用户首次访问考题列表时，后端创建 session 记录
- `deadline = min(session.startTime + timeLimitMinutes, assessmentTime.endTime)`
- 即使用户提前进入，最晚也不超过考核结束时间
- 示例：限时90分钟，考核1:00-3:00，用户1:10进入 → deadline=2:40；用户2:30进入 → deadline=3:00

**Session 表结构**：
```
tb_assessment_session
  id               BIGINT PK AUTO
  user_id          BIGINT NOT NULL
  assessment_time_id BIGINT NOT NULL
  start_time       TIMESTAMP NOT NULL  -- 首次查看时间
  deadline         TIMESTAMP NOT NULL  -- min(start+limit, endTime)
  UNIQUE(user_id, assessment_time_id)
```

**理由**：
- 计时起点是用户首次查看考题列表的时刻，不是全局 startTime
- deadline 由后端计算保证准确性，避免前端时钟偏差
- 前端只需拿到 deadline 做倒计时展示，逻辑简单可靠

**替代方案**：纯前端计算（当前实现）
- 缺点：无法追踪用户首次进入时间，刷新页面后计时不准确

### D5: 答题页为客户端组件

**选择**：答题页使用 `'use client'` 客户端组件。

**理由**：答题页有大量交互（文件拖拽上传、倒计时、表单状态），客户端组件更合适。这与考题目录页的模式一致。

### D6: 双栏布局与题型扩展性设计

**选择**：答题页采用双栏布局（左栏主内容 + 右栏 320px 侧栏），页面结构分为三层实现题型扩展性：

1. **共享层**（所有题型共用）：Header + 题目要求卡片 + 附件下载卡片
2. **题型专属层**（按 `questionType` 条件渲染）：文件上传卡片 / "正在开发"占位符
3. **侧栏层**（所有题型共用）：倒计时/时间范围 + 答题信息 + 已上传文件 + 提交 + 导航

新增题型时只需在题型专属层添加对应条件分支，无需改动共享层和侧栏层。

**理由**：
- 考核系统支持 4 种题型（单选、多选、文件上传、算法），当前仅实现文件上传题
- 共享层/侧栏层与题型无关的信息（描述、附件、倒计时、导航）无需随题型变化
- 占位符让考生知道该题型存在但尚未开放，避免空白页面的困惑

**替代方案**：每个题型独立实现完整页面
- 缺点：大量重复代码（Header、侧栏、导航逻辑等），维护成本高

## Risks / Trade-offs

- **[孤儿文件]** 用户上传文件后未提交答案会产生孤立文件 → 暂不处理，影响可控
- **[重复提交]** 用户可能多次上传文件并提交 → 创建答案时检查 `existsByUserIdAndQuestionId`，已存在则拒绝
- **[限时准确性]** 前端倒计时可能与服务器时间有偏差 → deadline 由后端计算并返回 ISO 时间戳，前端用 `deadline - Date.now()` 倒计时；同时后端在创建答案时校验 deadline 是否已过
- **[超时提交一致性]** 前端自动提交可能与后端 deadline 校验冲突 → 后端是最终裁判，超时后的提交请求会被拒绝（返回错误码），前端仅做 UI 展示
