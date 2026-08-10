## 1. 后端：暴露内推码字段

- [x] 1.1 在 `UserInfoResult` record 中新增 `internalReferralCode` 字段
- [x] 1.2 在 `UserInfo` DTO 中新增 `internalReferralCode` 字段
- [x] 1.3 更新 `UserInfoResponseConverter.toDTO()` 透传该字段
- [x] 1.4 更新 `UserInfoAppServiceImpl.getMyInfo()` 从 `User` 实体读取内推码
- [x] 1.5 为 `UserInfoAppServiceImpl.getMyInfo()` 补充/更新单元测试，验证返回内推码
- [x] 1.6 运行后端测试，确认无回归

## 2. 前端基础准备

- [x] 2.1 安装 `qrcode.react` 依赖
- [x] 2.2 将海报底图 `inf.png` 复制到 `public/referral-posters/inf.png`
- [x] 2.3 创建海报模板配置文件 `src/frontend/src/components/Profile/ReferralPoster/poster-templates.ts`，填入初版坐标
- [x] 2.4 更新 `profile.dto.ts` / `type.ts` 中的用户信息类型，增加 `internalReferralCode`

## 3. 报名页支持 URL 预填内推码

- [x] 3.1 更新 `useEnrollForm.ts`，读取 `searchParams.get('ref')` 并自动大写后填入表单
- [x] 3.2 更新 `EnrollForm.tsx`，为内推码输入框增加大写转换与正则校验
- [x] 3.3 运行前端类型检查与现有测试，确认报名页无回归

## 4. 内推分享弹窗组件

- [x] 4.1 创建 `ReferralPosterModal` 组件，支持展示内推码、复制链接、生成海报
- [x] 4.2 在 `ProfileSidebar` 中为成员及以上角色增加"我要内推"入口
- [x] 4.3 处理无内推码时隐藏入口的逻辑
- [x] 4.4 手动验证：登录成员点击入口 → 弹窗展示内推码与复制链接功能正常

## 5. 海报图片生成

- [x] 5.1 创建 `useReferralPosterCanvas` hook，使用 Canvas 2D 绘制底图、二维码、内推码、内推人姓名
- [x] 5.2 将生成的 Canvas 转换为图片并支持下载
- [x] 5.3 手动验证：下载海报 → 扫码跳转报名页 → 内推码已预填
- [x] 5.4 根据实际渲染效果微调模板配置文件坐标

## 6. 海报模板设计器页面

- [x] 6.1 创建 `app/admin/referral-poster-design/page.tsx` 页面
- [x] 6.2 实现可拖拽的内推码、二维码、内推人姓名元素
- [x] 6.3 实现坐标实时显示与配置 JSON 导出
- [x] 6.4 手动验证：拖拽元素 → 导出配置 → 替换配置文件后海报生成正常

## 7. 集成验证

- [x] 7.1 端到端验证完整链路：个人主页获取内推码 → 复制链接 → 新窗口打开报名页 → 内推码自动填写 → 下载海报 → 扫码确认
- [x] 7.2 检查 3000 端口占用情况，按需启动前端 dev 服务
- [x] 7.3 运行 Playwright E2E 验证（如已有相关用例则更新，否则至少手动验证）
- [x] 7.4 后端编译打包并构建 Docker 镜像，确认无构建错误
