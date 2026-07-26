# Proposal: show-referral-in-review-and-assessment

## Why

内推功能上线后，考生报名时可填写成员的内推码，但管理端只有报名详情抽屉能看到推荐人：报名审核列表没有内推标识，考核评分（逐题列表、考生积分榜）和录取决策工作台完全看不到内推信息。运营希望内推考生在报名审核和考核各环节都能被识别并排序靠前，以便重点关注。

## What Changes

- **报名审核列表**：内推考生排序靠前（`internal_referral_code` 非空优先，其余仍按 id 倒序）；列表项展示"内推"标识与推荐人姓名；推荐人姓名通过 SQL JOIN 一次查出，不做逐条反查（避免 N+1）
- **考核逐题提交列表**（judge/score 逐题视图）：展示内推标识与推荐人姓名；分组内排序规则调整为 内推优先 → 队长 → 学号
- **考生积分榜**（judge/score 积分榜视图）：同上，展示内推标识与推荐人姓名；分组内 内推优先 → 队长 → 学号
- **录取决策工作台**（judge/decision）：候选人列表展示内推标识与推荐人姓名；排序调整为 内推优先 → 学号
- 排序调整均不打散现有队伍分组：队伍之间的相对顺序不变，独立考生组仍排在所有队伍之后，内推优先仅作用于每个分组内部

明确不做：

- 不做盲评（改卷页面同样显示内推信息）
- 不做内推通过率/留存率统计
- 不将内推关系固化到 `tb_user`（enroll 表每届清空后历史不可见，接受该取舍）
- 不做"只看内推"筛选器（后续可作为独立小改动）

## Capabilities

### New Capabilities

- `assessment-referral-visibility`: 考核评判各环节（逐题提交列表、考生积分榜、录取决策工作台）展示考生内推标识与推荐人姓名，并在分组内将内推考生排序靠前

### Modified Capabilities

- `backend-enrollment-api`: 报名列表接口返回内推码与推荐人姓名（当前仅详情返回），且列表排序内推考生优先

## Impact

**后端**：

- `EnrollMapper.xml selectPageByConditions`：新增 LEFT JOIN `tb_user`（推荐人反查）、投影推荐人姓名、调整 ORDER BY；Mapper 返回投影 query DO（层间约定的性能投影例外，方法注释说明）
- `AssessmentJudgementMapper.xml selectQuestionSubmissions` / `selectCandidateScoreRows`：新增 LEFT JOIN `tb_enroll`（按 student_id）与 `tb_user`（推荐人反查），投影内推码与推荐人姓名，调整 ORDER BY
- Query DO（`AssessmentQuestionSubmissionQueryDO`、`AssessmentCandidateScoreQueryDO`）、聚合/Result、`AssessmentCandidateScoreboardDTO`、`AssessmentQuestionSubmissionDTO`、`AssessmentDecisionCandidateDTO`、`EnrollmentBriefDTO` 增加内推字段
- `AssessmentJudgementAppServiceImpl.getDecisionWorkspace`：排序 comparator 增加内推优先
- 无新增接口、无新增权限标识、无数据库结构变更

**前端**：

- `EnrollmentCard`：内推 Tag + 推荐人姓名
- `admin/assessment/judge/score` 逐题视图与积分榜视图：候选人列内推标记
- `admin/assessment/judge/decision`：候选人列内推标记
- `apis/schema/type.ts` 类型同步

**边界约定**：

- 通过 WPS 表单或后台直接创建、无 enroll 记录的考生：无内推标识
- 内推码无效（匹配不到成员）：不视为内推——不显示标识、不参与置顶排序
- 每届考核后 enroll 表清空，内推信息仅当届可见
