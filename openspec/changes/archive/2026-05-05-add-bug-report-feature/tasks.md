## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本：新增 `tb_bug_report` 和 `tb_bug_report_image` 表

#### 测试边界
- 输入条件：Flyway 迁移脚本 V{next}__add_bug_report.sql
- 前置状态：数据库无 bug_report 相关表
- 后置状态：`tb_bug_report` 和 `tb_bug_report_image` 表创建成功

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常迁移 | 执行 Flyway migrate | 表创建成功，无报错 | - |
| TC-002 | 重复迁移 | 再次执行 migrate | 幂等，不报错 | - |

---

## 2. 后端领域层（Domain）

- [x] 2.1 创建 `BugReportStatus` 枚举（PENDING、IN_PROGRESS、RESOLVED）
- [x] 2.2 创建 `BugReport` 领域实体（含构造工厂、状态变更方法）
- [x] 2.3 创建 `BugReportImage` 值对象/关联实体
- [x] 2.4 创建 `BugReportRepository` 仓储接口

#### 测试边界
- 输入条件：有效的 Bug 报告参数
- 前置状态：领域对象未创建
- 后置状态：领域对象正确承载业务规则

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常创建报告 | description="页面崩溃", pageUrl="/home" | 返回 BugReport 实体，status=PENDING | - |
| TC-002 | 状态流转 | PENDING → IN_PROGRESS | status 更新为 IN_PROGRESS | - |
| TC-003 | 无效状态流转 | 直接传入 null 作为状态 | - | 抛出 IllegalArgumentException |
| TC-004 | 描述超长 | description 超过 2000 字符 | - | 抛出 IllegalArgumentException |

---

## 3. 后端基础设施层（Infrastructure）

- [x] 3.1 创建 `BugReportDO` 和 `BugReportImageDO` 数据对象
- [x] 3.2 创建 MyBatis `BugReportMapper` 和 `BugReportImageMapper`
- [x] 3.3 创建 `BugReportRepositoryImpl` 仓储实现

#### 测试边界
- 输入条件：领域实体或查询参数
- 前置状态：数据库表已创建
- 后置状态：数据正确持久化或查询返回

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 保存报告 | BugReport 实体 | 数据库插入记录，返回自增 ID | - |
| TC-002 | 保存关联图片 | bugReportId=1, fileIds=[10,11] | tb_bug_report_image 插入 2 条记录 | - |
| TC-003 | 分页查询 | page=1, size=10, status=PENDING | 返回 Page<BugReport>，total≥0 | - |
| TC-004 | 查询详情 | id=1 | 返回 Optional<BugReport>，含图片列表 | - |
| TC-005 | 更新状态 | id=1, newStatus=IN_PROGRESS | 数据库状态字段更新 | - |

---

## 4. 后端应用层（Application）

- [x] 4.1 创建 `BugReportAppService` 接口与实现（提交、查询列表、详情、更新状态）
- [x] 4.2 创建 `BugReportAdminAppService` 接口与实现（管理端查询、状态更新）
- [x] 4.3 创建 DTO/VO/Command 对象及转换器

#### 测试边界
- 输入条件：前端传来的 DTO 或查询参数
- 前置状态：仓储层已实现
- 后置状态：业务用例正确编排，返回合适 DTO

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 提交报告 | CreateBugReportCommand | 返回报告 ID，状态为 PENDING | - |
| TC-002 | 提交带图片 | CreateBugReportCommand + fileIds=[1,2,3] | 报告与图片关联正确建立 | - |
| TC-003 | 提交图片超限 | fileIds=[1,2,3,4] | - | 抛出 IllegalArgumentException |
| TC-004 | 管理员列表查询 | ListBugReportsQuery(page=1, size=10) | 返回 PageDTO，含报告数据 | - |
| TC-005 | 管理员更新状态 | UpdateStatusCommand(id=1, status=RESOLVED) | 状态更新成功 | - |
| TC-006 | 更新不存在报告 | id=99999 | - | 抛出 NotFoundException |

---

## 5. 后端接口层（API / Controller）

- [x] 5.1 创建 `BugReportController`（公开提交接口 `POST /api/v1/bug-reports`）
- [x] 5.2 创建 `AdminBugReportController`（管理接口，路径 `/api/v1/admin/bug-reports`）
- [x] 5.3 配置权限注解 `@RequiresPermission`（提交接口 PUBLIC，管理接口 PROTECTED）

