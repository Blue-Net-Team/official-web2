# 实施任务清单

> TDD 顺序：单元测试 → 领域/应用层 → 集成测试 → Controller。禁止修改 `PermissionScanner`。

## 1. 后端：单元测试先行（红灯）

- [x] 1.1 编写 `FileDomainServiceImplTest` 新增用例：`checkDownloadPermission` 对 `ENROLL_FORM` 类型放行（匿名用户可下载），确认此时落入 `default` 分支抛 `Forbidden`（红灯）
- [x] 1.2 编写 `EnrollFormAppService` 单元测试（Mock FileRepository/FileDomainService）：`getCurrentEnrollForm` 有表返回 DTO、无表返回空；`setEnrollForm` 各分支——文件不存在抛 `DataNotFound`、类型非 ENROLL_FORM 抛参数异常、状态非 ACTIVE 抛异常、扩展名非 pdf/doc/docx 抛异常、正常替换时旧文件被 `deleteFileById`、同 id 幂等不删、首次设置无旧文件不删；`deleteEnrollForm` 有表删除、无表抛 `DataNotFound`

## 2. 后端：领域层与基础设施

- [x] 2.1 `FileType` 枚举新增 `ENROLL_FORM("enroll-form", "报名表")`
- [x] 2.2 `FileDomainServiceImpl.checkDownloadPermission()`：将 `ENROLL_FORM` 加入 `NORMAL_IMG, QRCODE` 公开空分支（绿灯 1.1）
- [x] 2.3 `FileRepository` 接口新增 `Optional<File> findLatestByType(FileType type)`；`FileRepositoryImpl` 实现（type + status=ACTIVE，id 降序取第一条）；`FileMapper` 接口与 `FileMapper.xml` 新增对应查询
- [x] 2.4 `FileMapper.xml` 的 `selectOrphanFiles` ACTIVE 分支增加 `AND f.type != 'enroll-form'` 类型排除

## 3. 后端：应用层

- [x] 3.1 新建 `EnrollFormAppService` 接口与 `EnrollFormAppServiceImpl`：`getCurrentEnrollForm()`、`setEnrollForm(fileId)`（事务内先校验新文件→后删旧文件，见 design D3）、`deleteEnrollForm()`（绿灯 1.2）
- [x] 3.2 新增应用层 Result/DTO（`EnrollFormResult`：`fileId`、`createdAt`）与 Command（`SetEnrollFormCommand`）
- [x] 3.3 扩展名校验逻辑：仅允许 pdf/doc/docx（从 `File.name` 提取扩展名，大小写不敏感）

## 4. 后端：集成测试

- [x] 4.1 `FileRepositoryImplIntegrationTest`：`findLatestByType` 语义——多条同类型取 id 最大、PENDING/REJECTED 不返回、无记录返回空
- [x] 4.2 `OrphanFileCleanupIntegrationTest`：ACTIVE 的 `enroll-form` 文件无业务引用时**不被**清理；其他类型孤儿行为不变（守护本次最危险改动）
- [x] 4.3 `EnrollFormAppServiceImplIntegrationTest`：完整替换流程（设置→替换→旧文件记录与对象删除）、删除流程

## 5. 后端：Controller 层

- [x] 5.1 新建 `EnrollFormController`：`GET /api/v1/enroll-form`，`@RequiresPermission` PUBLIC，返回 `ResponseMessage<EnrollFormDTO>`（无表时 data 为 null）
- [x] 5.2 新建 `AdminEnrollFormController`：`POST /api/v1/admin/enroll-form?fileId=`（`admin:enroll-form:update`，PROTECTED）与 `DELETE /api/v1/admin/enroll-form`（`admin:enroll-form:delete`，PROTECTED）；权限值全局搜索确认不重复
- [x] 5.3 新增 API 层 DTO 与 Converter（`EnrollFormDTO`/`EnrollFormResponseConverter`），含 Swagger 注解
- [x] 5.4 Controller 集成测试：公开接口匿名可访问、无表返回 null；管理接口无权限 403、完整替换/删除链路

## 6. 后端：编译打包与镜像

- [x] 6.1 `mvnw clean compile package` 通过（全量测试绿，828 条）
- [x] 6.2 `docker build -t bluenet-api-service:latest -f docker/api-service.Dockerfile .` 并运行容器（最终形态：`bluenet-api-service` 由 `docker compose -p bluenet --profile app` 管理，挂 `docker/.env`，接 `bluenet_network`）

## 7. 前端：API 层

- [x] 7.1 `apis/services` 新增 `enroll-form.service.ts`：`getCurrent()`（公开）、`setEnrollForm(fileId)`、`deleteEnrollForm()`；类型定义加入 `apis/schema/type`

## 8. 前端：enroll 报名页

- [x] 8.1 新建 `components/Enroll/EnrollFormDownloadCard`：「报名表」卡片（下载按钮 `<a href={API_BASE_URL/file/download/{fileId}}>` + 提示文案「填写完成后请下载打印本报名表，并在面试时带到实验室」）；接口返回 null 时 `return null`；加载中 Spin；视觉风格对齐 `ConsultationQrcode` 卡片
- [x] 8.2 enroll 页挂载：桌面端左侧栏置于 `DirectionSidebar` 与 `ConsultationQrcode` 之间；移动端 `max-lg` 区域表单下方单独渲染一份

## 9. 前端：admin 管理页

- [x] 9.1 新建 `/admin/enroll-form` 页面：状态区（是否已上传 + 上传时间）+【更新】（`Upload.Dragger`，accept 限 pdf/doc/docx → prepare-upload → 直传 → confirm-upload → set 接口 → 刷新）+【删除】（确认弹窗 → delete 接口 → 刷新）；上传/确认过程 loading 反馈
- [x] 9.2 `AdminNav` 的 `menuConfig` 新增 `key: 'enrollForm'`、label「报名表管理」、path `/admin/enroll-form`、`minLevel: 2`、图标 `FileTextOutlined`（已导入），位置紧邻「报名管理」

## 10. 端到端验证

- [x] 10.1 检查 3000 端口占用（占用则用现有 dev 服务，禁止 `pnpm dev`/`pnpm build`）
- [x] 10.2 Playwright 验证：admin 上传报名表 → enroll 页出现下载卡片（桌面 + 移动端断点）→ 点击下载拿到文件 → admin 更新替换 → 旧文件不可下载 → admin 删除 → enroll 页卡片消失
- [x] 10.3 验证权限：未登录访客可下载；低等级账号侧边栏无入口且直访接口 403
