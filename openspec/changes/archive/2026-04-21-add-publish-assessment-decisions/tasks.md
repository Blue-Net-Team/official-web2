## 1. 应用层：Service 接口与实现

### Task 1: 实现 publishDecisions 应用服务方法

#### 测试边界
- 输入条件：`assessmentTimeId`、当前登录用户
- 前置状态：考核时间存在，有若干已决策考生
- 后置状态：已向已决策考生发送 HTML 邮件，返回发送数量

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常发送 | assessmentTimeId=1，3个已决策考生 | 返回 3，调用 sendHtmlAsync 3 次 | - |
| TC-002 | 无已决策考生 | assessmentTimeId=2，0个已决策 | 返回 0，不调用 sendHtmlAsync | - |
| TC-003 | 考核时间不存在 | assessmentTimeId=999 | - | 抛出 GlobalException |
| TC-004 | 部分用户无邮箱 | 3个已决策，1个 email 为 null | 跳过无邮箱用户，返回 2 | - |
| TC-005 | 单封邮件发送失败 | sendHtmlAsync 抛异常 | 记录日志，继续发送其余，返回成功数 | - |

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）
- [x] 应用层：`AssessmentJudgementService` 接口新增 `publishDecisions(Long assessmentTimeId)` 方法
- [x] 应用层：`AssessmentJudgementServiceImpl` 实现方法，编排流程：获取考核时间→获取决策→过滤已决策→获取用户信息→构建邮件→异步发送
- [x] 运行测试（绿灯阶段）
- [x] 重构优化

## 2. 控制层：API 端点

### Task 2: 新增 publish 端点

#### 测试边界
- 输入条件：`POST /api/v1/admin/assessment-judgements/decisions/publish?assessmentTimeId=1`
- 前置状态：用户已登录且具有 `assessment-decision:set` 权限
- 后置状态：返回 `ResponseMessage<Integer>` 包含发送数量

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-006 | 正常调用 | assessmentTimeId=1 | HTTP 200，body 含发送数量 | - |
| TC-007 | assessmentTimeId 为 null | 无 query param | - | 400 Bad Request |
| TC-008 | 无权限 | 普通成员调用 | - | 403 Forbidden |

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）
- [x] 控制层：`AdminAssessmentJudgementController` 新增 `publishDecisions` 端点
- [x] 控制层：添加 `@Operation`、`@RequiresPermission` 注解
- [x] 运行测试（绿灯阶段）

## 3. 前端：对接 publish API

### Task 3: 前端 service 和页面更新

#### 测试边界
- 输入条件：用户点击"发布本轮结果"按钮
- 前置状态：已选择考核轮次
- 后置状态：展示发送结果或错误信息

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-009 | 成功发送 | 点击发布按钮 | 显示成功消息和发送数量 | - |
| TC-010 | API 失败 | 后端返回错误 | 显示错误消息 | - |
| TC-011 | 未选择考核轮次 | 无 assessmentTimeId | 按钮禁用 | - |

#### 实现步骤（严格按顺序）
- [x] 前端 service：`adminAssessmentJudgementService` 新增 `publishDecisions` 方法
- [x] 前端 decision 页面：替换 `showPublishNotice()` 为 API 调用
- [x] 前端 score 页面：替换 `showPublishNotice()` 为 API 调用
- [x] 手动验证（浏览器测试）
