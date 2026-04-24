## 1. 验证码模板场景化重构

### Task 1.1: 创建 VerificationCodeScene 枚举

#### 测试边界
- 输入：场景标识（LOGIN, RESET_PASSWORD, CHANGE_EMAIL_ORIGINAL, CHANGE_EMAIL_NEW）
- 前置状态：无
- 后置状态：枚举定义完成，每个场景包含 title、description、footer

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常获取 | LOGIN | title="登录" | - |
| TC-002 | 正常获取 | RESET_PASSWORD | title="密码重置" | - |
| TC-003 | 正常获取 | CHANGE_EMAIL_ORIGINAL | title="修改邮箱 - 验证原邮箱" | - |
| TC-004 | 正常获取 | CHANGE_EMAIL_NEW | title="修改邮箱 - 验证新邮箱" | - |

#### 实现步骤
- [x] 1.1.1 编写测试用例（红灯阶段）
- [x] 1.1.2 创建 `VerificationCodeScene` 枚举类
- [x] 1.1.3 运行测试确认通过

### Task 1.2: 创建 EmailVerificationCodeTemplate 通用模板

#### 测试边界
- 输入：VerificationCodeScene 枚举值 + 验证码字符串
- 前置状态：TemplateVariableSubstitutor 可用
- 后置状态：返回渲染后的 HTML 邮件内容

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | LOGIN 场景 | scene=LOGIN, code="123456" | HTML 包含 "蓝网登录验证码" 和 "123456" | - |
| TC-002 | RESET_PASSWORD 场景 | scene=RESET_PASSWORD, code="654321" | HTML 包含 "蓝网密码重置验证码" 和 "654321" | - |
| TC-003 | CHANGE_EMAIL_ORIGINAL 场景 | scene=CHANGE_EMAIL_ORIGINAL, code="111111" | HTML 包含 "验证原邮箱" 和 "111111" | - |
| TC-004 | CHANGE_EMAIL_NEW 场景 | scene=CHANGE_EMAIL_NEW, code="222222" | HTML 包含 "验证新邮箱" 和 "222222" | - |
| TC-005 | code 为 null | scene=LOGIN, code=null | HTML 中 code 位置为空字符串 | - |

#### 实现步骤
- [x] 1.2.1 编写测试用例（红灯阶段）
- [x] 1.2.2 创建 `EmailVerificationCodeTemplate` 组件类
- [x] 1.2.3 运行测试确认通过

### Task 1.3: 删除旧模板类并更新调用方

#### 测试边界
- 输入：无（重构任务）
- 前置状态：3 个旧模板类存在，3 个 Service 依赖它们
- 后置状态：旧模板类删除，Service 依赖新通用模板

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | AuthAppServiceImpl 发送登录验证码 | email + scene | 调用 EmailVerificationCodeTemplate.buildHtml(VerificationCodeScene.LOGIN, code) | - |
| TC-002 | ResetPasswordAppServiceImpl 发送重置验证码 | email | 调用 EmailVerificationCodeTemplate.buildHtml(VerificationCodeScene.RESET_PASSWORD, code) | - |
| TC-003 | UserInfoAppServiceImpl 发送修改邮箱验证码 | email + sceneStr | 根据 sceneStr 选择 CHANGE_EMAIL_ORIGINAL 或 CHANGE_EMAIL_NEW | - |

#### 实现步骤
- [x] 1.3.1 更新 `AuthAppServiceImpl`：注入 `EmailVerificationCodeTemplate`，删除 `LoginVerificationCodeTemplate` 依赖
- [x] 1.3.2 更新 `ResetPasswordAppServiceImpl`：注入 `EmailVerificationCodeTemplate`，删除 `ResetPasswordVerificationCodeTemplate` 依赖
- [x] 1.3.3 更新 `UserInfoAppServiceImpl`：注入 `EmailVerificationCodeTemplate`，删除 `ChangeEmailVerificationCodeTemplate` 依赖
- [x] 1.3.4 删除 `LoginVerificationCodeTemplate.java`
- [x] 1.3.5 删除 `ResetPasswordVerificationCodeTemplate.java`
- [x] 1.3.6 删除 `ChangeEmailVerificationCodeTemplate.java`
- [x] 1.3.7 更新 `AuthAppServiceImplTest` 中的 Mock 对象
- [x] 1.3.8 更新 `ResetPasswordAppServiceImplTest` 中的 Mock 对象
- [x] 1.3.9 更新 `UserInfoAppServiceImplTest` 中的 Mock 对象（如需）
- [x] 1.3.10 运行全量测试确认通过

