## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本添加 `tb_qrcode` 表新字段（epoch, direction, is_shared）
- [x] 1.2 执行迁移验证表结构（测试环境自动验证通过）

## 2. 后端枚举扩展

- [x] 2.1 修改 `QrcodeType.java` 枚举，添加 `CONSULTATION` 和 `ASSESSMENT` 类型
- [x] 2.2 保留 `GROUP` 并标记 `@Deprecated`（向后兼容）

## 3. 后端实体扩展

- [x] 3.1 修改 `Qrcode.java` 实体，添加 epoch/direction/isShared 字段

## 4. 后端领域服务扩展

- [x] 4.1 在 `QrcodeDomainService` 添加获取咨询群列表方法
- [x] 4.2 在 `QrcodeDomainService` 添加删除咨询群方法（含关联文件删除）

## 5. 后端 DTO 定义

- [x] 5.1 创建 `ConsultationQrcodeDTO.java`（id, fileId）

## 6. 后端 API 控制器

- [x] 6.1 创建 `QrcodeController.java`
- [x] 6.2 实现公开接口 `GET /api/v1/qrcodes/consultation`
- [x] 6.3 实现管理接口 `POST /api/v1/admin/qrcodes/consultation`（需 ADMIN 权限）
- [x] 6.4 实现管理接口 `DELETE /api/v1/admin/qrcodes/consultation/{id}`（需 ADMIN 权限）

## 7. 后端文件上传集成

- [x] 7.1 修改 `FileServiceImpl.uploadQrcode()` 支持 CONSULTATION 类型（已支持）

## 8. 前端 API 服务

- [x] 8.1 创建 `qrcode.service.ts` 添加获取咨询群列表方法

## 9. 前端组件开发

- [x] 9.1 创建 `ConsultationQrcode` 客户端组件
- [x] 9.2 实现列表展示 + 悬浮预览交互

## 10. 前端页面集成

- [x] 10.1 在 `enroll/page.tsx` 集成 `ConsultationQrcode` 组件
- [x] 10.2 调整布局位置（表单下方）

## 11. 编译验证

- [x] 11.1 后端编译通过（无错误）
- [x] 11.2 前端 lint 通过（无错误）

## 12. 单元测试与集成测试

- [x] 12.1 创建 `QrcodeDomainServiceImplTest` 领域服务单元测试（11个测试用例）
- [x] 12.2 创建 `QrcodeControllerIntegrationTest` 控制器集成测试（12个测试用例）
- [x] 12.3 所有测试通过（23/23）

## 13. 手动验证

- [x] 13.1 上传二维码文件到 MinIO（qrimage.png → qrcode bucket）
- [x] 13.2 插入数据库测试数据（tb_file + tb_qrcode）
- [x] 13.3 验证后端 API 返回正确数据（GET /api/v1/qrcodes/consultation）
- [x] 13.4 验证前端页面展示咨询群二维码组件
- [x] 13.5 验证悬浮预览功能正常工作

**注意事项**：
- 数据库枚举字段需使用小写值（如 `consultation` 而非 `CONSULTATION`）
- 前端组件需使用 `API_BASE_URL` 构建完整后端 URL
