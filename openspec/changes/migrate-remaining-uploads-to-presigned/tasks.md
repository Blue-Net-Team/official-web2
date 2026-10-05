# Tasks: migrate-remaining-uploads-to-presigned

## 1. 迁移（6 处，模板同构：usePresignedUpload 替换 fileService.upload）

- [ ] 1.1 VenueDrawer：场地图片迁移到预签名直传（NORMAL_IMG），失败时 catch 提示并复位上传状态
- [ ] 1.2 EquipmentDrawer：设备图片迁移到预签名直传（NORMAL_IMG）
- [ ] 1.3 CompetitionDrawer：竞赛封面 cover 迁移到预签名直传（NORMAL_IMG，保持无裁剪直传语义）
- [ ] 1.4 QrcodeDrawer：管理端二维码迁移到预签名直传（QRCODE）
- [ ] 1.5 ProfileInfo：微信二维码迁移到预签名直传（QRCODE）
- [ ] 1.6 AchievementDrawer：成就附件迁移到预签名直传（文件类型保持 NORMAL_IMG 不变）

## 2. 清理

- [ ] 2.1 从 `file.service.ts` 删除 `upload` 方法及其 multipart 相关注释
- [ ] 2.2 全局搜索确认前端无 `fileService.upload` 残留引用

## 3. 验证

- [ ] 3.1 `tsc --noEmit`、`next lint`、`prettier --check` 全绿
- [ ] 3.2 Playwright 登录后逐处回归：场地/设备/竞赛封面/二维码/成就附件上传成功且 fileId 回填；个人资料微信二维码上传成功；失败路径提示正常
