## Context

个人主页 (`/profile`) 使用 `ProfileSidebar` 组件展示用户头像。当前头像右下角有一个编辑图标按钮（`<EditOutlined />`），但该元素是纯装饰性 `div`，没有绑定任何事件处理。后端 `POST /api/v1/file/upload/avatar` 已完整支持已登录用户头像上传并自动更新 `tb_user.avatar_id`，前端 `fileService.uploadAvatar()` 也已封装好。

当前组件树：

```
ProfilePage (page.tsx, 'use client')
  └─ ProfileSidebar  (无 'use client', 纯展示)
       └─ .avatarContainer
            ├─ .avatarRing > .avatarImg    ← 头像图片（不可点击）
            └─ .avatarEdit                 ← 装饰性编辑按钮（无事件）
```

## Goals / Non-Goals

**Goals:**
- 点击头像圆形区域即触发文件选择（整个头像区域可点击）
- Hover 时显示半透明遮罩+编辑图标，提供视觉反馈
- 选择图片后弹出圆形裁剪弹窗，用户可调整裁剪区域
- 确认裁剪后上传到后端并自动更新头像显示
- 上传过程中有明确的 loading 状态反馈

**Non-Goals:**
- 不修改后端 API（已完全支持）
- 不实现高级图片编辑（旋转、亮度、滤镜等）
- 不处理旧头像文件清理（后端当前也不处理）
- 不改变 ProfileSidebar 的整体布局和样式风格
- 不实现拖拽上传、粘贴上传

## Decisions

### Decision 1: 点击整个头像区域触发上传

**选择**: 将 `.avatarRing` / `.avatarImg` 区域设为可点击，hover 时叠加半透明遮罩 + 编辑图标文案

**替代方案**: 保留仅点击右下角小编辑按钮

**理由**: 更符合主流应用（微信、GitHub、钉钉）的头像更换交互习惯——整个头像就是操作区域，更直觉。小编辑按钮作为视觉指示可保留在遮罩中。

### Decision 2: 使用 `react-easy-crop` 实现裁剪

**选择**: `react-easy-crop` + Canvas API 裁剪输出

**替代方案**:
- `antd-img-crop`：专为 antd Upload 设计，但我们不直接用 antd Upload 包裹头像区域，适配成本高
- `react-cropper`：基于 Cropper.js，较重（~60KB）
- 纯 Canvas 手写裁剪：开发成本高

**理由**: `react-easy-crop` 是最流行的 React 裁剪库（~30KB），提供圆形裁剪支持，API 简洁，维护活跃。配合 Canvas `getCroppedCanvas()` 工具函数输出裁剪后的 Blob。

### Decision 3: 不使用 antd Upload 包裹头像区域

**选择**: 隐藏 `<input type="file">` + 点击头像区域触发

**替代方案**: 用 antd `<Upload>` 组件包裹头像区域

**理由**: 头像区域是 120x120 的圆形，需要精确的圆角裁剪和 hover 遮罩效果。antd Upload 的内部 DOM 结构会增加不必要的样式覆盖成本。隐藏 input 方案更灵活，同时视觉上实现与 antd Upload 类似的体验。

### Decision 4: 裁剪弹窗使用 antd Modal

**选择**: `antd Modal` + `react-easy-crop` Cropper 组件

**理由**: 项目已使用 antd，Modal 提供一致的弹窗体验（遮罩、动画、可访问性）。

### Decision 5: 新增独立 `AvatarCropModal` 组件

**选择**: 抽取为独立组件 `components/Profile/AvatarCropModal`

**理由**: 裁剪逻辑（加载图片、crop state、Canvas 裁剪、输出 Blob）有足够复杂度，独立组件职责清晰，也便于后续复用。

## Risks / Trade-offs

- **[Risk] 新增 npm 依赖 `react-easy-crop`** → 该库维护活跃、社区广泛使用（10k+ stars），风险可控
- **[Risk] Canvas 裁剪在某些旧浏览器可能有问题** → 目标用户是高校学生，浏览器版本普遍较新，可接受
- **[Risk] 大图裁剪可能导致 Modal 中图片加载慢** → 可在裁剪前用 Canvas 做一次尺寸压缩（限制最大 2048px）来缓解
- **[Trade-off] 不使用 antd Upload** → 丢失开箱即用的上传进度条，但头像文件通常较小（<5MB），loading spinner 足够
