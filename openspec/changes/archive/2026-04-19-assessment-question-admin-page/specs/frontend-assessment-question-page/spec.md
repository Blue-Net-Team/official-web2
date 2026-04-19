## MODIFIED Requirements

### Requirement: 题型 Content 类型定义
前端 DTO 中题型 Content 类型 SHALL 与后端 `QuestionContent` 多态 JSON 结构完全一致。

所有 Content 类型继承基类字段 `type`（用于多态标识）和 `content`（题干）。

具体字段对齐：
- `SingleChoiceContent`: `content`（题干）, `options`（string[]）, `correctAnswer`（string）
- `MultipleChoiceContent`: `content`（题干）, `options`（string[]）, `correctAnswers`（string[]）
- `FileUploadContent`: `content`（题干），无额外字段
- `AlgorithmContent`: `content`（题干）, `testCases`（TestCase[]，含 `input` 和 `expectedOutput`）, `timeLimit`（number）, `memoryLimit`（number）

#### Scenario: DTO 字段名称正确
- **WHEN** 前端发送创建/更新考题请求
- **THEN** Content JSON 中题干字段名为 `content`，选项字段名为 `options`，正确答案字段名为 `correctAnswer`（单选）或 `correctAnswers`（多选）

#### Scenario: DTO 字段类型正确
- **WHEN** 前端处理单选题的 `correctAnswer`
- **THEN** 类型为 `string`（选项文本），而非 `number`

#### Scenario: DTO 字段类型正确-多选
- **WHEN** 前端处理多选题的 `correctAnswers`
- **THEN** 类型为 `string[]`（选项文本数组），而非 `number[]`
