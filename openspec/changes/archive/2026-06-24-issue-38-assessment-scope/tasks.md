## 1. 领域模型测试先行

- [x] 1.1 为 `AssessmentScope` 编写单元测试：覆盖 `isGlobalFinal()`、`isDirectional()`、`matches(AssessmentScope)` 的正例与反例。
- [x] 1.2 为 `AssessmentTime` 新增行为方法编写测试：覆盖 `getScope()`、`matchesGrade(AssessmentTime)`、委托后的 `isGlobalFinalAssessment()`。

## 2. AssessmentTime 领域模型实现

- [x] 2.1 在 `AssessmentTime` 同一包下新增 `AssessmentScope` 值对象（Java `record`），封装 `(direction, epoch)` 语义。
- [x] 2.2 在 `AssessmentTime` 中新增 `getScope()` 方法，返回当前考核的 `AssessmentScope`。
- [x] 2.3 在 `AssessmentTime` 中新增 `matchesGrade(AssessmentTime other)` 方法，实现年级通配匹配规则。
- [x] 2.4 将 `isGlobalFinalAssessment()` 的实现委托给 `getScope().isGlobalFinal()`，保持既有行为。

## 3. 淘汰匹配逻辑重构

- [x] 3.1 重写 `AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade`，使用 `AssessmentScope` 和 `matchesGrade` 替代 null/魔术数字判断。
- [x] 3.2 重写 `AssessmentDecisionDomainServiceImpl.isPriorEpoch`，使用 `AssessmentScope.isValidDirectionalEpoch()` 等显式方法替代魔术数字。
- [x] 3.3 运行 `AssessmentDecisionDomainServiceImplTest` 全部用例，确保行为等价。

## 4. 补充与清理

- [x] 4.1 检查 `AssessmentTimeTest` 是否需要补充 `isGlobalFinalAssessment()` 委托后的新测试场景。
- [x] 4.2 运行相关模块单元测试（`AssessmentTimeTest`、`AssessmentDecisionDomainServiceImplTest`、`AssessmentDecisionPublicationServiceTest`）。
- [x] 4.3 检查编译无警告，代码符合项目风格（显式类型、无 `var`）。

## 5. 后端打包验证

- [x] 5.1 执行 `./mvnw clean compile`（或 `mvnw.cmd clean compile`）验证后端编译通过。
