## 1. 修复淘汰限制 grade 匹配逻辑

- [x] 1.1 修改 `AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade`：全局考核场景下 `eliminatedTime.grade == null` 时返回 `true`
- [x] 1.2 修改 `AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade`：普通场景下 `eliminatedTime.grade == null || targetTime.grade == null` 时返回 `true`
- [x] 1.3 修复 `isEliminatedFromPriorEpoch_directionEliminated_targetGlobalDifferentGrade_shouldReturnFalse` 测试用例（该用例期望本身正确，但需确认修复后仍通过）
- [x] 1.4 新增测试：`eliminated.grade=null` 限制同方向后续 `grade!=null` 的考核
- [x] 1.5 新增测试：`eliminated.grade=null` 限制全局考核 `grade!=null` 的考核
- [x] 1.6 运行 `AssessmentDecisionDomainServiceImplTest` 确认全部通过

## 2. 增加同方向同轮次 grade 互斥校验

- [x] 2.1 在 `AssessmentTimeAppServiceImpl.createAssessmentTime` 中增加互斥校验：查询同方向同轮次已有记录，检查 grade 形式冲突
- [x] 2.2 在 `AssessmentTimeAppServiceImpl.updateAssessmentTime` 中增加互斥校验（排除自身 ID）
- [x] 2.3 在 `AssessmentTimeRepository` / `AssessmentTimeMapper` 中新增 `countConflictingGradeByDirectionAndEpoch` 查询方法
- [x] 2.4 运行 `AssessmentTimeAppServiceImplTest` 确认全部通过

## 3. 集成测试与验证

- [x] 3.1 编译后端：`./mvnw clean compile`
- [x] 3.2 运行全部单元测试和集成测试
- [x] 3.3 打包并构建 Docker 镜像验证
