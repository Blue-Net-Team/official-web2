## Context

考生考题作答页由单一页面组件 `src/frontend/src/components/Assessment/QuestionDetail/index.tsx` 承载，按 `question.questionType` 分支渲染作答区，四种题型分别是 `FILE_UPLOAD`、`SINGLE_CHOICE`、`MULTIPLE_CHOICE`、`ALGORITHM`。页面顶部有一个**不区分题型**的「题目描述」面板，下面按题型渲染作答区（`AlgorithmQuestion` / `ChoiceQuestion` / `FileUploadArea`）。

当前实现的三个事实（缺陷根因）：

1. **题干字段是四题型共有的**。后端 `QuestionContent` 多态结构的公共基类 `BaseQuestionContent`（`src/frontend/src/apis/schema/assessment.dto.ts`）定义了 `type` 与 `content: string`（题干 Markdown）。四类具体内容都继承它。因此 `algorithmContent.content` 与 `fileContent.content` 是同一个字段。
2. **通用面板把内容硬 cast 成 `FileUploadContent`**，再取 `.content` 渲染。对算法题这个 cast 并不报错也不失效——它"碰巧"取到了正确的题干，于是题干被渲染了第一遍；`AlgorithmQuestion` 内部又渲染一次，共两遍。
3. **上传约束展示块写在题型分支之外**。`effectiveAllowedExtensions` 恒为 `FileUploadContent.allowedExtensions ?? ALLOWED_ARCHIVE_EXTENSIONS`，而后者后端从不返回，所以永远兜底为 `['zip','rar','7z','tar','gz','tar.gz','bz2','xz']`。该常量被无条件展示，于是算法题与选择题都出现「允许的文件类型：zip, rar, 7z…」。对文件上传题而言，同一信息还额外出现在上传卡头的 `uploadHintText` 与拖拽区的 `dropHintText`，共三处。

约束：项目规范要求组件内聚、类型严谨；页面所有容器面板已统一为 `SmokedGlassCard`（见 `frontend-assessment-question-page` 既有 requirement），本次改动不改变面板材质与布局骨架。

## Goals / Non-Goals

**Goals:**

- 题干在作答页只渲染一次，且渲染职责唯一归属于「题目描述」面板。
- 上传约束（扩展名 / 大小）只在 `FILE_UPLOAD` 路径上可达，其他题型的代码路径根本无法引用这些派生值。
- 三张作答卡（上传 / 选择 / 算法）的标题语法一致，题型标签只在页面头部出现一次。
- 保持既有交互（编辑、运行、提交、重新提交、过期只读、组队）与视觉骨架不变，改动范围限于两个文件、无接口契约变化。

**Non-Goals:**

- 不引入后端返回 `allowedExtensions` / `maxFileSize` 的能力，不修改 DTO 与接口。
- 不修改管理端题目编辑/预览页面。
- 不修改 `MarkdownRenderer` 的全局排版（标题层级差异属既有问题，牵涉其它使用方）。
- 不解决「管理员把题面同时写进 `title` 与 `content`」的内容层重复。
- 不重构页面的整体信息架构（侧栏、倒计时、判题结果面板不动）。

## Decisions

### 决策 1：题干单一来源放在通用「题目描述」面板，而不是算法卡

**选择**：通用面板保留题干渲染，`AlgorithmQuestion` 删除自身的 `MarkdownRenderer`。

**理由**：四种题型的题干是同一字段，若把题干下放到各作答卡，就需要在 `ChoiceQuestion`（当前不渲染题干）等组件里各补一份，重复度更高；而「上读题、下作答」是考生心智模型最自然的顺序，通用面板天然是题面的归属地。

**替代方案：A 打补丁式去重**（保留双渲染，仅在算法题时给通用面板的 `MarkdownRenderer` 加条件）。被否决：题面归属仍然分散，且未来新增题型会再次踩坑。

**替代方案：C 完全按题型分页组装**。被否决：重复代码多，与现有组件切分方式不符，收益不抵成本。

### 决策 2：删除通用面板中的上传约束块，而非用 `isFileUpload &&` 包住

**选择**：整块删除（不仅限于限定题型）。

**理由**：上传约束对文件上传题本身也是重复信息（上传卡头 `uploadHintText` 与拖拽区 `dropHintText` 已覆盖），且它离操作点更远。约束信息应出现在用户即将执行上传的区域内。删除后信息不丢失，且不存在"两个地方的值将来不同步"的风险。

