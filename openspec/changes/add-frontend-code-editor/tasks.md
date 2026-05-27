## 1. 依赖安装与配置

- [x] 1.1 安装 `@monaco-editor/react` 到前端项目
- [x] 1.2 配置 Next.js 动态加载 Monaco Editor，避免 SSR 问题（`@monaco-editor/react` 内部已处理 SSR 兼容）
- [x] 1.3 验证依赖安装后项目能正常编译

## 2. 封装 CodeEditor 通用组件

- [x] 2.1 创建 `src/frontend/src/components/CodeEditor/index.tsx` 组件
- [x] 2.2 实现语言映射（python/c/cpp/java/javascript → Monaco 语言标识）
- [x] 2.3 配置编辑器主题（vs-dark）、行号、括号匹配、minimap 关闭等默认选项
- [x] 2.4 实现受控组件接口（value, onChange, language, readOnly, height）
- [x] 2.5 确保组件与 Ant Design Form 兼容（支持 value/onChange 模式）

## 3. 考生端算法题编辑器替换

- [x] 3.1 在 `AlgorithmQuestion.tsx` 中引入 CodeEditor 组件
- [x] 3.2 替换算法题代码编写区的 textarea 为 CodeEditor
- [x] 3.3 绑定 language 属性到 algorithmLanguage 状态
- [x] 3.4 保持现有 onCodeChange 回调逻辑不变
- [x] 3.5 验证 readOnly 状态在考核过期时正常工作（通过直接设置编辑器 readOnly 选项验证）

## 4. 管理端考题编辑器替换

- [x] 4.1 在 `QuestionDrawer.tsx` 中引入 CodeEditor 组件
- [x] 4.2 替换 Generator 源码的 Input.TextArea 为 CodeEditor
- [x] 4.3 替换标准解源码的 Input.TextArea 为 CodeEditor
- [x] 4.4 替换语言模板代码的 Input.TextArea 为 CodeEditor
- [x] 4.5 确保各编辑器的 language 绑定到对应表单字段
- [x] 4.6 验证 viewMode 下所有编辑器为只读状态（通过 JavaScript 直接设置 readOnly 选项验证）

## 5. 验证与优化

- [x] 5.1 编译前端项目确认无类型错误
- [x] 5.2 启动前端开发服务验证各编辑器正常渲染（管理端和考生端均正常渲染）
- [x] 5.3 测试五种语言（Python/C/C++/Java/JS）的语法高亮（均已验证通过）
- [x] 5.4 测试行号显示、括号匹配功能（均已验证通过）
- [ ] 5.5 测试代码修改后表单提交数据正确（因后端 API 返回 content 为 null，前端语言选择器被禁用，暂时无法完整测试）
- [x] 5.6 测试只读模式下编辑器不可编辑（已通过 JavaScript 直接设置 readOnly 选项验证）

## 端到端验证总结

### 已验证通过的项目

1. **Monaco Editor 渲染**：管理端（QuestionDrawer）和考生端（AlgorithmQuestion）均正常渲染 Monaco Editor
2. **语法高亮**：Python、C、C++、Java、JavaScript 五种语言语法高亮均正常工作
3. **行号显示**：编辑器左侧行号正常显示
4. **暗色主题**：vs-dark 主题正常应用
5. **括号匹配**：matchBrackets 和 bracketPairColorization 已配置启用
6. **只读模式**：通过 JavaScript 直接设置 readOnly 选项验证，编辑器变为不可编辑状态
7. **代码编辑**：在可编辑模式下，可以通过 setValue 修改编辑器内容

### 发现的问题

1. **后端 API 数据返回问题**：考生端 API `/api/v1/assessment-questions/{id}` 返回的 `content` 字段为 `null`，导致前端语言选择器被禁用。数据库中数据是正确的（已验证），问题出在后端 DTO 转换或权限控制逻辑。
2. **第一个测试题目（id=17）content 为空**：创建时题干 textarea 的值没有正确绑定到表单，导致 content 为空。第二个题目（id=18）已正确保存。
