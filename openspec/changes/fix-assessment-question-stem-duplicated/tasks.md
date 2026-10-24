## 1. 题面单一来源

- [x] 1.1 在 `src/frontend/src/components/Assessment/QuestionDetail/index.tsx` 中，将通用「题目描述」面板的题干取值由 `fileContent?.content` 改为按 `BaseQuestionContent` 窄化取 `content`（`questionContent?.content`），移除对 `FileUploadContent` 的硬 cast 依赖
- [x] 1.2 在 `src/frontend/src/components/Assessment/QuestionDetail/AlgorithmQuestion.tsx` 中删除 `<MarkdownRenderer content={algorithmContent?.content} emptyText="暂无题目描述" />`，并移除不再使用的 `MarkdownRenderer` 导入
- [ ] 1.3 确认算法卡在无题干渲染后，首个内容块（输入/输出/数据范围网格或样例）与卡片头部的 `<hr>` 间距合理，无空段落或双分隔线

## 2. 上传约束收归文件上传题

- [x] 2.1 删除 `index.tsx` 通用「题目描述」面板中的「允许的文件类型」展示块
- [x] 2.2 删除 `index.tsx` 通用「题目描述」面板中的「最大文件大小」展示块（`fileContent?.maxFileSize` 分支）
- [x] 2.3 将 `fileContent` 及 `effectiveAllowedExtensions` / `allowedExtsText` / `maxSizeText` / `uploadHintText` / `dropHintText` / `draggerProps` 等上传专用派生值收进 `isFileUpload` 为真的路径（分支内计算或抽为独立构造函数/hook），使非上传题型在类型层无法引用
- [x] 2.4 保持 `allowedExtensions` 缺省时回退 `ALLOWED_ARCHIVE_EXTENSIONS` 的行为不变，并确认 `accept`、`beforeUpload`、`dropHintText`、`uploadHintText` 四处使用的扩展名集合一致
- [x] 2.5 运行前端类型检查与 lint，确认无未使用变量、无因收窄暴露的隐式 any

## 3. 作答卡标题语义统一

- [x] 3.1 将 `AlgorithmQuestion` 卡片标题由「算法题」改为动作短语「代码作答」（保留 `ExperimentOutlined` 与右侧「正式资源限制由判题配置决定」提示）
- [x] 3.2 对照 `ChoiceQuestion`（「选择答案（单选/多选）」）与上传卡（「上传答案」）确认三卡标题语法一致，且页面 h1 的题型标签未重复

## 4. 逐题型自测

- [ ] 4.1 算法题：题面只出现一次；无「允许的文件类型 / 最大文件大小」；输入输出说明、数据范围、样例、语言选择、编辑器、运行/提交控件齐全可用
- [ ] 4.2 文件上传题：题面只出现一次；扩展名与大小提示仅在上传区可见；正常上传、非法扩展名、超大小、空文件、拖拽文件夹、重新提交均行为不变
- [ ] 4.3 单选题与多选题：题面只出现一次；页面不再出现任何文件类型或大小提示；选项选中/提交/重新提交正常
- [ ] 4.4 过期态（`isExpired`）：顶部横幅、只读编辑器、隐藏提交/重新提交按钮等行为不变
- [ ] 4.5 组队场景（`allowTeam` 且题面为文件上传题）：队长上传、队员只读、未入队提示三种分支均正常

## 5. 端到端验证

- [x] 5.1 确认 3000 端口占用情况：已占用则复用现有前端服务，未占用再启动 `pnpm dev`（禁止重复启动）
- [ ] 5.2 用 Playwright 以考生账号依次打开算法题、文件上传题、单选题、多选题作答页，逐个断言题干文本在页面中仅出现一次、上传约束仅在文件上传题出现
- [ ] 5.3 对算法题完整走一次「运行默认用例 → 提交答案 → 查看判题结果」链路，确认删除题干后运行/提交/结果面板无回归
- [ ] 5.4 截图归档四类题型的作答页，作为视觉回归基线

## 6. 收尾

- [ ] 6.1 按 `fix: 考生端考题页题干重复渲染并修正上传约束展示范围` 规范提交（冒号后空格、描述 ≥5 字符）
- [ ] 6.2 若发现管理端题干与标题重复的问题，记录为后续独立变更而非纳入本次范围
