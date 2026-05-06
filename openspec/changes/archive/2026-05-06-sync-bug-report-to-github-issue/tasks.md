## 1. 数据库迁移与配置

### Task 1.1: 添加数据库迁移文件

#### 测试边界
- 输入条件：Flyway 迁移脚本 `V9__add_github_issue_to_bug_report.sql`
- 前置状态：数据库已存在 `tb_bug_report` 表
- 后置状态：`tb_bug_report` 表新增 `github_issue_url` 和 `github_issue_number` 字段

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常迁移 | 执行 Flyway migrate | `tb_bug_report` 新增两列，现有数据不受影响 | - |
| TC-002 | 重复迁移 | Flyway 已执行过 V9 | 幂等，不报错 | - |

#### 实现步骤
- [x] 编写 Flyway 迁移脚本
- [x] 本地执行 `./mvnw flyway:migrate` 验证

### Task 1.2: 添加 GitHub App 配置属性类

#### 测试边界
- 输入条件：`application.yml` 中 `github.app` 配置项
- 前置状态：Spring Boot 应用启动
- 后置状态：配置类正确绑定，缺失配置时同步逻辑跳过

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-003 | 完整配置 | app-id、private-key-path、owner、repo 均配置 | `GitHubAppProperties` 正确绑定，启用同步 | - |
| TC-004 | 配置缺失 | app-id 为空 | `enabled=false`，同步逻辑跳过 | - |

#### 实现步骤
- [x] 创建 `GitHubAppProperties.java`
- [x] 更新 `.env.example` 和 `application.yml`

## 2. GitHub API 客户端层

### Task 2.1: 实现 GitHub App JWT 和 Installation Token 生成

#### 测试边界
- 输入条件：GitHub App 私钥（PEM）和 App ID
- 前置状态：私钥文件存在且有效
- 后置状态：生成可用于调用 GitHub API 的 Installation Access Token

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-005 | 成功生成 Token | 有效私钥和 App ID | 返回非空 Access Token 字符串 | - |
| TC-006 | 私钥文件不存在 | 错误的私钥路径 | 抛出 `IllegalStateException` | - |
| TC-007 | 无效私钥内容 | 损坏的 PEM 文件 | 抛出 `IllegalStateException` | - |

#### 实现步骤
- [x] 编写 `GitHubAppTokenServiceTest`（红灯）
- [x] 实现 `GitHubAppTokenService`：JWT 生成（使用 `jjwt` 库或 Java 原生）+ Installation Token 换取
- [x] 运行测试确认通过（绿灯）

### Task 2.2: 实现 GitHub Issue 创建客户端

#### 测试边界
- 输入条件：Installation Access Token、仓库 owner/repo、Issue 标题和 Body
- 前置状态：Token 有效
- 后置状态：GitHub 仓库中创建新 Issue

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-008 | 成功创建 Issue | 有效参数 | 返回包含 `number` 和 `html_url` 的响应对象 | - |
| TC-009 | API 返回 401 | Token 过期 | 抛出 `RuntimeException` 并记录日志 | - |
| TC-010 | API 返回 403 | 权限不足 | 抛出 `RuntimeException` 并记录日志 | - |
| TC-011 | API 返回 422 | 参数无效（如标题为空）| 抛出 `RuntimeException` 并记录日志 | - |

#### 实现步骤
- [x] 编写 `GitHubIssueClientTest`（红灯，使用 MockRestServiceServer 模拟 GitHub API）
- [x] 实现 `GitHubIssueClient`：使用 `RestTemplate` 调用 `POST /repos/{owner}/{repo}/issues`
- [x] 运行测试确认通过（绿灯）

## 3. 领域层与应用层

### Task 3.1: 修改 BugReport 领域实体

#### 测试边界
- 输入条件：`githubIssueUrl` 和 `githubIssueNumber`
- 前置状态：BugReport 实体已存在
- 后置状态：实体支持存储和更新 GitHub Issue 信息

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-012 | 更新 GitHub Issue 信息 | url="https://...", number=42 | `getGithubIssueUrl()` 和 `getGithubIssueNumber()` 返回正确值 | - |

#### 实现步骤
- [x] 修改 `BugReport.java`，新增 `githubIssueUrl` 和 `githubIssueNumber` 字段
- [x] 新增 `updateGithubIssueInfo(String url, Integer number)` 方法
- [x] 更新 `BugReportRepository` 接口，支持按 ID 更新 GitHub Issue 信息
- [x] 更新 `BugReportDO` 和 `BugReportRepositoryConverter`

### Task 3.2: 实现 GitHub Issue 同步应用服务

