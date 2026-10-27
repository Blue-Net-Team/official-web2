## 1. 测试先行（红灯）

- [x] 1.1 在 `JudgeLanguageLimitRepositoryImplIntegrationTest` 中新增用例：按 `question_id` 查询返回该题全量限制行，字段（语言、timeLimitMs、memoryLimitKb、outputLimitKb、confirmed、confirmedAt）与插入值一致
- [x] 1.2 在同一集成测试中新增用例：多选题语言时结果按 `language` 升序排列
- [x] 1.3 在同一集成测试中新增用例：题目无限制行时返回空列表（非 null）
- [x] 1.4 在 `JudgeProblemConfigAdminServiceImpl` 相关单测中新增用例：`findByQuestionId` 组装的 `JudgeProblemConfigResult` 携带 `languageLimits`；配置不存在时仍返回空 Optional
- [x] 1.5 在 `AdminJudgeProblemConfigControllerTest` / 集成测试中新增用例：`GET .../config` 响应体 `languageLimits` 字段存在、结构正确；无限制时为空数组而非缺失或 null
- [x] 1.6 运行上述测试确认全部失败（编译失败或断言失败），锁定功能边界

## 2. 基础设施层

- [x] 2.1 `JudgeLanguageLimitMapper` 新增 `selectByQuestionId(@Param("questionId") Long questionId)` 返回 `List<JudgeLanguageLimitDO>`，并在 `JudgeLanguageLimitMapper.xml` 中实现 `SELECT ... FROM tb_judge_language_limit WHERE question_id = #{questionId} ORDER BY language ASC`
- [x] 2.2 为 `selectByQuestionId` 补充 Javadoc，说明排序语义与按题目（非按 config）读取的原因
- [x] 2.3 `JudgeLanguageLimitRepository` 新增 `List<JudgeLanguageLimit> findByQuestionId(Long questionId)`，并在 `JudgeLanguageLimitRepositoryImpl` 中通过 DO → 领域实体转换实现（无行时返回空列表）
- [x] 2.4 运行第 1 组的仓储集成测试转绿

## 3. 应用层与接口层

- [x] 3.1 新增 `JudgeLanguageLimitResult`（语言、timeLimitMs、memoryLimitKb、outputLimitKb、confirmed、confirmedAt）记录类型
- [x] 3.2 `JudgeProblemConfigResult` 新增 `List<JudgeLanguageLimitResult> languageLimits` 字段
- [x] 3.3 `JudgeProblemConfigAdminServiceImpl.findByQuestionId` 在组装结果时附带 `judgeLanguageLimitRepository.findByQuestionId(questionId)` 的转换结果；`upsert` 返回路径复用同一组装逻辑
- [x] 3.4 新增 `JudgeLanguageLimitDTO`，`JudgeProblemConfigDTO` 新增 `List<JudgeLanguageLimitDTO> languageLimits`，并在 `JudgeProblemConfigResponseConverter` 中完成 Result → DTO 转换（null 安全）
- [x] 3.5 运行第 1 组的应用层与 Controller 测试转绿

## 4. 前端对接

- [x] 4.1 `src/frontend/src/apis/schema/assessment.dto.ts` 新增 `JudgeLanguageLimitDTO` 类型，并为 `JudgeProblemConfigDTO` 增加可选字段 `languageLimits?: JudgeLanguageLimitDTO[]`
- [x] 4.2 `QuestionDrawer` 的「资源限制」页签新增已确认限制表格：语言、时间(ms)、内存(KB)、输出(KB)、确认状态、确认时间；不在当前标准解语言集合中的限制行标记为历史遗留且不可选
- [x] 4.3 确认表单语言下拉选项改为 `standardSolutions` 的语言集合，默认值改为第一个语言；移除 `confirmLanguage: 'python'` 硬编码（含类型默认值与题型切换重置两处）
- [x] 4.4 实现语言切换回填：有已确认限制则填既有值，否则填 `suggestedTimeLimitMs` / `peakMemoryKb`，均缺失时回退 5000 / 262144 / 1024
- [x] 4.5 抽屉打开完成（现有 config 加载 effect）与 `refreshJudgeConfig` 完成后触发同一回填逻辑，保证确认成功后页签即时刷新
- [x] 4.6 前端类型检查通过（`tsc --noEmit` 或项目既有 lint 命令），确认无类型报错

## 5. 编译打包与端到端验证

- [x] 5.1 后端 `./mvnw clean compile package` 打包成功（Windows 用 `mvnw.cmd`）
- [x] 5.2 构建并运行 `bluenet-api-service:latest` 镜像，启动 compose 基础设施（`docker compose -p bluenet --profile infra up -d`）
- [x] 5.3 检查 3000 端口占用情况：已占用则复用现有前端服务，未占用才自行启动 `pnpm dev`
- [x] 5.4 Playwright 验证：对已有确认记录的算法题打开编辑抽屉 → 「资源限制」页签显示既有数值与「已确认」状态
- [x] 5.5 Playwright 验证：切换语言时表单回填已确认值或 benchmark 建议值；提交确认后页签即时更新且无需重开抽屉
- [x] 5.6 Playwright 验证：无任何确认记录的算法题，页签不报错、`languageLimits` 为空、确认表单默认语言为该题标准解第一个语言
- [x] 5.7 回归验证：候选人端该题仍可正常提交已确认语言，未确认语言仍被拒绝
