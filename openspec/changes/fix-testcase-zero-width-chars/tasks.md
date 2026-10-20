## 1. 测试先行：净化工具类

- [x] 1.1 新建 `src/backend/src/test/java/com/bluenet/web/domain/util/TestcaseTextSanitizerTest.java`
- [x] 1.2 覆盖 `Cf` 类删除：`U+200B`、`U+200C`、`U+200D`、`U+2060`、`U+FEFF`、`U+00AD`
- [x] 1.3 覆盖 `Zl` / `Zp` 删除：`U+2028`、`U+2029`
- [x] 1.4 覆盖 `Zs` 转 ASCII 空格：`U+00A0`、`U+3000`、`U+2000`，并断言 `a\u00a0b` 仍为两个空格分隔的 token
- [x] 1.5 覆盖 `\r\n` / `\r` 归一为 `\n`，且 `\t`、`\n`、ASCII 空格保留
- [x] 1.6 覆盖 `null` 返回 `null`、空串返回空串、二次净化结果不变
- [x] 1.7 运行 `./mvnw -B test -Dtest=TestcaseTextSanitizerTest` 确认测试失败（类未创建）

## 2. 实现净化工具类

- [x] 2.1 新建 `src/backend/src/main/java/com/bluenet/web/domain/util/TestcaseTextSanitizer.java`，`final` 类 + `@NoArgsConstructor(access = AccessLevel.PRIVATE)`，风格对齐 `HashUtils`
- [x] 2.2 实现唯一对外方法 `public static String sanitize(String input)`，按顺序执行：`\r\n`/`\r` → `\n`、删除 `\p{Cf}`、删除 `\p{Zl}`/`\p{Zp}`、非 ASCII `\p{Zs}` → ASCII 空格
- [x] 2.3 运行 `./mvnw -B test -Dtest=TestcaseTextSanitizerTest` 确认全部通过

## 3. 写入侧接入：题目用例

- [x] 3.1 在 `AssessmentQuestionAppServiceImpl` 增加私有方法 `normalizeQuestionContent(QuestionContent content)`，对 `AlgorithmContent` 的 `examples`、`runTestCases`、`testCases` 的 `input` 与 `expectedOutput` 调用 `TestcaseTextSanitizer.sanitize`
- [x] 3.2 在 `createQuestion`（`validateQuestionContent` 之前）调用 `normalizeQuestionContent`
- [x] 3.3 在 `updateQuestion`（`validateQuestionContent` 之前）调用 `normalizeQuestionContent`
- [x] 3.4 确认非算法题型与 `content`、`inputDescription`、`outputDescription`、`constraints`、`explanation`、`starterCode` 不被修改

## 4. 写入侧接入：自定义运行输入

- [x] 4.1 在 `AlgorithmJudgeJob.create(...)` 内对 `customInput` 赋值时调用 `TestcaseTextSanitizer.sanitize`

## 5. 应用层测试

- [x] 5.1 新增「保存含 `U+200B` 的 `examples` / `runTestCases` 后，持久化内容不含不可见字符」的测试
- [x] 5.2 新增「用例 input 只含零宽空格时，保存请求被拒绝且报错指出第 N 个用例」的测试
- [x] 5.3 新增「`run` 接口提交含零宽字符的 `customInput` 后，判题任务实体字段已净化」的测试
- [x] 5.4 运行 `./mvnw -B test` 确认 backend 全量测试通过，含 `ArchitectureRulesTest`

## 6. 校验与收尾

- [x] 6.1 运行 `./mvnw -B clean compile` 确认编译通过
- [x] 6.2 运行 spotless 校验（`./mvnw -B spotless:check`），必要时执行 `spotless:apply`
- [x] 6.3 确认 `judge-service` 与前端无任何改动（`git status`）

## 7. 测试环境存量数据

- [ ] 7.1 执行定位 SQL 找出测试环境中受影响的题目：

```sql
SELECT id, title
FROM tb_assessment_question
WHERE content ~ ('[' || chr(8203) || chr(8204) || chr(8205) || chr(65279) || chr(160) || chr(12288) || ']');
```

- [ ] 7.2 对查询结果中每道题，在管理端打开并保存一次，触发 `updateQuestion` 归一化
- [ ] 7.3 重新执行 7.1 的 SQL，确认结果为空
