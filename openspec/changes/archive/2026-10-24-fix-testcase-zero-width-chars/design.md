## Context

测试环境中候选人运行算法题时出现 `RE`。判题服务把用例输入字节原样写入沙箱 `stdin`，输入中的 `U+200B`（ZERO WIDTH SPACE）不属于 Python 认定的空白字符（`'\u200b'.isspace()` 为 `False`），因此 `sys.stdin.read().split()` 不会在它之前切分，`int('12\u200b')` 抛 `ValueError`。

`stdin` 有三条来源，全部由 api 服务写入、judge-service 原样消费：

| 来源 | 存储位置 | 写入方式 |
|---|---|---|
| `FORMAL` | OSS 用例文件 | judge-service 生成（generator 脚本 stdout） |
| `DEFAULT_RUN` | `tb_assessment_question.content` 的 `examples` / `runTestCases` | api 服务，管理端浏览器录入 |
| `CUSTOM_RUN` | `tb_algorithm_judge_job.custom_input` | api 服务，候选人浏览器录入 |

经排查，`FORMAL` 已排除：generator 源码与输出均由代码产生，实际数据中未发现不可见字符。问题集中在两条浏览器录入路径，即 `DEFAULT_RUN` 与 `CUSTOM_RUN`。

现有写入路径没有任何文本归一化：`AssessmentQuestionAppServiceImpl.validateQuestionContent`（`:417`）只校验非空，`AlgorithmJudgeJob.create` 只做字段赋值。

## Goals / Non-Goals

**Goals:**

- 管理端保存算法题时，用例字段（`examples` / `runTestCases` / `testCases` 的 `input` 与 `expectedOutput`）入库前完成归一化，不再含不可见字符
- 候选人自定义运行输入落库前完成归一化
- 归一化规则集中在一处实现，外部只有一个调用入口
- 归一化先于内容校验，使「只含不可见字符」的用例被正确拒绝

**Non-Goals:**

- 不修改 judge-service、前端、`ai-service`
- 不修改生成侧（`TestDataGenerationWorkflow`）与 OSS 用例文件
- 不对 `content`、`inputDescription`、`outputDescription`、`constraints`、`explanation`、`starterCode` 做归一化
- 不引入数据库迁移脚本
- 不引入跨服务共享模块
- 不改变判题语义、判题语言支持、`\t` 与行内空格语义

## Decisions

### D1：只在 api 服务写入侧归一化，不改 judge-service

`FORMAL` 路径已排除污染，`DEFAULT_RUN` 与 `CUSTOM_RUN` 的污染字节都经 api 服务写入。在写入侧归一化后，judge-service 拿到的 `stdin` 本身就是干净字节，无需修改。

被否决的替代方案：

- **judge-service 读取侧归一化**：能覆盖已有脏数据，但需在 `FormalTestcaseBundle` 建立唯一构造入口，且若两端都要归一化则需跨服务共享类
- **两端都归一化**：需要新增 `src/common` 模块。根 `pom.xml` 只有 `src/backend` 与 `src/judge-service` 两个 module，而 `ci.yml` 分别以 `-f` 指向单个 pom 构建，新增共享模块要求改动 4 处构建命令（`:102`、`:180`、`:256`、`:346`）为先 `install` 共享模块。judge-service 无需改动时，这个成本没有收益

### D2：单一工具类 + 单一方法

新增 `com.bluenet.web.domain.util.TestcaseTextSanitizer`，与现有 `HashUtils`、`GradeCalculator`、`PasswordGenerator` 同包。类为 `final`，构造器私有（`@NoArgsConstructor(access = AccessLevel.PRIVATE)`）。

唯一对外方法：

```java
public static String sanitize(String input)
```

不提供 `byte[]` 重载：写入侧的货币是字符串字段，多一个入口只增加用错的可能。`null` 返回 `null`，空串返回空串，幂等。

`ArchitectureRulesTest` 未对不同包路径的静态工具类设限制，放置在 `domain.util` 不会触发架构测试失败。

### D3：净化规则与执行顺序

单次调用内按序执行：

| 顺序 | 规则 | 目的 |
|---|---|---|
| 1 | `\r\n` → `\n`，`\r` → `\n` | 统一行结束符 |
| 2 | 删除 `\p{Cf}` | 零宽空格、零宽连接符、词连接符、BOM、软连字符 |
| 3 | 删除 `\p{Zl}`、`\p{Zp}` | 行分隔符、段分隔符 |
| 4 | 非 ASCII 的 `\p{Zs}` → ASCII 空格 | `U+00A0`、`U+3000`、`U+2000`–`U+200A` 等 |
| 5 | 保留 `\t`、`\n`、ASCII 空格 | 不参与归一化 |

字符类别已在 JDK 21 上核实：`U+200B`/`U+200C`/`U+200D`/`U+2060`/`U+FEFF`/`U+00AD` 为 `Cf`(16)；`U+00A0`/`U+3000` 为 `Zs`(12)；`U+2028` 为 `Zl`(13)、`U+2029` 为 `Zp`(14)；`\t`/`\n` 为 `Cc`(15)。

