## 1. 后端 — 补全二维码字段的数据链路

- [x] 1.1 `UserInfoResult` record 添加 `wechatQrcode` 字段
- [x] 1.2 `UserInfo` API DTO 添加 `wechatQrcode` 字段
- [x] 1.3 `UserInfoResponseConverter.toDTO()` 映射 `wechatQrcode`
- [x] 1.4 `UserInfoAppServiceImpl.getMyInfo()` 从 `UserVO` 取出 `wechatQrcode` 放入 `UserInfoResult`
- [x] 1.5 `UpdateProfileRequestDTO` 添加 `qrcodeFileId` 字段（nullable）
- [x] 1.6 `UserInfoCommands.UpdateProfileCommand` 添加 `qrcodeFileId` 字段
- [x] 1.7 `UserInfoRequestConverter` 映射 `qrcodeFileId`

## 2. 后端 — 更新个人资料支持二维码

- [x] 2.1 `UserDomainService.updateProfile()` 签名增加 `Long qrcodeFileId` 参数
- [x] 2.2 `UserDomainServiceImpl.updateProfile()` 实现：校验文件存在且类型为 QRCODE，调用 repository 更新
- [x] 2.3 `UserRepository` 接口添加 `updateQrcodeId(Long userId, Long qrcodeId)` 方法
- [x] 2.4 `UserRepositoryImpl` 实现 `updateQrcodeId()`（复用已有的 `userMapper.updateQrcodeId`）
- [x] 2.5 `UserInfoAppServiceImpl.updateProfile()` 透传 `qrcodeFileId` 到 domain service

## 3. 后端 — 单元测试与集成测试

- [x] 3.1 `UserInfoAppServiceImplTest`：测试 `getMyInfo()` 返回包含 `wechatQrcode`
- [x] 3.2 `UserInfoAppServiceImplTest`：测试 `updateProfile()` 成功更新二维码
- [x] 3.3 `UserInfoAppServiceImplTest`：测试 `updateProfile()` 文件不存在时抛 404
- [x] 3.4 `UserInfoAppServiceImplTest`：测试 `updateProfile()` 文件类型不匹配时抛 400
- [x] 3.5 `UserDomainServiceImplTest`：测试 `updateProfile()` 更新二维码逻辑
- [x] 3.6 `UserProfileControllerIntegrationTest`：测试 `GET /api/v1/user/info` 响应包含 `wechatQrcode`
- [x] 3.7 `UserProfileControllerIntegrationTest`：测试 `PUT /api/v1/user/info` 可更新 `qrcodeFileId`

## 4. 前端 — 类型定义与 API 更新

- [x] 4.1 `type.ts` 的 `UserInfo` 接口添加 `qrcodeFileId: number | null` 和 `wechatQrcode: string | null`
- [x] 4.2 `type.ts` 的 `MemberDetailDTO` 接口添加 `wechatQrcode: string | null`
- [x] 4.3 `profile.dto.ts` 的 `UpdateProfileRequestDTO` 添加 `qrcodeFileId?: number | null`

## 5. 前端 — ProfileSidebar 二维码展示

- [x] 5.1 `SidebarProfile` 接口添加 `wechatQrcode: string | null`
- [x] 5.2 `ProfileSidebar` 组件在基本信息（学院/专业/年级）下方新增二维码展示区域
- [x] 5.3 有二维码时显示图片，支持点击放大预览（Modal）
- [x] 5.4 无二维码时显示占位提示"暂无微信二维码"
- [x] 5.5 个人主页 `page.tsx` 确保 `profile` 数据传入 `wechatQrcode`
- [x] 5.6 成员详情 `members/[id]/page.tsx` 的 `adaptToSidebarProfile` 传入 `wechatQrcode`

## 6. 前端 — ProfileInfo 编辑表单增加二维码上传

- [x] 6.1 `ProfileInfo` 编辑表单增加二维码上传区域（位于个人简介下方）
- [x] 6.2 上传流程：点击上传 → 选择图片 → 调用 `fileService.upload(file, 'QRCODE')` → 成功后显示预览并记录 `qrcodeFileId`
- [x] 6.3 已上传二维码显示删除按钮，点击后清空 `qrcodeFileId`
- [x] 6.4 表单提交时 `handleSubmit` 将 `qrcodeFileId` 包含在请求中
- [x] 6.5 保存成功后刷新页面数据，二维码区域更新

## 7. 前端 — ProfileInfoDisplay 适配（成员详情只读展示）

- [x] 7.1 `ProfileDisplayData` 接口添加 `wechatQrcode?: string | null`
- [x] 7.2 `ProfileInfoDisplay` 在基本信息网格中增加二维码展示（或有则展示）
- [x] 7.3 成员详情 `members/[id]/page.tsx` 的 `adaptToDisplayData` 传入 `wechatQrcode`

## 8. 编译打包与端到端验证

- [x] 8.1 后端编译通过 `./mvnw clean compile package`
- [x] 8.2 构建 Docker 镜像 `bluenet-api-service:latest`
- [x] 8.3 启动 compose 基础设施
- [x] 8.4 前端检查 3000 端口，按需启动 `pnpm dev`
- [x] 8.5 Playwright 验证：个人主页可查看二维码区域
- [x] 8.6 Playwright 验证：成员详情页可查看二维码区域
- [x] 8.7 Playwright 验证：编辑模式显示二维码上传入口
