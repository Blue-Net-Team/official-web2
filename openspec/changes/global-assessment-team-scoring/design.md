## Context

当前系统已实现全局考核（`direction=null, grade=null`）的创建与答题，但评分和录用决策链路未打通。前端 `/admin/assessment/judge/score` 和 `/admin/assessment/judge/decision` 页面的方向选择器仅枚举 `DIRECTION_LABELS`，不包含"全局"选项，导致全局考核时间无法被加载。

更深层的问题是组队基础逻辑不完整。组队仅支持 `FILE_UPLOAD` 题，队长提交答案后 `tb_assessment_answer` 中只存储队长的记录。所有评分查询 SQL（`selectQuestionSubmissions`、`selectCandidateScoreRows`、`selectQuestionScoreboard`）均按 `user_id` JOIN answer，完全不认识 `team_id`，导致组员在评分和录用链路中完全不可见。

此外，组队生命周期管理缺失：队长提交答案后队员仍可自由退队；解散队伍后不清理 answer/judgement 记录；队员已有队伍答案后仍可加入其他队伍。

## Goals / Non-Goals

**Goals:**
- 评分和录用决策页面支持选择全局考核（`direction=null`）
- 队长提交 FILE_UPLOAD 答案后，自动为所有组员创建 answer 记录
- 评分查询支持通过 `team_id` 展开组员数据
- 队伍提交答案后进入锁定状态（禁止退出、转让、解散）
- 解散队伍时级联清理关联的 answer 和 judgement 记录
- 加入队伍时检查是否已有队伍答案，避免重复继承
- 前端组队界面展示规则声明

**Non-Goals:**
- 不改动客观题（单选/多选/算法）的独立作答逻辑
- 不新增 `tb_assessment_answer` 表字段，复用现有 `team_id`
- 不改动已有全局考核的答题和淘汰逻辑（`cross-direction-global-assessment` 已覆盖）
- 不引入实时协作编辑等高级组队功能
- 不修改 OSS 文件存储策略（孤儿文件由现有定时任务清理）

## Decisions

### 1. 提交时自动为组员创建 answer 记录

**选择**：队长提交/更新答案时，遍历所有组员，为每人创建一条独立的 `tb_assessment_answer` 记录，字段复制队长（`content`, `file_id`, `language`, `submit_time`, `team_id`）。

**理由**：
- 保持数据模型一致性，每个考生有自己的 answer + judgement 记录
- 现有评分查询 SQL 只需微调查询条件即可支持，无需大幅重写 JOIN 逻辑
- 天然支持"独立评分"业务需求（组员可单独调整分数）

**替代方案**：查询时 SQL 展开（LEFT JOIN `tb_assessment_team_member`）。放弃原因：所有查询都要改，且 `judgement.user_id` 归属复杂（共享 `answer_id` 时 `user_id` 该写谁？）。

### 2. 答案同步策略：写时复制

**选择**：队长提交时复制给组员；队长更新时同步更新所有组员记录。

**理由**：读查询简单，无需每次 JOIN team_member。代价是写时多点开销，但文件上传题提交频率低。

**风险**：若队伍很大（如 10 人），批量插入 10 条 answer 记录是可控的。

### 3. 队伍锁定时机：队长首次提交 FILE_UPLOAD 答案时

**选择**：当队长为任意 FILE_UPLOAD 题创建答案时，队伍进入锁定状态。

**理由**：FILE_UPLOAD 是唯一组队题型，队长提交即意味着"队伍作品已确定"。

**锁定范围**：禁止 `leaveTeam`（队员）、`transferLeader`（队长）、`disbandTeam`（队长）。队员仍可加入（若之前未加入过）。

### 4. 解散清理策略：级联删除

**选择**：`disbandTeam()` 中，删除该队伍所有成员的 answer 记录（包括队长），以及关联的 judgement 记录。

**理由**：解散意味着"队伍不存在，作品作废"。若保留 answer，则成为悬空数据。

**文件处理**：answer 关联的 `file_id` 指向 OSS 文件。删除 answer 后文件可能成为孤儿，由现有 `OrphanFileCleanupJob` 定时清理。

### 5. 跨队伍限制：已有队伍答案者禁止加入新队伍

**选择**：`joinTeam()` 中，检查该用户在该考核时间是否已有 `team_id != null` 的 answer 记录。若有，禁止加入。

**理由**：避免一个考生同时属于多个队伍，导致答案继承混乱。

**边界**：若用户已退队（队伍仍在），其 answer 记录仍保留（直到队伍解散或新队长更新）。此时该用户能否加入新队伍？→ 禁止，因为已有队伍答案记录。

### 6. 前端"全局"选项的值表示

**选择**：沿用 `cross-direction-global-assessment` 的设计——前端 Select 中 `"GLOBAL"` 作为特殊标记，提交/查询时转换为 `null`。

**理由**：与已有 change 保持一致，Ant Design Select 的 `value` 不支持 `null`。

## Risks / Trade-offs

- [数据一致性] 批量创建组员 answer 时若部分失败，可能导致部分组员有记录、部分没有。→ 使用事务包裹批量插入，失败时整体回滚。
- [并发] 队长同时提交和队员退队可能产生竞态。→ 队伍锁定在事务中完成，获取队伍状态后加锁。
- [性能] 全局考核的 `selectCandidateScoreRows` 需查询所有方向考生，数据量大。→ 已有 `keyword` 过滤和分页，全局考核预期人数可控（数百人）。
- [兼容性] 已存在的队伍数据（无 answer 或已有 answer）如何处理？→ 本 change 只影响新提交，历史数据不受影响。历史队伍的 answer 记录保持现状。

## Migration Plan

1. 发布后端代码（自动创建组员 answer、锁定、清理逻辑）
2. 发布前端代码（全局选项、组队声明）
3. 无需数据库迁移（复用现有 `team_id` 字段）
4. 无需数据回迁
