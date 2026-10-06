## 1. 迁移作答页主卡与侧栏（QuestionDetail/index.tsx + QuestionSidebar.tsx）

- [x] 1.1 `index.tsx`：题面主卡容器（`bg-white/[0.06]` 圆角壳）替换为 `<SmokedGlassCard>`（deep，不挂 hoverable），删除内容根部 Tailwind padding
- [x] 1.2 `index.tsx`：其余容器级面板（工具栏、判题结果等外层壳）替换为 `<SmokedGlassCard tone="soft">`
- [x] 1.3 `index.tsx`：侧栏 `<aside>` 保持 `lg:sticky` 布局不变，sticky 类随容器透传（backdrop-filter 不影响 sticky）
- [x] 1.4 `QuestionSidebar.tsx`：队伍面板、倒计时面板、提交面板等最外层 `bg-white/[0.06] rounded-xl` 壳替换为 `<SmokedGlassCard tone="soft">`（删除原 `p-5`）
- [x] 1.5 `QuestionSidebar.tsx`：题目导航格内层容器去底留框——移除 `bg-white/[0.03]`，边框改为 `border-white/[0.15]`

## 2. 迁移题型组件（AlgorithmQuestion / ChoiceQuestion / JudgeResultPanel / TeamPanel / FileUploadArea / CountdownSection）

- [x] 2.1 `AlgorithmQuestion.tsx`：主 section（`bg-white/[0.06] rounded-xl p-7`）替换为 `<SmokedGlassCard tone="soft">`，原 28px 内边距用 `--sgc-padding: 28px` 覆盖
- [x] 2.2 `AlgorithmQuestion.tsx`：用例格/提交记录格等内层"面板状"容器去底留框（`border-white/[0.15]`）；语言选择器、结果标签等点缀不动
- [x] 2.3 `AlgorithmQuestion.tsx`：代码编辑区 `bg-black/30` 凹槽语义保留，零改动
- [x] 2.4 `ChoiceQuestion.tsx`：选项容器外层壳替换为 `<SmokedGlassCard tone="soft">`，选项行选中态（`#6677ff` 系）不动
- [x] 2.5 `JudgeResultPanel.tsx`：结果面板外层壳替换为 `<SmokedGlassCard tone="soft">`，判题状态标签不动
- [x] 2.6 `TeamPanel.tsx`：队伍面板外层壳（`bg-white/[0.06] rounded-xl p-5`）替换为 `<SmokedGlassCard tone="soft">`；成员行 `bg-white/[0.04]` 为点缀级，不动
- [x] 2.7 `FileUploadArea.tsx`、`CountdownSection.tsx`：容器级透明壳替换为 soft，原子元素不动

## 3. 清理 questions 列表页残留与全局核查

- [x] 3.1 `assessment/[timeId]/questions/page.tsx`：核查 2 处 `bg-white/[0.x]` 命中，容器级替换为 SmokedGlassCard、点缀级标记不动
- [x] 3.2 全局复查：`grep -rn "bg-white/\[0" src/frontend/src/components/Assessment src/frontend/src/app/\(public\)/\(other\)/assessment`，确认无容器级透明玻璃残留（仅剩点缀级）
- [x] 3.3 确认未改动 `MarkdownRenderer.tsx`、`QuestionStemMarkdownEditor.tsx` 的渲染样式（点缀级，零改动范围）

## 4. 验证与清理

- [x] 4.1 Playwright 打开 `/glass-demo` 对照页复核 B2 方案观感，然后访问考题作答页：比对迁移前后截图（重点：内边距无挤压/空旷、DarkVeil 光斑下内层格子分层清晰、sticky 侧栏正常）
- [x] 4.2 微信 XWEB 等 backdrop-filter 失效场景无法直接验证时，用 devtools 关闭 `backdrop-filter` 确认面板仍有形（保底底色生效）
- [x] 4.3 跑通作答页核心链路冒烟：选择题提交、算法题运行/提交、文件上传、倒计时显示无样式错位
- [x] 4.4 删除临时评审页 `src/app/(public)/(other)/glass-demo/page.tsx`（含空目录）
- [x] 4.5 提交前自查：无新增 tone 档位、无 antd 样式覆盖、无布局结构变更
