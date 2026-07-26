# Tasks: show-referral-in-review-and-assessment

## 1. 报名审核列表（后端）

- [x] 1.1 新增 `EnrollBriefQueryDO`（含 EnrollDO 全部字段 + `referralUsername`），`EnrollMapper.selectPageByConditions` 改为 LEFT JOIN `tb_user ru ON ru.internal_referral_code = tb_enroll.internal_referral_code` 返回投影（Mapper 方法注释说明 SQL 投影例外），ORDER BY 改为 `(internal_referral_code 非空) DESC, id DESC`
- [x] 1.2 `EnrollRepositoryImpl` 适配投影 DO → `Enroll` 聚合（携带 referralUserName），`EnrollResult.Brief` 与 `EnrollResponseConverter` 增加 `internalReferralCode`、`referralUserName`
- [x] 1.3 `EnrollmentBriefDTO` 增加 `internalReferralCode`、`referralUserName` 字段

## 2. 报名审核列表（测试，TDD 先行可于 1.x 同步编写）

- [x] 2.1 Repository 集成测试：内推报名排在非内推之前，组内 id 倒序
- [x] 2.2 Repository 集成测试：列表项正确带出推荐人姓名；无效码/无码时 referralUserName 为 null
- [x] 2.3 集成测试断言列表查询无 N+1（查询条数固定）

## 3. 考核评判 SQL（后端）

- [x] 3.1 `AssessmentQuestionSubmissionQueryDO`、`AssessmentCandidateScoreQueryDO` 增加 `referralCode`、`referralUsername` 字段
- [x] 3.2 `selectQuestionSubmissions` 增加 `LEFT JOIN tb_enroll e ON e.student_id = u.student_id` + `LEFT JOIN tb_user ru ON ru.internal_referral_code = e.internal_referral_code`，投影别名字段，ORDER BY 调整为 队伍 → 内推优先 → 队长 → 学号
- [x] 3.3 `selectCandidateScoreRows` 同样增加两表 JOIN 与投影，ORDER BY 同样调整

## 4. 考核评判链路透传（后端）

- [x] 4.1 领域/应用层聚合（`AssessmentCandidateScoreboard`、`AssessmentQuestionSubmission` 等）与 Result 增加内推字段透传
- [x] 4.2 `AssessmentCandidateScoreboardDTO`、`AssessmentQuestionSubmissionDTO`、`AssessmentDecisionCandidateDTO` 增加 `internalReferralCode`、`referralUserName`，ResponseConverter 同步
- [x] 4.3 `AssessmentJudgementAppServiceImpl.getDecisionWorkspace` 排序改为 内推优先 → 学号

## 5. 考核评判（测试）

- [x] 5.1 Mapper/Repository 集成测试：逐题列表与积分榜组内排序（内推 > 队长 > 学号）、队伍顺序不变、独立考生组内推优先
- [x] 5.2 集成测试：无 enroll 记录的考生字段为 null 且按非内推排序；无效码考生按内推排序但 referralUserName 为 null
- [x] 5.3 应用服务单元测试：决策工作台 comparator 内推优先 → 学号

## 6. 前端

- [x] 6.1 `apis/schema/type.ts` 同步四处 DTO 新字段
- [x] 6.2 `EnrollmentCard` 增加"内推"Tag 与"xxx 内推"展示（referralUserName 为空时仅显示 Tag）
- [x] 6.3 judge/score 逐题视图候选人列增加内推标记与推荐人姓名
- [x] 6.4 judge/score 积分榜视图候选人列增加内推标记与推荐人姓名
- [x] 6.5 judge/decision 决策工作台候选人列增加内推标记与推荐人姓名

## 7. 端到端验证

- [x] 7.1 后端编译打包、重建 `bluenet-api-service:latest` 镜像并重启容器
- [x] 7.2 检查 3000 端口占用，复用或启动前端服务
- [x] 7.3 Playwright 验证：报名审核列表内推置顶与推荐人展示；逐题/积分榜/决策页内推标记与组内置顶顺序