## 2. 报名拒绝邮件通知

### Task 2.1: 创建报名拒绝通知模板

#### 测试边界
- 输入：用户名（username）、拒绝原因（rejectReason）
- 前置状态：TemplateVariableSubstitutor 可用
- 后置状态：返回渲染后的 HTML 邮件内容

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常场景 | username="张三", rejectReason="人数已满" | HTML 包含用户名和拒绝原因 | - |
| TC-002 | 参数为 null | username=null, rejectReason="不符合要求" | HTML 中用户名为空字符串 | - |

#### 实现步骤
- [x] 2.1.1 编写测试用例（红灯阶段）
- [x] 2.1.2 创建 `EnrollmentRejectionTemplate` 组件类
- [x] 2.1.3 运行测试确认通过

### Task 2.2: 在 rejectEnrollment 中触发发送拒绝邮件

#### 测试边界
- 输入：报名 ID + 拒绝命令（含原因）
- 前置状态：报名状态为 PENDING，报名记录有邮箱地址
- 后置状态：报名状态变为 REJECTED，异步发送拒绝邮件

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常拒绝 | id=1, reason="人数已满" | 报名状态变为 REJECTED，messageDispatcher.dispatchAsync 被调用 | - |
| TC-002 | 无邮箱地址 | id=1, enroll.email=null | 报名状态变为 REJECTED，不发邮件，记录 warn 日志 | - |
| TC-003 | 邮件发送失败 | id=1, reason="不符合要求" | 报名状态变为 REJECTED，邮件发送异常被捕获并记录 | - |

#### 实现步骤
- [x] 2.2.1 编写测试用例（红灯阶段）
- [x] 2.2.2 修改 `EnrollAppServiceImpl.rejectEnrollment()`：注入 `EnrollmentRejectionTemplate`，发送拒绝邮件
- [x] 2.2.3 运行测试确认通过

## 3. 考核结果发布改造

### Task 3.1: 新增查询方向最大轮次的 Repository 方法

#### 测试边界
- 输入：方向（Direction）+ 年级（grade）
- 前置状态：tb_assessment_time 表有该方向的多轮次数据
- 后置状态：返回最大 epoch 值

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 方向有3轮 | direction=COMPUTER_VISION, grade=2024 | 返回 3 | - |
| TC-002 | 方向有2轮 | direction=STRUCTURAL_DESIGN, grade=2024 | 返回 2 | - |
| TC-003 | 方向无数据 | direction=EMBEDDED, grade=2024 | 返回 0 或 Optional.empty() | - |

#### 实现步骤
- [x] 3.1.1 编写测试用例（红灯阶段）
- [x] 3.1.2 在 `AssessmentTimeRepository` 接口新增 `findMaxEpochByDirectionAndGrade` 方法
- [x] 3.1.3 在 `AssessmentTimeMapper` XML 中实现 SQL 查询
- [x] 3.1.4 在 `AssessmentTimeRepositoryImpl` 中实现方法
- [x] 3.1.5 运行测试确认通过

### Task 3.2: 改造 publishDecisions 区分最后一轮

#### 测试边界
- 输入：assessmentTimeId
- 前置状态：该考核时间有已决策考生
- 后置状态：最后一轮使用录取/淘汰文案，非最后一轮使用通过/未通过文案

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 中间轮次通过 | assessmentTimeId(第1轮), passed=true | 邮件 resultText="通过" | - |
| TC-002 | 中间轮次淘汰 | assessmentTimeId(第1轮), passed=false | 邮件 resultText="未通过" | - |
| TC-003 | 最终轮次通过 | assessmentTimeId(第3轮,最大), passed=true | 邮件 resultText="录取" | - |
| TC-004 | 最终轮次淘汰 | assessmentTimeId(第3轮,最大), passed=false | 邮件 resultText="淘汰" | - |
| TC-005 | 查询不到最大轮次 | assessmentTimeId, 无方向数据 | 默认视为最后一轮，使用录取/淘汰文案 | - |

