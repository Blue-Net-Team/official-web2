## 后端任务

### 1. 咨询群二维码管理接口扩展

- [ ] 1.1 新增管理员获取咨询群二维码列表接口 `GET /api/v1/admin/qrcodes/consultation`
- [ ] 1.2 新增管理员更新咨询群二维码接口 `PUT /api/v1/admin/qrcodes/consultation/{id}`
- [ ] 1.3 编写单元测试

### 2. 考核群二维码管理接口实现

- [ ] 2.1 创建 `AssessmentQrcodeDTO` 数据传输对象
- [ ] 2.2 创建 `CreateAssessmentQrcodeRequestDTO` 请求对象
- [ ] 2.3 创建 `UpdateAssessmentQrcodeRequestDTO` 请求对象
- [ ] 2.4 扩展 `QrcodeResult` 支持考核群字段（direction、epoch、isShared）
- [ ] 2.5 扩展 `QrcodeAppService` 添加考核群二维码操作方法
- [ ] 2.6 扩展 `QrcodeDomainService` 添加考核群二维码领域逻辑
- [ ] 2.7 扩展 `QrcodeRepository` 添加按方向、轮次查询方法
- [ ] 2.8 在 `AdminQrcodeController` 添加考核群二维码 CRUD 接口
  - `GET /api/v1/admin/qrcodes/assessment` - 列表（支持筛选）
  - `POST /api/v1/admin/qrcodes/assessment` - 创建
  - `PUT /api/v1/admin/qrcodes/assessment/{id}` - 更新
  - `DELETE /api/v1/admin/qrcodes/assessment/{id}` - 删除
- [ ] 2.9 编写单元测试

## 前端任务

### 3. API 服务层

- [ ] 3.1 扩展 `qrcode.service.ts` 添加管理端 API 调用
  - `getConsultationQrcodesAdmin()` - 获取咨询群列表
  - `updateConsultationQrcode()` - 更新咨询群
  - `getAssessmentQrcodes()` - 获取考核群列表
  - `createAssessmentQrcode()` - 创建考核群
  - `updateAssessmentQrcode()` - 更新考核群
  - `deleteAssessmentQrcode()` - 删除考核群
- [ ] 3.2 添加相关 TypeScript 类型定义

### 4. 二维码管理页面

- [ ] 4.1 创建 `/admin/qrcode/page.tsx` 主页面
- [ ] 4.2 实现 Tab 切换组件（咨询群/考核群）
- [ ] 4.3 实现咨询群二维码列表展示
- [ ] 4.4 实现考核群二维码列表展示
- [ ] 4.5 实现考核群筛选器（方向、轮次）
- [ ] 4.6 实现上传按钮和交互

### 5. 二维码编辑 Drawer

- [ ] 5.1 创建 `QrcodeDrawer.tsx` 组件
- [ ] 5.2 实现咨询群编辑表单（图片上传）
- [ ] 5.3 实现考核群编辑表单（方向、轮次、共用、图片上传）
- [ ] 5.4 实现共用开关逻辑（开启时清空方向）
- [ ] 5.5 实现图片预览功能

### 6. 删除确认

- [ ] 6.1 实现删除确认对话框
- [ ] 6.2 实现删除成功后刷新列表

## 测试任务

- [ ] 7.1 后端接口集成测试
- [ ] 7.2 前端页面功能测试
- [ ] 7.3 验证咨询群二维码管理流程
- [ ] 7.4 验证考核群二维码管理流程
- [ ] 7.5 验证筛选功能
