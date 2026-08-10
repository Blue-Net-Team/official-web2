## Why

内推码功能目前已完成后端生成与报名表单存储，但成员无法获知自己的内推码，导致"报名页有输入框、却没人能把码分享出去"的闭环断裂。通过在个人主页提供"我要内推"入口，成员可以一键获取专属内推链接和可下载的海报图片，使内推码真正可用。

## What Changes

- 在后端用户资料接口中暴露当前登录用户的 `internalReferralCode`。
- 在前端个人主页（`/profile`）增加"我要内推"入口，点击后弹窗展示：
  - 内推码明文
  - 可复制分享链接（`/enroll?ref=ABCDEF12`）
  - 基于海报模板生成的带二维码海报图片（二维码扫描后跳转报名页并自动填写内推码）
- 在报名页读取 URL 中的 `?ref=` 参数，自动填入"内推码"字段，并对输入框增加大写与格式校验。
- 新增海报模板配置与海报模板设计器页面，方便后续更换模板时精调动态元素坐标。

## Capabilities

### New Capabilities

- `referral-poster-generation`：基于模板底图与动态内容（内推码、二维码、内推人姓名）生成可下载的海报图片，并提供模板坐标调试能力。

### Modified Capabilities

- `backend-user-profile`：当前登录用户资料响应中新增 `internalReferralCode` 字段，仅返回用户自己的内推码。
- `frontend-enroll-page`：报名页支持通过 URL query 参数 `ref` 预填内推码，并在前端对内推码输入做格式与大写校验。
- `frontend-user-profile`：个人主页增加"我要内推"按钮与分享弹窗。

## Impact

- 后端：`UserInfoResult`、`UserInfo` DTO 及对应转换器新增字段；无新接口、无权限变化。
- 前端：新增 `ReferralPosterModal` 组件、海报模板配置文件、海报设计器页面；安装 `qrcode.react` 依赖。
- 静态资源：海报模板底图从 `src/assets/inf.png` 迁移到 `public/referral-posters/inf.png` 以便 Canvas 直接加载。
- 数据库：无变更。
