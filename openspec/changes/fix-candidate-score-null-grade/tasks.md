## 1. SQL 修复

- [ ] 1.1 修改 `AssessmentJudgementMapper.xml` 中 `selectCandidateScoreRows` 的 `candidate_users` CTE，将 `direction` 和 `grade` 的硬等号条件改为支持 null 的 `IS NULL OR =` 模式

## 2. 测试补充

- [ ] 2.1 新增 `AssessmentJudgementMapperIntegrationTest`：创建 direction=COMPUTER_VISION、grade=null 的考核时间，插入考生、题目、作答和评分记录，验证 `selectCandidateScoreRows` 返回非空结果
- [ ] 2.2 补充 `AssessmentJudgementAppServiceImplTest`：添加 `listCandidateScoreboard` 在 `grade=null` 场景下的单元测试用例

## 3. 验证

- [ ] 3.1 运行后端单元测试和集成测试，确保全部通过
- [ ] 3.2 编译打包后端产物
- [ ] 3.3 构建 Docker 镜像并启动容器
- [ ] 3.4 使用 Playwright 验证 `/admin/assessment/judge/score` 页面：题目视图和人员视图均正常显示考生