**代价**：若未来产品希望"读题时就知道要交什么格式"，需要重新在上传区强化提示——这属于上传区的设计自由度，成本更低。

### 决策 3：作答卡标题改为动作短语

**选择**：`AlgorithmQuestion` 标题由写死的「算法题」改为「代码作答」；保留右侧「正式资源限制由判题配置决定」。`ChoiceQuestion`（「选择答案（单选/多选）」）与上传卡（「上传答案」）保持不变。

**理由**：卡片标题回答"这里能做什么"，题型回答"这是什么题"。题型已在页面 h1（`题目 N · 题型`）出现，重复写「算法题」既冗余又破坏三卡语法一致性。

### 决策 4：按 `BaseQuestionContent` 取题干，并把上传派生值收进 `isFileUpload` 分支

**选择**：
- 通用面板取题干改用 `BaseQuestionContent` 窄化（`QuestionContent` 联合类型的公共字段），不再 cast 成 `FileUploadContent`。
- `fileContent` 及 `effectiveAllowedExtensions` / `uploadHintText` / `dropHintText` / `draggerProps` 等上传专用派生值，在结构上限定在 `isFileUpload` 为真的分支内计算与使用（提取为独立的小构造函数或 hook，返回 `null` 供非上传题型短路）。

**理由**：缺陷的**结构性**原因是"算法题路径也能看见上传配置"。仅在展示处加条件（方案 A）依赖开发者记得加门禁；把派生值限定在上传分支后，非上传题型在类型层就拿不到这些变量，错误表达不成立。

**代价**：`draggerProps` 是一个较大的闭包（含 `customRequest` / `beforeUpload` / `onDrop`），把它从页面组件抽出会带来一次小的搬迁风险；抽成 `useArchiveUploadConfig(question, ...)` 需要把 `setUploadedFile`、`message`、`upload` 等依赖作为入参传递。允许分两步：先做结构收进分支（必需），再视情况抽 hook（可选优化）。

### 决策 5：题干为空时仍渲染通用面板

**选择**：`MarkdownRenderer` 的 `emptyText="暂无题目描述"` 保持不变，面板不因题干为空而隐藏。

**理由**：面板头部承载 `question.title`，是长页面上的导航锚点；删掉面板会让"没有题干的题"在视觉上突然空出一块、也让标题消失。

## Risks / Trade-offs

- **[算法题页面变短后，样例上移导致首屏信息密度变化]** → 属于预期效果；验证时确认输入/输出/数据范围网格与样例仍成组呈现、不出现孤立分隔线。
- **[删除通用面板的上传约束后，考生在检查上传区之前不知道格式要求]** → 上传卡头与拖拽区仍有提示，且这是操作发生的位置；同时在验收清单中确认两处提示文案一致。
- **[把 `draggerProps` 搬进分支/hook 可能引入闭包依赖遗漏（`setUploadedFile`、`effectiveAllowedExtensions`）]** → 该搬迁可选；若执行必须走完整上传回归（正常上传、非法扩展名、超大小、空文件、拖拽文件夹、重新提交）。
- **[四题型回归面]** → 单个页面组件 + 一个作答卡组件；用 Playwright 以真实账号逐题型验证（算法 / 文件上传 / 单选 / 多选 / 过期态）。
- **[与既有「烟色玻璃」面板规范冲突]** → 本次不新增/不删除面板，只调整面板内部内容，材质规范不受影响；但需确认删除内容后 `SmokedGlassCard` 的 padding 与 `<hr>` 间距无空洞。
- **[类型收窄可能暴露既有 `as` 断言掩盖的类型问题]** → 编译期即可发现，属于收益（提前暴露）而非风险。

## Migration Plan

1. 纯前端展示层改动，与后端并行部署无依赖；不需要数据迁移或灰度开关。
2. 部署顺序：随前端常规发布即可。若需回滚，回滚前端镜像到上一版本即恢复原行为。
3. 验证顺序建议：本地 `pnpm dev`（需先确认 3000 端口未被占用）→ 逐题型自测 → Playwright 端到端 → 提交。

## Open Questions

- `AlgorithmQuestion` 标题的最终文案（「代码作答」/「编写代码」/「作答区」）是否需要在设计评审时与产品确认？
- 是否要在管理端题干字段旁补充「标题用于目录与页头，题面请填在描述中」的提示，以从内容侧避免 title/content 重复（当前列为非目标）？
- 通用面板删除上传约束后，是否需要在文件上传题的拖拽区把大小限制文案加粗以补偿提示强度？
