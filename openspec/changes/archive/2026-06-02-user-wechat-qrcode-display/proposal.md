## Why

用户表 `tb_user.qrcode_id` 字段及后端完整的数据链路（User → Member → MemberResult → MemberDetailDTO）已存在，但前端个人主页和成员详情页均未展示微信二维码，用户也无法在个人主页设置自己的社交二维码。打通这条已就绪的数据链路，可让成员在团队内便捷地交换微信联系方式。

## What Changes

- **后端**: `GET /api/v1/user/info` 响应新增 `wechatQrcode` 字段；`PUT /api/v1/user/info` 支持通过 `qrcodeFileId` 更新微信二维码；`UserInfoResult`、`UserInfo`、`UpdateProfileRequestDTO` 及其转换链补全二维码字段。
- **前端个人主页**: `ProfileSidebar` 新增微信二维码展示区域（有二维码时显示图片，无时显示占位提示）；`ProfileInfo` 编辑表单增加二维码上传入口（复用预签名上传，文件类型 `QRCODE`，无需裁剪）。
- **前端成员详情**: `MemberDetailDTO` 类型补全 `wechatQrcode` 字段；`ProfileSidebar` / `ProfileInfoDisplay` 展示成员的微信二维码。
- **文件权限**: 沿用现有 `FileType.QRCODE` 及公开可见权限规则，无需新增权限控制。

## Capabilities

### New Capabilities

-（无新增独立能力，属现有能力的增量扩展）

### Modified Capabilities

- `backend-user-profile`: `GET /api/v1/user/info` 需返回当前用户的微信二维码 URL；`PUT /api/v1/user/info` 需支持更新 `qrcodeFileId`。
- `frontend-user-profile`: 用户信息卡片和个人信息编辑需增加微信二维码的展示与上传能力。
- `frontend-member-profile-view`: 成员详情页需展示成员的微信二维码。

## Impact

- **后端 API**: `UserInfoResult`、`UserInfo`、`UpdateProfileRequestDTO`、`UserInfoCommands.UpdateProfileCommand`、`UserDomainService.updateProfile`、`UserRepository` 及实现类需变更。
- **前端类型**: `type.ts`（`UserInfo`、`MemberDetailDTO`）、`profile.dto.ts`（`UpdateProfileRequestDTO`）需变更。
- **前端组件**: `ProfileSidebar`、`ProfileInfo`、`ProfileInfoDisplay`、`members/[id]/page.tsx` 需变更。
- **数据库**: 无变更，沿用现有 `tb_user.qrcode_id` 及 `tb_qrcode` 关联表。
