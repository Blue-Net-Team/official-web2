## Why

系统已有咨询群二维码的 API 实现，但缺少管理后台 UI，管理员无法便捷地管理二维码。同时，考核群二维码的管理功能尚未实现，无法支持按方向、轮次管理考核群二维码。

## What Changes

### 后端变更
- 新增管理员获取咨询群二维码列表 API
- 新增考核群二维码 CRUD API（创建、列表、更新、删除）
- 扩展 `QrcodeAppService` 支持考核群二维码操作

### 前端变更
- 新增管理后台二维码管理页面 `/admin/qrcode`
- 实现 Tab 切换（咨询群/考核群）
- 实现咨询群二维码列表展示、上传、编辑、删除
- 实现考核群二维码列表展示、上传、编辑、删除（支持方向、轮次筛选）

## Capabilities

### New Capabilities
- `qrcode-management`: 二维码管理后台功能，统一管理咨询群和考核群二维码

### Modified Capabilities
- `consultation-qrcode`: 补充管理员列表 API

## Impact

### 后端
- `AdminQrcodeController.java` - 扩展考核群二维码管理接口
- `QrcodeAppService.java` - 扩展考核群二维码操作方法
- `QrcodeDomainService.java` - 扩展考核群二维码领域逻辑
- `QrcodeResult.java` - 扩展返回字段（方向、轮次、共用标识）

### 前端
- 新增 `src/app/admin/qrcode/page.tsx` - 二维码管理主页面
- 新增 `src/app/admin/qrcode/QrcodeDrawer.tsx` - 二维码编辑抽屉组件
- 扩展 `src/apis/services/qrcode.service.ts` - 新增管理端 API 调用

### 数据库
- 无变更（表结构已预留）
