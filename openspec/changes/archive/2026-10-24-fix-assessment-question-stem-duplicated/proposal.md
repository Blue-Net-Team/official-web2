## Why

考生端考题作答页（`/assessment/[timeId]/questions/[questionId]`）的题干渲染存在两处结构性缺陷：

1. **题面重复**：通用「题目描述」卡无条件用 `MarkdownRenderer` 渲染 `question.content.content`，而算法题作答卡 `AlgorithmQuestion` 又渲染了同一个字段，导致算法题考生看到两份题面（上卡无样例、下卡带样例）。
2. **上传约束渗漏到所有题型**：同一张通用卡把「允许的文件类型 / 最大文件大小」写在题型分支之外，且 `allowedExtensions` 后端从不返回、永远兜底到压缩包白名单常量，于是算法题和选择题都会显示「允许的文件类型：zip, rar, 7z…」这种与题型无关的信息。对文件上传题而言该信息还是**三重复**（通用卡 + 上传卡头部 + 拖拽区提示）。

这是纯展示层缺陷，影响所有考生答题体验，且会误导考生认为算法题需要上传压缩包，应当立即收敛。

## What Changes

- **题干归属唯一化**：通用「题目描述」卡成为四类题型（文件上传 / 单选 / 多选 / 算法）唯一的题干 markdown 渲染点；`AlgorithmQuestion` 移除自身的 `MarkdownRenderer`，只保留输入说明、输出说明、数据范围、样例、语言选择、代码编辑器与运行/提交控件。
- **上传约束归位上移**：删除通用卡中的「允许的文件类型 / 最大文件大小」展示块，上传约束仅保留在文件上传作答区（上传卡头部提示 + 拖拽区提示），不再出现在算法题与选择题页面。
- **作答卡标题语义统一**：`AlgorithmQuestion` 卡片标题由写死的「算法题」改为动作短语（与「选择答案（单选/多选）」「上传答案」对齐），题型信息仅由页面头部 `题目 N · 题型` 承担。
- **类型收窄**：通用卡按 `BaseQuestionContent` 取题干，替代「把 `question.content` 硬 cast 成 `FileUploadContent` 再取 `.content`」的写法；文件上传相关的派生值（有效扩展名、大小提示、`UploadProps`）显式限定在 `isFileUpload` 分支，使错误引用在类型层不成立。

**BREAKING**: 无。纯前端展示层重排，不改动 API 请求/响应契约、不改动数据库、不改动管理端。

## Capabilities

### New Capabilities

（无新增能力）

### Modified Capabilities

- `frontend-assessment-question-page`: 新增「考生端题干单一来源」与「上传约束仅对文件上传题可见」两条约束，并调整算法题作答区的展示范围（不再包含题干）。

## Impact

- **前端代码**（仅两个文件）：
  - `src/frontend/src/components/Assessment/QuestionDetail/index.tsx` —— 通用题面卡的题干取值与上传约束展示范围
  - `src/frontend/src/components/Assessment/QuestionDetail/AlgorithmQuestion.tsx` —— 移除题干渲染、调整卡片标题
- **受影响页面**：考生端考题作答页，四类题型（`FILE_UPLOAD` / `SINGLE_CHOICE` / `MULTIPLE_CHOICE` / `ALGORITHM`）全部。
- **不受影响**：管理端题目编辑与预览（`app/admin/assessment/question/*`）、后端接口、判题链路、组队与提交逻辑、倒计时与侧栏。
- **非目标（Non-goals）**：
  - 不处理「管理员把题面同时写进 `title` 与 `content`」造成的内容层重复（属运营约定，另议）；
  - 不调整 `MarkdownRenderer` 的全局标题层级（会影响其它使用方）；
  - 不引入后端返回 `allowedExtensions` 的能力。
- **验证方式**：Playwright 逐题型（算法 / 文件上传 / 单选 / 多选 / 过期态）核对题面仅出现一次、上传约束仅在上传题可见、作答交互无回归。
