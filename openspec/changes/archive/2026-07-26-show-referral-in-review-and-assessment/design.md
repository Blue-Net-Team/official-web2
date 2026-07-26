# Design: show-referral-in-review-and-assessment

## Context

内推信息当前只存在于 `tb_enroll.internal_referral_code`（考生报名时填写的推荐码）。注意命名陷阱：`tb_user.internal_referral_code` 是**成员自己的推荐码**（用于分享），与 enroll 表同名字段含义不同——后者是"我被谁推荐"，前者是"我的码是什么"。推荐人反查 = 用 enroll 的码去匹配 user 的码。

三个需要改造的查询现状：

| 查询 | 主表 | 当前排序 | 推荐人现状 |
|---|---|---|---|
| `EnrollMapper.selectPageByConditions`（报名审核列表） | `tb_enroll` | `id DESC` | 不查（`toEntity(do, false)` 刻意避免 N+1） |
| `AssessmentJudgementMapper.selectQuestionSubmissions`（逐题列表） | `tb_assessment_answer` JOIN `tb_user` | 队伍 → 队长 → 学号 | 无 |
| `AssessmentJudgementMapper.selectCandidateScoreRows`（积分榜） | `tb_user`(CANDIDATE) | 队伍 → 队长 → 学号 | 无 |

决策工作台（`getDecisionWorkspace`）复用积分榜数据后在 Java 内存中按学号排序。

约束：enroll 表每届考核结束后手动清空，因此 join enroll 拿到的内推信息仅当届有效（已与需求方确认接受）。

## Goals / Non-Goals

**Goals:**

- 报名审核列表、逐题提交列表、考生积分榜、决策工作台四处均展示内推标识与推荐人姓名
- 四处内推考生排序靠前，且不打散现有队伍分组（组内置顶）
- 所有列表推荐人姓名一次 SQL 查出，无 N+1

**Non-Goals:**

- 不做盲评（改卷界面可见内推信息）
- 不做内推转化率/留存统计
- 不新增 `tb_user.referred_by` 持久化列（不改造审批链路、不做数据库结构变更）
- 不做"只看内推"筛选器
- 不改动 `EnrollRepositoryImpl` 详情页的现有逐条反查逻辑（单条查询无 N+1 问题）

## Decisions

### D1: 内推数据源 join `tb_enroll`，而非固化到 `tb_user`

两个候选方案：

- **方案甲（采用）**：查询时 `LEFT JOIN tb_enroll e ON e.student_id = u.student_id`。改动局限于 SQL 与 DTO 透传，无建列、无审批链路改造。
- 方案乙：审批通过时将 `referred_by` 写入 `tb_user` 新列。数据持久、可做历史统计，但需数据库迁移 + 审批链路改造，且需求方明确不需要历史统计。

取舍：方案乙的唯一收益是历史数据与统计，需求方已明确不需要；enroll 每届清空导致历史不可见是可接受的。

### D2: 排序规则——组内置顶，不打散队伍

评分相关列表当前按队伍分组展示（队伍 → 队长 → 学号）。内推全局置顶会打散队伍（候选方案 A，否决）。采用**组内置顶**：

```
ORDER BY t.id NULLS LAST,                                    -- 队伍顺序不变，独立考生最后
         CASE WHEN ru.id IS NOT NULL THEN 0 ELSE 1 END,      -- 组内内推优先（码有效才算）
         CASE WHEN t.leader_id = u.id THEN 0 ELSE 1 END,     -- 队长
         u.student_id ASC                                    -- 学号
```

- **"内推"的判定口径：内推码匹配到真实成员（`ru.id IS NOT NULL`）才算内推**。仅填了码但码无效的视为非内推——不显示标记、不参与置顶（否则会出现"没标记却排在前面"的矛盾）
- 独立考生组（无队伍）同样适用：组内内推优先于学号
- 决策工作台为 Java 内存排序：`isReferred()` 基于 `referralUserName` 非空判定，再按学号
- 报名审核列表无队伍概念：`ORDER BY (ru.id 非空) , e.id DESC`

### D3: 报名列表推荐人用 SQL 投影，Mapper 返回 query DO

`selectPageByConditions` 目前返回 `EnrollDO` 再由 `toEntity(do, false)` 转换。逐条反查推荐人是 N+1（`EnrollRepositoryImpl.getReferralUserName` 每行两次 select）。改为：

```sql
SELECT e.*, ru.username AS referral_username
FROM tb_enroll e
LEFT JOIN tb_user ru ON ru.internal_referral_code = e.internal_referral_code
```

新增 `EnrollBriefQueryDO`（含 `referralUsername`），Mapper 直接返回投影——这是层间约定允许的"SQL 层投影"例外，需在 Mapper 方法注释中说明。`EnrollRepositoryImpl` 将投影传入 converter，brief 链路透出 `internalReferralCode` + `referralUserName`。

### D4: 考核两条 SQL 的 join 方式

`selectQuestionSubmissions` 与 `selectCandidateScoreRows` 均已 join `tb_user u`（考生），在其上追加：

```sql
LEFT JOIN tb_enroll e ON e.student_id = u.student_id
LEFT JOIN tb_user ru ON ru.internal_referral_code = e.internal_referral_code
```

- `tb_enroll` 与 `tb_user` 无物理外键，按 `student_id` 应用层关联（符合项目"不使用物理外键"约束）
- 学号在当届 enroll 中唯一（业务假设：每届清空 + 报名时 `countByStudentId` 查重），LEFT JOIN 不会放大行数
- 别名字段：`e.internal_referral_code AS referral_code`、`ru.username AS referral_username`，避免与 `u.internal_referral_code`（考生自己的码）混淆

### D5: 字段命名与空值语义

DTO 统一复用报名详情已有的字段名：`internalReferralCode`（考生填的码，原样返回）、`referralUserName`（推荐人姓名，码无效或缺失时为 null）。前端展示规则：

- `referralUserName` 非空 → 显示"xxx 内推"标识
- `referralUserName` 为空（无码或无效码）→ 不显示任何内推标识，排序按非内推处理

## Risks / Trade-offs

- [enroll 清空后历史考核回看失去内推信息] → 已确认接受；如未来需要，再立项做 `tb_user.referred_by` 固化
- [同名不同义字段（`tb_user.internal_referral_code` vs `tb_enroll.internal_referral_code`）在 SQL 中混淆] → 统一使用别名 `referral_code`/`referral_username` 投影，join 条件以 enroll 侧为准；集成测试覆盖
- [join 放大行数导致积分榜聚合错乱] → 学号唯一性假设 + 集成测试断言每个考生行数不变
- [组内置顶改变评委习惯的列表顺序] → 排序规则在 spec 中固化，前端内推标识醒目以便解释顺序变化
- [决策工作台与积分榜排序规则不一致] → 决策工作台按"内推优先 → 学号"，本就是平铺列表无队伍分组，规则差异是有意的

## Migration Plan

无数据库结构变更、无接口路径变更、无权限变更。DTO 仅新增可空字段，旧前端忽略新字段，可平滑发布。回滚 = 回滚版本。
