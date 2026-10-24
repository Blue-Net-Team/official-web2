## Why

测试环境中候选人运行算法题时报 `RE`：判题服务把题库里的用例输入原样写入沙箱 `stdin`，而输入中的 `U+200B` 零宽空格不会被 `str.split()` 当作空白，导致 `int('12\u200b')` 抛 `ValueError`。

根因在写入侧：管理端通过浏览器文本框录入题面样例、默认运行用例时，把零宽字符、非断行空格等不可见字符一并粘贴入库。生成侧（`generator` 脚本输出到 OSS）由代码产生，未发现同类问题，因此本次只处理写入侧。

## What Changes

- 新增判题用例文本净化工具类，提供唯一净化方法：删除 `Cf` / `Zl` / `Zp` 类不可见字符，非 ASCII 空格转为 ASCII 空格，统一 `\r\n` / `\r` 为 `\n`，保留 `\t` 与 `\n`
- 算法题保存时对 `examples`、`runTestCases`、`testCases` 的 `input` 与 `expectedOutput` 执行净化，且净化发生在内容校验之前
- 候选人自定义运行输入在创建判题任务时执行净化
- 存量测试环境题目需管理端重新保存一次以触发净化

不在本次范围：前端改动、`judge-service` 改动、生成侧改动、`starterCode` 净化、正式环境数据处理。

## Capabilities

### New Capabilities

- `testcase-text-sanitization`: 判题用例文本与候选人自定义输入的不可见字符净化规则、生效边界与校验顺序

### Modified Capabilities

<!-- 现有算法判题与题目 CRUD 能力的需求语义不变，新增约束由 testcase-text-sanitization 承载 -->

## Impact

**代码**

- 新增 `com.bluenet.web.domain.util.TestcaseTextSanitizer`
- 修改 `AssessmentQuestionAppServiceImpl`（`createQuestion` / `updateQuestion` 写入路径）
- 修改 `com.bluenet.web.domain.model.entity.AlgorithmJudgeJob`（`customInput` 赋值）

**数据**

- `tb_assessment_question.content`：新写入的用例文本不再含不可见字符，非 ASCII 空格归一为 ASCII 空格，行结束符统一为 `\n`
- `tb_algorithm_judge_job.custom_input`：同上

**接口**

- 请求与响应结构不变，仅保存后的内容被归一化

**不受影响**

- `judge-service`、前端、`ai-service`、正式环境
- 判题语言支持、`\t` 与行内空格语义

**存量数据**

- 测试环境中受影响的题目需经管理端重新保存一次，`updateQuestion` 路径会触发净化；历史 `custom_input` 仅存在于已完成的判题任务记录中，不影响后续判题
