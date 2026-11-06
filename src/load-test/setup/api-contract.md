# 压测初始化与业务 API 调用清单

本文依据当前 Controller/DTO 源码整理，仅用于隔离压测环境。所有管理员与考生请求均走正常 JWT Cookie + CSRF 流程，不使用 SQL 或认证绕过。

## 1. 认证

| 用途 | 方法与路径 | 关键请求/响应 |
|---|---|---|
| 系统管理员/考生登录 | `POST /api/v1/auth/login/student-id` | `{studentId,password}`；`password` 是原始密码的 SHA256 hex；响应 `data.csrfToken`、`data.userInfo`，JWT 写入 HttpOnly Cookie |
| 当前会话检查 | `GET /api/v1/auth/me` | 可用于健康后的登录预检，不用于替代登录 |

登录接口不要求 CSRF。后续 POST/PUT/PATCH/DELETE 请求携带 `X-CSRF-Token: data.csrfToken`，Cookie 由每个 k6 VU 的 Cookie Jar 独立维护。

## 2. 初始化顺序

| 顺序 | 方法与路径 | 必要数据与返回值 |
|---|---|---|
| 1 | `POST /api/v1/admin/colleges` | `{name}` → `data.id` |
| 2 | `POST /api/v1/admin/users` | `{studentId,email,username,password,roleId,collegeId,major,direction,gender,assessmentGradeYear}`；密码传 SHA256；`roleId` 从压测配置 `CANDIDATE_ROLE_ID` 读取（系统角色，不是业务动态 ID）→ `data.id` |
| 3 | `POST /api/v1/admin/assessment-times` | `{direction,epoch,grade,startTime,endTime,timeLimit:false,allowTeam:false}` → `data.id`；LocalDateTime 使用不带时区后缀的本地 ISO 格式 |
| 4 | `POST /api/v1/admin/assessment-questions` | 文件题：`{assessmentTimeId,questionNo,questionType:"file_upload",title,content:{type:"file_upload",content:"..."},score}` → `data.id` |
| 5 | 同上 | 算法题：`questionType:"algorithm"`，`content` 包含 `type:"algorithm"`、题面、`starterCode.python`、默认 `runTestCases`；多道题使用不同 `questionNo` → 每题 `data.id` |
| 6 | `PUT /api/v1/admin/judge/questions/{questionId}/config` | `{generatorLanguage:"python",generatorSource,primaryStandardLanguage:"python",benchmarkRepeatTimes,marginMultiplier,minExtraMs,roundToMs,standardSolutions:[{language:"python",source,primarySolution:true}],testcases:[...]}` |
| 7 | `POST /api/v1/admin/judge/questions/{questionId}/config/generation-tasks` | 请求异步测试数据生成，无响应数据 |
| 8 | `GET /api/v1/admin/judge/questions/{questionId}/config` | 轮询 `data.status`；`GENERATED` 后确认资源限制，最终 `READY`；`FAILED` 立即中止 |
| 9 | `PUT /api/v1/admin/judge/questions/{questionId}/config/language-limits/python` | `{timeLimitMs,memoryLimitKb,outputLimitKb}`；确认后再次 GET，要求 `status=READY` 且 Python `confirmed=true` |

初始化阶段必须等待每道题达到 READY，且 run/formal/test-data 相关异步活动结束后再记录空闲基线。

## 3. 报名场景

`POST /api/v1/enrollments`

```json
{
  "username": "压测考生",
  "studentId": "12-13位全局唯一数字",
  "email": "唯一且合法的邮箱",
  "collegeId": 1,
  "major": "计算机科学与技术",
  "gender": "unknown",
  "direction": "computer_vision",
  "introduction": "至少100个字符的固定压测介绍",
  "forceUpdate": false
}
```

有效新建要求 HTTP 201 且响应 `code=201`；409 冲突、400 校验失败及其他错误均不计入有效报名。

## 4. 文件上传考核场景

1. `POST /api/v1/file/prepare-upload`
   - `{filename:"load-test.zip",type:"WORK",size:<bytes>,contentType:"application/zip"}`
   - 读取 `data.fileId/uploadUrl/callbackToken`。
2. `PUT <uploadUrl>`
   - 请求体为真实固定大小 ZIP fixture；该请求直接到 MinIO，不经过 API。
3. `POST /api/v1/file/confirm-upload`
   - `{fileId,callbackToken,md5:<文件MD5 hex>,size:<bytes>}`
   - 要求响应 `data.status=ACTIVE`（当前 Jackson 返回枚举名）。
4. 首次：`POST /api/v1/assessment-answers`；后续：`PUT /api/v1/assessment-answers`
   - `{questionId:<文件题ID>,fileId}`。

任何步骤失败即终止当前样本，不继续提交无效答案。

## 5. 多道 Python 算法题运行与提交

| 操作 | 方法与路径 | 请求 | 返回任务字段 |
|---|---|---|---|
| 默认运行 | `POST /api/v1/algorithm-judge/run` | `{questionId,language:"python",sourceCode,testcaseType:"DEFAULT_RUN",customInput:null}` | `data.judgeJobId` |
| 正式提交 | `POST /api/v1/algorithm-judge/submit` | `{questionId,language:"python",content:<sourceCode>}` | `data.judgeJobId`、`data.answerId` |
| 轮询 | `GET /api/v1/algorithm-judge/jobs/{jobId}` | 无 | `data.status/testcaseType/caseResults/judgement` |

`PENDING/RUNNING/RETRYING` 为非终态；`SUCCEEDED` 为成功终态；`FAILED_REVIEW_REQUIRED` 为失败终态。运行和提交分别进入 `algorithm.judge.run`、`algorithm.judge.formal`，每 VU 同时最多一个非终态任务。

## 6. 健康检查与观测

- API：`GET /api/v1/health`
- Judge：`GET /api/v1/judge/health`
- Actuator：`/actuator/metrics/{metric}`，仅通过隔离环境内部地址采集。
- RabbitMQ：分别采集 `algorithm.judge.run`、`algorithm.judge.formal` 的 ready/unacked。

当前源码核对结论：上述初始化与业务路径均有现有 API，无需补充 SQL 或认证调试 KEY。
