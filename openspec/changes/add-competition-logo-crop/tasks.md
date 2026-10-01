# Tasks: 竞赛 Logo 上传前裁剪

## 1. 通用裁剪组件 ImageCropModal

- [ ] 1.1 将 `src/frontend/src/components/Profile/AvatarCropModal/index.tsx` 重构迁移至 `src/frontend/src/components/common/ImageCropModal/index.tsx`，组件更名 `ImageCropModal`，保留 react-easy-crop 的 React 19 兼容 cast 写法
- [ ] 1.2 参数化裁剪输出：`getCroppedBlob` 的 `maxSize` 改为由 props 传入，新增 `outputSize`（默认 512）、`aspect`（默认 1）；aspect ≠ 1 时画布高 = outputSize / aspect
- [ ] 1.3 在 `components/common/index.ts` 中导出 `ImageCropModal`
- [ ] 1.4 删除原 `components/Profile/AvatarCropModal/` 目录及 `Profile/index.ts` 中的相关导出

## 2. 头像消费方适配（行为不变）

- [ ] 2.1 修改 `src/frontend/src/components/Profile/ProfileSidebar/index.tsx`，引用 `common/ImageCropModal`，不传新 props（保持 512 正方形输出，外部行为不变）
- [ ] 2.2 本地验证头像上传/裁剪链路正常（dev 服务或构建检查）

## 3. 竞赛 Logo 裁剪接入

- [ ] 3.1 修改 `src/frontend/src/app/admin/competition/CompetitionDrawer.tsx`：logo 的 Upload `beforeUpload` 拦截为读取文件 objectURL、打开 `ImageCropModal`（`outputSize=256, aspect=1`）、return false 阻止默认上传
- [ ] 3.2 实现裁剪确认回调：blob 包装为 File 后调用 `fileService.upload(file, 'NORMAL_IMG')`，成功回填 `setLogoFileId` 与 `form.setFieldValue('logoFileId')`，带 uploading 状态
- [ ] 3.3 实现取消回调：关闭弹窗、不修改 `logoFileId`
- [ ] 3.4 确认 cover 封面上传流程未受影响（仍直传原图）

## 4. 验证

- [ ] 4.1 `pnpm build`（或已有 dev 服务下浏览器验证）通过，无类型错误
- [ ] 4.2 Playwright 端到端：管理员新建竞赛，选择非方形 logo → 弹出裁剪弹窗 → 确认 → 保存 → 公开竞赛列表卡片 logo 渲染为正方形、宽度一致
- [ ] 4.3 Playwright 回归：个人中心头像上传/裁剪/保存链路正常
