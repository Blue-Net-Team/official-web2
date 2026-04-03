## 1. 依赖安装

- [x] 1.1 在 `src/frontend/` 安装 `react-easy-crop` 依赖

## 2. AvatarCropModal 组件开发

- [x] 2.1 创建 `components/Profile/AvatarCropModal/index.tsx` 组件
- [x] 2.2 使用 antd `Modal` 作为弹窗容器，`react-easy-crop` 的 `Cropper` 作为裁剪 UI
- [x] 2.3 实现圆形裁剪框（`cropShape="round"`），1:1 比例（`aspect={1}`）
- [x] 2.4 实现 `getCroppedCanvas` 工具函数：根据 crop 区域参数用 Canvas 输出裁剪后的 Blob
- [x] 2.5 Props 设计：`open`, `imageSrc`（objectURL）, `onConfirm(blob: Blob)`, `onCancel()`
- [x] 2.6 确认按钮触发 Canvas 裁剪 → 输出 Blob → 调用 `onConfirm`
- [x] 2.7 创建 `AvatarCropModal` 的样式文件

## 3. ProfileSidebar 组件改造

- [x] 3.1 添加 `'use client'` 指令
- [x] 3.2 Props 新增 `onAvatarUpdate?: () => void` 回调
- [x] 3.3 添加隐藏 `<input type="file" accept="image/jpeg,image/png,image/gif,image/webp">`
- [x] 3.4 添加 state：`uploading`（上传中）、`cropImageSrc`（裁剪图片 URL）、`cropModalOpen`
- [x] 3.5 将 `.avatarRing` 区域改为可点击（`cursor: pointer` + `onClick` 触发 file input click）
- [x] 3.6 实现 hover 遮罩效果：`.avatarRing:hover` 时叠加半透明遮罩 + 编辑图标（CSS 实现）
- [x] 3.7 移除或简化原有的 `.avatarEdit` 小按钮（改为遮罩内的元素或移除）
- [x] 3.8 实现文件校验逻辑：类型检查 + 大小检查（≤5MB），失败用 `message.error` 提示
- [x] 3.9 校验通过后：生成 objectURL 设置到 `cropImageSrc`，打开裁剪弹窗
- [x] 3.10 裁剪确认回调：调用 `fileService.uploadAvatar(blob)`，成功后 `onAvatarUpdate`
- [x] 3.11 上传失败时 `message.error` 提示，关闭弹窗，恢复状态
- [x] 3.12 上传中状态：头像区域显示 Spinner，禁用点击

## 4. 父组件对接

- [x] 4.1 在 `page.tsx` 中为 `ProfileSidebar` 传递 `onAvatarUpdate={loadData}` prop

## 5. 验证

- [x] 5.1 hover 头像时显示半透明遮罩和编辑图标
- [x] 5.2 点击头像弹出文件选择对话框
- [x] 5.3 选择图片后弹出圆形裁剪弹窗
- [x] 5.4 裁剪确认后上传成功，头像刷新
- [x] 5.5 文件类型/大小大小校验正常拦截
- [x] 5.6 上传中显示 loading，完成后恢复
