## 1. 测试先行

- [x] 1.1 在现有架构测试体系中新增 API DTO 校验消息约定测试，扫描字段、record 组件及嵌套请求类型上的 Jakarta Validation 约束，要求 `message` 非空白，并运行测试确认当前缺失项会失败。
- [x] 1.2 为 7 个受影响 DTO 添加 Jakarta Validator 测试，覆盖必填、空白字符串、空集合、嵌套对象及数值边界场景，断言预期中文字段提示并确认修复前失败。
- [x] 1.3 在相关现有 Controller 集成测试中补充代表性校验失败场景，断言 HTTP 400、`ResponseMessage` 结构及明确的 `msg`。

## 2. 补齐校验消息

- [x] 2.1 为 `CommentRequestDTO` 和 `FinalizeScoreRequestDTO` 的 3 个 `@NotNull` 约束补充答案ID、最终得分等字段提示。
- [x] 2.2 为 `ConfirmJudgeLanguageLimitRequestDTO` 的 6 个必填与最小值约束补充时间、内存、输出限制及单位提示。
- [x] 2.3 为 `UpsertJudgeProblemConfigRequestDTO` 及其嵌套请求类型的 20 个约束补充生成器、标准解、测试用例和数值边界提示。
- [x] 2.4 为 `UpdateTagDescriptionRequestDTO`、`UploadKnowledgeDocRequestDTO` 和 `SoftwareResourceListRequestDTO` 的 5 个约束补充标签描述、Markdown 文件及分页边界提示。
- [x] 2.5 检查所有改动仅增加 `message`，未改变注解类型、校验值、字段定义或已有消息。

## 3. 验证与交付

- [x] 3.1 运行新增 DTO 测试、架构约定测试及相关 Controller 测试，确认全部通过且合法请求行为不变。
- [x] 3.2 执行后端编译打包及完整相关测试，确认无回归。
- [x] 3.3 按项目运行配置重建并启动后端 Docker 容器，通过代表性无效请求端到端验证明确校验消息及 HTTP 400 响应。
