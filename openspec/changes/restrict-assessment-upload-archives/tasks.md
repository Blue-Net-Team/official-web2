## 1. 常量与校验工具

- [ ] 1.1 在 `src/frontend/src/components/Assessment/QuestionDetail/utils.ts`（或同目录新建常量文件）定义 `ALLOWED_ARCHIVE_EXTENSIONS` 常量：`['zip', 'rar', '7z', 'tar', 'gz', 'tar.gz', 'bz2', 'xz']`，并导出扩展名校验函数（优先匹配复合后缀如 `.tar.gz`，回落末段扩展名；大小写不敏感）
- [ ] 1.2 在 `index.tsx` 中派生 `effectiveAllowedExtensions`（`fileContent?.allowedExtensions ?? ALLOWED_ARCHIVE_EXTENSIONS`），供 `accept`、`beforeUpload`、提示文案三处共用

## 2. beforeUpload 拦截

- [ ] 2.1 在 `beforeUpload` 中增加扩展名白名单校验：不在白名单内则 `message.error('仅支持 zip、rar、7z 等压缩包格式，请将文件压缩后再上传')` 并返回 `Upload.LIST_IGNORE`
- [ ] 2.2 在 `beforeUpload` 中增加空文件（`file.size === 0`）拦截，提示文件内容为空，返回 `Upload.LIST_IGNORE`（保留原有大小限制检查逻辑）

## 3. 文件夹拖拽检测

- [ ] 3.1 为 `draggerProps` 增加 `onDrop` 处理：通过 `e.dataTransfer.items[0].webkitGetAsEntry()` 检测 `isDirectory`，命中时 `preventDefault` 并提示「不支持上传文件夹，请压缩后上传」，阻止其进入上传流程（注意 antd 类型声明对 `webkitGetAsEntry` 的兼容写法）

## 4. 错误信息透传

- [ ] 4.1 修改 `customRequest` 的 catch：优先展示 `error.message`，过滤 `UPLOAD_ABORTED` 等内部错误码（不弹提示），无可用 message 时回退「上传失败，请重试」

## 5. 提示文案同步

- [ ] 5.1 更新 `dropHintText` 及格式说明区域：基于 `effectiveAllowedExtensions` 展示实际允许的格式，移除「所有文件格式」回退表述
- [ ] 5.2 确认 `FileUploadArea.tsx` 展示层无需改动（若文案由 props 传入则不动组件内部）

## 6. 验证

- [ ] 6.1 确认 3000 端口占用情况：已占用则使用现有前端服务，未占用则 `pnpm dev` 启动
- [ ] 6.2 进入一道 file_upload 考题页面，Playwright/浏览器验证：选择 `.zip` 正常上传；选择 `.docx`/无扩展名文件被拦截并提示；拖入文件夹（Chromium）提示「不支持上传文件夹」；验证错误透传（可用断网或构造 confirm 失败场景）
- [ ] 6.3 验证重新提交（resubmitting 状态）路径同样生效（draggerProps 共用）
