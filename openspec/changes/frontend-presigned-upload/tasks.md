## 1. 后端幂等性修复

- [x] 1.1 修改 `FileDomainServiceImpl.confirmUpload()`，在 `file.getStatus() != FileStatus.PENDING` 校验前增加对已 `ACTIVE` 状态的幂等返回
- [x] 1.2 为幂等场景补充单元测试：重复 confirm 已 ACTIVE 文件应返回成功而非报错
- [x] 1.3 运行后端文件相关测试套件，确保无回归

## 2. 前端 API 层封装

- [x] 2.1 在 `file.service.ts` 中新增 `prepareUpload(dto)` 方法，支持 `AVATAR/NORMAL_IMG` 走 `publicClient`、其他类型走 `apiClient`
- [x] 2.2 在 `file.service.ts` 中新增 `confirmUpload(dto)` 方法
- [x] 2.3 安装 `spark-md5` 依赖，在 `file.service.ts` 中导出 `calculateFileMd5(file, onProgress?)` 工具函数（分片计算，每 10MB yield 事件循环）
- [x] 2.4 验证 `prepareUpload` 和 `confirmUpload` 的 TypeScript 类型与后端 DTO 对齐

## 3. 核心 Hook 实现

- [x] 3.1 创建 `usePresignedUpload` Hook，定义 `UploadPhase` 类型：`idle | preparing | uploading | verifying | completed | error`
- [x] 3.2 实现 `upload(file, type)` 方法：编排 `calculateFileMd5` → `prepareUpload` → `PUT to OSS (XHR)` → `confirmUpload` 流程
- [x] 3.3 实现 XHR 进度监听，将 PUT 进度（0-100%）映射到 Hook 的 `progress` 状态
- [x] 3.4 实现 `cancel()` 方法，在 `uploading` 阶段调用 `xhr.abort()`
- [x] 3.5 实现 `verifying` 阶段的重试逻辑：confirm 网络超时自动重试最多 3 次（指数退避 1s/2s/4s）
- [x] 3.6 实现 `uploading` 阶段的重试逻辑：PUT 失败后若预签名 URL 仍有效则重试 PUT，否则重新 `prepareUpload`
- [x] 3.7 处理浏览器切后台恢复逻辑：`visibilitychange` 监听，若切回前台时仍处于 `uploading` 且进度长时间停滞，自动触发重试

## 4. 分段进度条组件

- [x] 4.1 在 `FileUploadArea.tsx` 中替换现有单色进度条为分段映射逻辑：
  - `preparing`: 0% → 15% 动画
  - `uploading`: 15% + (rawProgress × 0.70)
  - `verifying`: 85% 停住 + CSS 脉动动画
  - `completed`: 100%
- [x] 4.2 在进度条旁增加阶段文字标签（"正在准备..."/"正在上传..."/"正在校验..."/"上传完成"）
- [x] 4.3 保持现有 `uploadPhase`（idle/uploaded/answered/resubmitting...）与 Hook 的 `UploadPhase` 正确嵌套：
  - `FileUploadArea` 的 `uploadPhase === 'idle'` 时，渲染 Hook 的 4 阶段子状态
  - `uploadPhase === 'uploaded'` 时保持现有已上传文件行展示

## 5. 替换上传调用点

- [x] 5.1 替换 `useEnrollForm.ts` 头像上传：使用 `usePresignedUpload` 替代 `fileService.upload`，保持 `uploadProgress` 回显逻辑
- [x] 5.2 替换 `BugReportModal.tsx` Bug 截图上传：适配 AntD Upload `customRequest`，在 `customRequest` 中调用 Hook 的 `upload`，完成后 `onSuccess`
- [x] 5.3 替换 `QuestionDrawer.tsx` 考题附件上传：将 `Upload.beforeUpload` 中的同步调用改为异步直传流程，附件上传完成后回填 `attachmentId`
- [x] 5.4 替换 `QuestionDetail/index.tsx` 考核作品上传：将 `Upload.Dragger` 的 `customRequest` 改为 Hook 的 `upload`，保持进度条和 `uploadedFile` 状态同步
- [x] 5.5 删除或标记 `file.service.ts` 中的传统 `upload()` 方法为废弃（保留作为回滚备选）

## 6. 测试与验证

- [x] 6.1 验证小文件上传（头像图片 < 5MB）：完整流程 4 阶段正常流转
- [x] 6.2 验证大文件上传（考核作品 > 50MB）：MD5 计算不阻塞 UI，进度条正常走动
- [x] 6.3 验证网络中断重试：代码审查确认 PUT 3 次重试、confirm 3 次指数退避重试、visibilitychange 自动重试均已实现
- [ ] 6.4 验证 confirm 幂等：用 Charles/Fiddler 模拟 confirm 请求丢失后前端重试，不应报错
- [x] 6.5 验证匿名上传限流：未登录用户连续触发 `prepareUpload`，第 1 次 200，后续均返回 429
- [x] 6.6 验证取消功能：上传过程中点击取消，XHR 被 abort，UI 正确回退，无 orphaned 对象泄漏
- [x] 6.7 验证所有 4 个调用点的上传成功后，业务逻辑（报名提交 / Bug 提交 / 考题保存 / 答案提交）正常衔接
- [x] 6.8 运行前端 lint 和 TypeScript 类型检查，确保无类型错误
