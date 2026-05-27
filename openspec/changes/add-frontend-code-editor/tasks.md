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
- [ ] 3.5 验证 readOnly 状态在考核过期时正常工作

## 4. 管理端考题编辑器替换

- [x] 4.1 在 `QuestionDrawer.tsx` 中引入 CodeEditor 组件
- [x] 4.2 替换 Generator 源码的 Input.TextArea 为 CodeEditor
- [x] 4.3 替换标准解源码的 Input.TextArea 为 CodeEditor
- [x] 4.4 替换语言模板代码的 Input.TextArea 为 CodeEditor
- [x] 4.5 确保各编辑器的 language 绑定到对应表单字段
- [ ] 4.6 验证 viewMode 下所有编辑器为只读状态

## 5. 验证与优化

- [x] 5.1 编译前端项目确认无类型错误
- [ ] 5.2 启动前端开发服务验证各编辑器正常渲染
- [ ] 5.3 测试五种语言（Python/C/C++/Java/JS）的语法高亮
- [ ] 5.4 测试行号显示、括号匹配功能
- [ ] 5.5 测试代码修改后表单提交数据正确
- [ ] 5.6 测试只读模式下编辑器不可编辑
