## 1. 后端 - DTO 与 Redis 流程状态

- [x] 1.1 创建密码重置请求 DTO：`VerifyStudentRequestDTO`（studentId）、`VerifyEmailRequestDTO`（resetToken, email）、`SendResetCodeRequestDTO`（resetToken）、`ResetPasswordRequestDTO`（resetToken, code, newPassword, confirmPassword）
- [x] 1.2 创建 Redis 流程状态管理组件：`ResetPasswordStateService`（存储/读取/更新/删除 `reset_pwd:{token}` 状态），包含 studentId、email、step 字段，TTL 15 分钟

## 2. 后端 - 接口层（Controller）

- [x] 2.1 创建 `ResetPasswordController`，添加 4 个公开接口：`/api/v1/auth/reset-password/verify-student`、`/verify-email`、`/send-code`、`/reset`
- [x] 2.2 为所有接口添加 `@RequiresPermission(access = AccessLevel.PUBLIC)` 注解
- [x] 2.3 将 4 个接口路径添加到 CSRF 白名单（`CsrfTokenFilter` 或配置类）

## 3. 后端 - 应用层（Service）

- [x] 3.1 创建 `ResetPasswordService` 接口和 `ResetPasswordServiceImpl`，编排 4 步流程
- [x] 3.2 实现学号验证逻辑：查询 UserRepository 确认学号存在，生成 resetToken，存储 Redis 状态
- [x] 3.3 实现邮箱验证逻辑：校验 resetToken 和 step，查询 User 表确认邮箱与学号匹配，更新 Redis 状态
- [x] 3.4 实现发送验证码逻辑：校验 resetToken 和 step，调用 `VerificationCodeDomainService.generateCode(email, ip, "reset_password")`，更新 Redis 状态
- [x] 3.5 实现重置密码逻辑：校验 resetToken、step 和验证码，BCrypt 编码新密码，更新 User 表，清除 Redis 状态，吊销所有设备 JWT Token

## 4. 后端 - 测试

- [x] 4.1 编写 `ResetPasswordServiceTest` 单元测试：覆盖学号不存在、邮箱不匹配、Token 过期、验证码错误、密码不匹配等场景
- [x] 4.2 编写 `ResetPasswordControllerTest` 接口测试：验证 4 个接口的请求/响应格式和权限配置

## 5. 前端 - 页面与组件

- [x] 5.1 创建 `/forgot-password` 页面路由文件 `src/app/(public)/(other)/forgot-password/page.tsx`
- [x] 5.2 创建 `ForgotPasswordForm` 客户端组件，包含 4 步表单状态管理和步骤切换动画
- [x] 5.3 实现步骤 1：学号输入表单（输入框 + "下一步"按钮 + "返回登录"链接）
- [x] 5.4 实现步骤 2：邮箱输入表单（输入框 + "下一步"按钮）
- [x] 5.5 实现步骤 3：验证码输入表单（验证码输入框 + "发送验证码"按钮含 60 秒倒计时 + "下一步"按钮）
- [x] 5.6 实现步骤 4：密码设置表单（新密码 + 确认密码，含眼睛图标切换显示）
- [x] 5.7 实现步骤指示器组件（4 个圆点 + 连接线 + 标签，动态颜色：完成=绿/当前=橙/未完成=灰）
- [x] 5.8 实现桌面端/移动端响应式布局（桌面左右分栏，移动端全宽）

## 6. 前端 - API 集成

- [x] 6.1 在前端 auth service 中添加 4 个密码重置 API 调用方法
- [x] 6.2 集成表单提交逻辑：调用 API、处理成功/失败响应、步骤切换
- [x] 6.3 密码重置成功后显示成功提示，3 秒后自动跳转到登录页