#### 实现步骤
- [x] 3.2.1 编写测试用例（红灯阶段）
- [x] 3.2.2 修改 `AssessmentJudgementAppServiceImpl.publishDecisions()`：注入 `AssessmentTimeRepository`，查询最大轮次并判断
- [x] 3.2.3 更新 `AssessmentDecisionNotificationTemplate`：支持传入 resultText 或根据 isFinalRound 自动选择文案
- [x] 3.2.4 运行测试确认通过

## 4. 模板管理后台

### Task 4.1: 创建模板元数据注册表

#### 测试边界
- 输入：无（系统初始化时注册）
- 前置状态：所有模板组件已定义
- 后置状态：可以通过代码枚举所有模板元数据

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 枚举所有模板 | - | 返回包含所有模板元数据的列表 | - |
| TC-002 | 按 code 查询 | code="LOGIN_VERIFY_CODE" | 返回对应模板元数据 | - |
| TC-003 | 查询不存在 | code="NON_EXISTENT" | 返回 empty/null | - |

#### 实现步骤
- [x] 4.1.1 编写测试用例（红灯阶段）
- [x] 4.1.2 创建 `MessageTemplateRegistry` 组件，在构造时注册所有模板元数据
- [x] 4.1.3 运行测试确认通过

### Task 4.2: 创建 MessageTemplateAppService

#### 测试边界
- 输入：模板操作命令（列表、详情、更新、启禁用、预览）
- 前置状态：模板注册表已初始化
- 后置状态：返回对应的模板数据或执行状态变更

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 列表查询 | page=0, size=10 | 返回分页模板列表 | - |
| TC-002 | 详情查询 | code="LOGIN_VERIFY_CODE" | 返回模板完整元数据 | - |
| TC-003 | 更新内容 | code="LOGIN_VERIFY_CODE", newSubject, newContent | 更新成功，后续渲染使用新内容 | - |
| TC-004 | 更新含非法变量 | code="LOGIN_VERIFY_CODE", content="{{unsupported}}" | 抛出 IllegalArgumentException | - |
| TC-005 | 启禁用切换 | code="LOGIN_VERIFY_CODE", enabled=false | 模板状态变为禁用 | - |
| TC-006 | 预览 | code="LOGIN_VERIFY_CODE", variables={"code":"123456"} | 返回渲染后的 HTML | - |
| TC-007 | 禁用后发送 | code="LOGIN_VERIFY_CODE", enabled=false | 发送时抛出 TemplateDisabledException | - |

#### 实现步骤
- [x] 4.2.1 编写测试用例（红灯阶段）
- [x] 4.2.2 创建 `MessageTemplateAppService` 接口和实现类
- [x] 4.2.3 创建相关 Command/VO/Result 类
- [x] 4.2.4 运行测试确认通过

### Task 4.3: 创建 AdminMessageTemplateController

#### 测试边界
- 输入：HTTP 请求（GET/PUT/POST）
- 前置状态：MessageTemplateAppService 可用
- 后置状态：返回对应的 ResponseMessage

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 列表 | GET /api/v1/admin/message-templates | 200 + 模板列表 | - |
| TC-002 | 详情 | GET /api/v1/admin/message-templates/{code} | 200 + 模板详情 | - |
| TC-003 | 更新 | PUT /api/v1/admin/message-templates/{code} | 200 + success | - |
| TC-004 | 启禁用 | POST /api/v1/admin/message-templates/{code}/toggle | 200 + 新状态 | - |
| TC-005 | 预览 | POST /api/v1/admin/message-templates/{code}/preview | 200 + HTML 内容 | - |
| TC-006 | 无权限 | 非管理员调用 | 403 Forbidden | - |

#### 实现步骤
- [x] 4.3.1 创建 `AdminMessageTemplateController`
- [x] 4.3.2 配置权限注解（RequiresPermission）
- [x] 4.3.3 运行编译确认通过

## 5. 文档修复与全局验证

### Task 5.1: 修复数据库设计文档

- [x] 5.1.1 修改 `docs/数据库设计.md` 中 `tb_message_template` 的字段定义：将 `type` 改为 `code`，补充 `description` 字段

### Task 5.2: 全量测试验证

- [x] 5.2.1 运行 `mvn clean test` 确认全部 820+ 测试通过
- [x] 5.2.2 运行 `mvn clean compile` 确认编译无错误
