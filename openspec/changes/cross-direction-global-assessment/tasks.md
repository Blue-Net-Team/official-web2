## 1. 数据库迁移

- [x] 1.1 创建迁移文件 `V15__allow_null_grade_for_global_assessment.sql`，`ALTER TABLE tb_assessment_time ALTER COLUMN grade DROP NOT NULL`

## 2. 后端 DTO 与命令对象

- [x] 2.1 `CreateAssessmentTimeRequestDTO.java`：移除 `direction` 的 `@NotNull`，移除 `grade` 的 `@NotNull`、`@Min`、`@Max`
- [x] 2.2 `AssessmentTimeCommands.java`：确认 `CreateAssessmentTimeCommand` 和 `UpdateAssessmentTimeCommand` 的 direction/grade 类型支持 null（无强制改动）

## 3. Mapper/Repository 层

- [x] 3.1 `AssessmentTimeMapper.java`：新增 `countByEpochGrade(epoch, grade)` 方法
- [x] 3.2 `AssessmentTimeMapper.xml`：实现 `countByEpochGrade` SQL（WHERE direction IS NULL AND epoch = #{epoch} AND grade 动态条件）
- [x] 3.3 `AssessmentTimeMapper.xml`：修改 `selectPageByUserParticipation`，增加 `OR t.grade IS NULL`
- [x] 3.4 `AssessmentTimeMapper.xml`：修改 `selectMaxEpoch` 为动态 WHERE 条件，支持 direction/grade 为 null
- [x] 3.5 `AssessmentTimeRepository.java`：新增 `countByEpochGrade`、`findMaxEpoch` 等接口方法
- [x] 3.6 `AssessmentTimeRepositoryImpl.java`：实现新增的 Repository 接口方法

## 4. 考核时间创建/管理（应用层）

- [x] 4.1 `AssessmentTimeAppServiceImpl.validateDirectionPermission`：处理 `targetDirection == null`，仅 SUPER_ADMIN 可创建全局考核
- [x] 4.2 `AssessmentTimeAppServiceImpl.createAssessmentTime`：当 direction 为 null 时调用 `countByEpochGrade` 校验唯一性
- [x] 4.3 `AssessmentTimeAppServiceImpl.updateAssessmentTime`：同 4.2，处理 direction 为 null 时的唯一性校验
- [x] 4.4 `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser`：确认考生查询时全局考核被正确返回（Mapper 已改则无需额外改动）

## 5. 考题访问控制

- [x] 5.1 `AssessmentQuestionAppServiceImpl.listQuestionsForUser`：方向校验改为 `if (time.getDirection() != null && ...)`
- [x] 5.2 `AssessmentQuestionAppServiceImpl.getQuestionDetailForUser`：同上修改

## 6. 评审权限控制

- [x] 6.1 `AssessmentJudgementAccessGuard.assertAssessmentTimeScope`：增加 `assessmentTime.getDirection() != null` 条件，全局考核跳过方向校验

## 7. 最终轮次判定与结果发布

- [x] 7.1 `AssessmentJudgementAppServiceImpl.publishDecisions`：处理 `direction == null` 时，调用 `findMaxEpoch` 新方法判定最终轮次
- [x] 7.2 `AssessmentJudgementAppServiceImpl.publishDecisions`：处理 `direction == null` 时邮件方向标签显示"全局"

## 8. 前端

- [x] 8.1 `AssessmentTimeDrawer.tsx`：方向选择器对 SUPER_ADMIN 增加"全局"选项（value=GLOBAL，提交转 null）
- [x] 8.2 `AssessmentTimeDrawer.tsx`：年级字段支持 null（SUPER_ADMIN 选择"全局"方向时，年级自动置为"不限年级"）
- [x] 8.3 `page.tsx`：表格方向列支持 null 时显示"全局"标签
- [x] 8.4 `page.tsx`：表格年级列支持 null 时显示"不限"标签
- [x] 8.5 `AssessmentTimeDrawer.tsx` + `page.tsx`：DIRECTION_ADMIN 不显示"全局"选项，且不可编辑全局考核记录
- [x] 8.6 确认前端类型定义 `assessment.dto.ts` 中 direction/grade 类型是否需调整

## 9. 验收

- [x] 9.1 运行现有后端测试，确认无回归（973 tests, 0 failures ✓）
- [ ] 9.2 创建全局考核（SUPER_ADMIN），验证创建成功且 direction/grade 为 null
- [ ] 9.3 DIRECTION_ADMIN 尝试创建全局考核，验证被 403 拒绝
- [ ] 9.4 考生查看考核列表，验证全局考核可见
- [ ] 9.5 不同方向考生在全局考核中查看题目、提交答案，验证正常
- [ ] 9.6 跨方向组队，验证正常
- [ ] 9.7 评审全局考核，验证 DIRECTION_ADMIN 可访问
- [ ] 9.8 发布全局考核结果，验证邮件发送和文案正确
- [x] 9.9 编译打包后端，验证构建成功（`mvnw.cmd compile` + `mvnw.cmd test` ✓）