#### 测试边界
- 输入条件：`BugReport` 实体（含描述、URL、环境信息、截图 fileIds）
- 前置状态：GitHub App 配置完整
- 后置状态：成功创建 GitHub Issue 并将结果回写

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-013 | 成功同步 | 完整的 BugReport 实体 | 返回 `GitHubIssueResult` 包含 number 和 url | - |
| TC-014 | GitHub API 失败 | 模拟网络异常 | 抛出异常，由调用方捕获并记录 | - |
| TC-015 | 配置未启用 | `GitHubAppProperties.enabled=false` | 返回 null，不调用 GitHub API | - |
| TC-016 | 无截图 | fileIds 为空列表 | Issue Body 中截图部分为空 | - |

#### 实现步骤
- [x] 编写 `GitHubIssueSyncServiceTest`（红灯，Mock GitHubIssueClient）
- [x] 实现 `GitHubIssueSyncService`：生成 Markdown Body → 调用 Client（携带 `labels: ["Bug"]`）→ 返回结果
- [x] 运行测试确认通过（绿灯）

### Task 3.3: 修改 BugReport 提交流程

#### 测试边界
- 输入条件：`CreateBugReportCommand`
- 前置状态：数据库事务已提交
- 后置状态：BugReport 保存成功，GitHub Issue 同步触发

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-017 | 同步成功 | 有效提交参数 | 返回 `Created` 结果包含 `githubIssueUrl` | - |
| TC-018 | 同步失败但保存成功 | 模拟 GitHub API 异常 | 返回 `Created` 结果中 `githubIssueUrl` 为 null，不抛异常给用户 | - |
| TC-019 | 配置未启用 | 无 GitHub App 配置 | 正常保存，返回结果中 `githubIssueUrl` 为 null | - |

#### 实现步骤
- [x] 编写 `BugReportAppServiceImplTest` 补充测试用例（红灯）
- [x] 修改 `BugReportAppServiceImpl.submitBugReport()`：保存后 `@Async` 调用 `githubIssueSyncService.sync()`，由异步方法内部完成 GitHub API 调用和结果回写
- [x] 更新 `BugReportResult.Created` 添加 `githubIssueUrl` 字段
- [x] 运行测试确认通过（绿灯）

## 4. API 层与 DTO

### Task 4.1: 更新 DTO 和转换器

#### 测试边界
- 输入条件：`BugReportResult.Created` 含 `githubIssueUrl`
- 前置状态：现有 DTO 不包含该字段
- 后置状态：API 响应中返回该字段

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-020 | DTO 转换 | `Created(id=1, status=PENDING, githubIssueUrl="https://...")` | `BugReportCreatedDTO` 的 `githubIssueUrl` 正确赋值 | - |

#### 实现步骤
- [x] 更新 `BugReportCreatedDTO`，新增 `githubIssueUrl` 字段
- [x] 更新 `BugReportAppConverter`
- [x] 更新 `BugReportDetailDTO`，新增 `githubIssueUrl` 和 `githubIssueNumber` 字段
- [x] 更新 `BugReportBriefDTO`，新增 `githubIssueUrl` 字段（可选，列表展示用）

## 5. 前端更新

### Task 5.1: 更新管理端 Bug 报告列表和详情

#### 测试边界
- 输入条件：API 返回含 `githubIssueUrl` 的 BugReport 数据
- 前置状态：管理端页面已存在
- 后置状态：列表和详情展示 GitHub Issue 跳转链接

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-021 | 已同步报告 | `githubIssueUrl` 不为空 | 详情页显示「查看 GitHub Issue」外链按钮 | - |
| TC-022 | 未同步报告 | `githubIssueUrl` 为空 | 不显示 GitHub Issue 相关入口 | - |

#### 实现步骤
- [x] 更新 `bug-report.dto.ts`，新增 `githubIssueUrl` 和 `githubIssueNumber` 字段
- [x] 更新管理端列表页，增加 GitHub Issue 链接列（或图标）
- [x] 更新管理端详情页，增加「查看 GitHub Issue」外链按钮
- [x] 运行前端 lint 检查

## 6. 集成与验证

### Task 6.1: 端到端验证

#### 实现步骤
- [x] 本地启动后端，提交一条测试 Bug 报告
- [x] 验证 GitHub Issue 是否自动创建
- [x] 验证 Issue Body 格式、截图链接是否正确
- [x] 验证管理端能否看到跳转链接
- [x] 移除 GitHub 配置，验证降级行为（正常保存，不报错）

### Task 6.2: 运行全量测试

#### 实现步骤
- [x] 后端：`./mvnw test`
- [x] 前端：`pnpm lint`
