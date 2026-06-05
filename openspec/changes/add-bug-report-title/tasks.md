## 1. 数据库迁移

- [ ] 1.1 在 `tb_bug_report` 表新增 `title VARCHAR(200) NOT NULL` 字段
- [ ] 1.2 编写迁移脚本将历史数据的 `description` 直接回填到 `title`（超长部分由 `VARCHAR(200)` 自然截断）

## 2. 后端领域层与基础设施

- [ ] 2.1 `BugReport` 实体新增 `title` 字段及创建校验（必填、最多 100 字符）
- [ ] 2.2 `BugReportDO` 新增 `title` 字段
- [ ] 2.3 `BugReportRepositoryConverter` 添加 `title` 字段映射
- [ ] 2.4 `BugReportCommands.CreateBugReportCommand` 新增 `title` 字段并 trim

## 3. 后端应用层与接口层

- [ ] 3.1 `BugReportResult.Detail` 与 `BugReportResult.Brief` 新增 `title` 字段
- [ ] 3.2 `BugReportAppServiceImpl.submitBugReport()` 将 `title` 传入 `BugReport.create()`
- [ ] 3.3 `BugReportAdminAppServiceImpl` 在 `toBriefResult`/`toDetailResult` 中返回 `title`
- [ ] 3.4 `CreateBugReportRequestDTO` 新增 `title`（`@NotBlank`、`@Size(max = 100)`）
- [ ] 3.5 `BugReportDetailDTO` 与 `BugReportBriefDTO` 新增 `title` 字段
- [ ] 3.6 `BugReportRequestConverter` 在 DTO → Command 时携带 `title`
- [ ] 3.7 `BugReportResponseConverter` 在 Result → DTO 时携带 `title`
- [ ] 3.8 `GitHubIssueSyncService` 改用 `bugReport.getTitle()` 生成 Issue 标题，缺失时降级使用 description

## 4. 后端测试

- [ ] 4.1 更新 `BugReportAppServiceImplTest`：构造 command 时包含 `title`，并断言保存的实体包含 `title`
- [ ] 4.2 更新 `GitHubIssueSyncServiceTest`：构造 BugReport 时包含 `title`，断言 Issue 标题使用 `title`
- [ ] 4.3 在 `GitHubIssueSyncServiceTest` 中补充"title 缺失时降级使用 description"的测试

## 5. 前端用户端

- [ ] 5.1 `bug-report.dto.ts` 的 `CreateBugReportRequestDTO` 新增 `title`
- [ ] 5.2 `BugReportModal.tsx` 新增"Bug 标题"输入框（必填、maxLength={100}、showCount）
- [ ] 5.3 提交时将 `title` 携带到请求体

## 6. 前端管理端

- [ ] 6.1 `bug-report.dto.ts` 的 `BugReportListItemDTO` 与 `BugReportDetailDTO` 新增 `title`
- [ ] 6.2 `admin/bug-report/page.tsx` 列表页将主展示列从"描述"改为"标题"
- [ ] 6.3 `admin/bug-report/page.tsx` 详情页在"问题描述"上方新增"标题"展示区块

## 7. 文档与验证

- [ ] 7.1 更新 `docs/数据库设计.md` 中 `tb_bug_report` 表结构，补充 `title` 字段说明
- [ ] 7.2 后端编译打包通过
- [ ] 7.3 前端类型检查通过