第 4 步不能简化为删除：`a\u00a0b` 删掉会变成 `ab`，把两个 token 粘连成一个。转为 ASCII 空格后，Python 的 `split()` 与 C 的 `scanf("%d")` 对该输入的行为一致（`scanf` 不认 `U+00A0` 为空白，是与之并列的隐患）。

### D4：调用点放在独立归一化步骤，不放进校验方法

`AssessmentQuestionAppServiceImpl` 增加私有方法 `normalizeQuestionContent(QuestionContent content)`，在 `createQuestion`（`:88` 前）与 `updateQuestion`（`:131` 前）显式调用，随后再调用现有的 `validateQuestionContent`。

不把归一化并入 `validateQuestionContent`：该方法当前语义是纯校验（只抛异常、不改数据），混入字段写入会让命名与行为不符，后续维护者难以判断调用该方法是否会产生副作用。

被否决的替代方案：

- **在 `QuestionContent` 增加 `normalizeForStorage()` 抽象方法**：与现有 `sanitizeForUser()` 对称，形态更领域化。但它会给非算法题型引入空实现，且归一化只针对用例字段，不是所有题型内容都有该概念，抽象层级不匹配
- **在 `QuestionContentTypeHandler` 归一化**：能覆盖所有写路径，但该处属基础设施层，承担业务规则不合适，且需要处理全部字段而非仅用例字段

### D5：`customInput` 在领域实体构造时归一化

在 `AlgorithmJudgeJob.create(...)` 内对 `customInput` 赋值时调用 `TestcaseTextSanitizer.sanitize`。`AlgorithmJudgeJob` 是唯一构造入口（`AlgorithmJudgeAppServiceImpl:93-101`），放在领域层可覆盖未来新增的调用方，同时避免在 DTO 或 converter 中重复逻辑。

### D6：归一化先于校验，顺带修复校验缺陷

`validateTestCases`（`:443-457`）用 `hasText`（`:492`）判断非空，底层是 `Character.isWhitespace`，不认 `U+200B`，因此「input 只由一个零宽空格组成」的用例目前能通过校验。归一化在校验之前执行后，该输入变为空串，`hasText` 会正确返回 false，无需单独修改 `hasText`。

### D7：存量数据不做迁移脚本

一次性 SQL 需要覆盖 `chr(8203)`、`chr(8204)`、`chr(8205)`、`chr(65279)`、`chr(160)`、`chr(12288)` 等多个码点，且要处理 JSONB 内部转义，出错成本高于收益。采用管理端重新保存题目触发 `updateQuestion` 的方式，与正常写入路径一致，不需要额外维护一套迁移逻辑。

### D8：不记录归一化日志

写入侧的归一化是管理端显式保存动作的期望行为，记录 WARN 会为每次带不可见字符的正常粘贴产生噪声。若后续需要追溯污染来源，再以独立变更加可观测性。

## Risks / Trade-offs

- **合法的零宽字符被剥离** → 零宽字符不用于中英文排版，且归一化只作用于用例的 `input` / `expectedOutput`，题面 markdown 与 `starterCode` 不动，影响面可控
- **沉淀在库中的存量脏数据不会被自动修复** → 部署后需管理端重新保存受影响题目；变更内提供定位 SQL，并在实施说明中明确这一步
- **管理员保存后内容与他粘贴的原始文本不完全一致** → 用例字段由程序消费，不存在展示一致性承诺；归一化只影响不可见字符与非 ASCII 空格
- **「只含不可见字符」的用例从静默通过变为校验失败** → 这是期望行为，报错信息含「第 N 个用例」，定位明确
- **数据库比较行为变化** → `tb_assessment_question.content` 内文本归一后，按内容精确比对的历史逻辑（若有）结果可能不同；用例字段未参与任何按内容匹配的查询，风险不存在
- **未覆盖绕过 api 服务的写入** → 直接连接数据库写入的脏数据不会被归一化；当前不存在该路径（无导入脚本、无批量导入接口）

## Migration Plan

1. 部署 api 服务（无数据库迁移、无配置变更）
2. 用以下查询定位测试环境中受影响的题目：

   ```sql
   SELECT id, title
   FROM tb_assessment_question
   WHERE content::text ~ ('[' || chr(8203) || chr(8204) || chr(8205) || chr(65279) || chr(160) || chr(12288) || ']');
   ```

3. 对查询结果中的每道题，在管理端打开并保存一次，触发 `updateQuestion` 归一化
4. 回滚策略：还原代码即可。已归一化的用例文本仍是合法文本，无需数据回滚

## Open Questions

- 是否需要为归一化动作增加可观测性（记录被剥离的码点与题目 ID），以便长期追溯污染来源。本次按 D8 不做，留待后续变更评估