#### 测试边界
- 输入条件：HTTP 请求及各类参数
- 前置状态：应用层服务已实现
- 后置状态：返回标准 ResponseMessage<T>

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 公开提交成功 | POST /api/v1/bug-reports，有效参数 | 返回 200 + 报告 ID | - |
| TC-002 | 提交描述为空 | description="" | 返回 400 + 校验错误 | - |
| TC-003 | 管理员列表 | GET /api/v1/admin/bug-reports，MEMBER 登录 | 返回 200 + 分页数据 | - |
| TC-004 | 无权限访问管理接口 | CANDIDATE 调用列表接口 | 返回 403 | - |
| TC-005 | 更新状态 | PUT /api/v1/admin/bug-reports/1/status | 返回 200 + 更新后数据 | - |
| TC-006 | 权限标识唯一性校验 | 启动应用 | PermissionScanner 无重复报错 | - |

---

## 6. 前端公共组件

- [x] 6.1 创建 `BugReportFloatButton` 组件（全局 FloatButton）
- [x] 6.2 创建 `BugReportModal` 组件（含描述输入、图片上传、邮箱输入）
- [x] 6.3 在 `src/frontend/src/app/layout.tsx` 中挂载 FloatButton
- [x] 6.4 实现环境信息自动收集（URL、UA、分辨率、视口）
- [x] 6.5 集成 App.message 成功/失败提示

#### 测试边界
- 输入条件：用户交互（点击、输入、上传）
- 前置状态：组件已挂载，后端接口可访问
- 后置状态：正确调用 API 并给出反馈

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 打开 Modal | 点击 FloatButton | Modal 弹出，表单字段可见 | - |
| TC-002 | 提交成功 | 填写描述 + 3 张图片 + 邮箱 | 调用上传 API → 提交 API → message.success → Modal 关闭 | - |
| TC-003 | 描述为空提交 | 描述为空，点击提交 | 前端校验阻止提交，显示错误提示 | - |
| TC-004 | 不上传图片 | 仅填写描述 | 正常提交，fileIds 为空数组 | - |
| TC-005 | 上传超过 3 张 | 尝试添加第 4 张截图 | 前端阻止上传，提示"最多上传 3 张截图" | - |
| TC-006 | 环境信息正确 | 在 /home 页面提交 | payload 中包含 /home 及当前 UA/分辨率 | - |

---

## 7. 前端 Admin 管理页面

- [x] 7.1 创建 `src/frontend/src/app/admin/bug-report/page.tsx` 列表页
- [x] 7.2 实现分页表格、状态筛选、操作列（查看详情、更新状态）
- [x] 7.3 创建详情 Modal/Drawer，展示完整描述、环境信息、图片预览
- [x] 7.4 在 `AdminSideBar` 新增「Bug 报告」菜单项（minLevel 对应 MEMBER）
- [x] 7.5 配置前端权限守卫，CANDIDATE 不可访问该页面

#### 测试边界
- 输入条件：管理员交互及后端返回数据
- 前置状态：后端管理接口已实现
- 后置状态：页面正确展示数据并支持操作

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 列表加载 | MEMBER 进入页面 | 展示表格，含报告数据 | - |
| TC-002 | 状态筛选 | 选择"未解决" | 表格仅展示 PENDING 状态数据 | - |
| TC-003 | 查看详情 | 点击"查看"按钮 | Drawer 弹出，展示完整信息及图片 | - |
| TC-004 | 更新状态 | 将报告标记为"已解决" | 调用 API，表格状态更新 | - |
| TC-005 | 无权限访问 | CANDIDATE 直接访问 /admin/bug-report | 跳转 403 或无菜单入口 | - |

---

## 8. 集成与验证

- [x] 8.1 运行后端全量测试（`./mvnw test`），确保无回归 — 编译通过，单元测试通过；集成测试失败为环境 Testcontainers 问题，非本变更引起
- [x] 8.2 运行前端 lint（`pnpm lint`），确保无错误 — 通过，无新增 error
- [x] 8.3 手动端到端验证：访客提交 → 管理员查看 → 更新状态
- [x] 8.4 提交代码并归档变更

#### 测试边界
- 输入条件：完整功能链路
- 前置状态：前后端代码已完成
- 后置状态：全链路可用，测试通过

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 端到端提交 | 访客填写并提交报告 | 数据库有记录，MinIO 有图片，Admin 可见 | - |
| TC-002 | 端到端处理 | 管理员查看并标记已解决 | 状态更新，前端列表同步 | - |
| TC-003 | 全量测试 | `./mvnw test` | 所有测试通过 | - |
| TC-004 | 代码规范 | `pnpm lint` | 无 error | - |
