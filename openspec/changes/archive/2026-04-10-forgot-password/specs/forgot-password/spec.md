## ADDED Requirements

### Requirement: 学号验证接口
系统 SHALL 提供公开接口 `POST /api/v1/auth/reset-password/verify-student`，接收学号，验证学号是否存在于 `tb_user` 表。验证通过后生成 resetToken（UUID），在 Redis 中存储流程状态（key=`reset_pwd:{resetToken}`，含 studentId 和 step=1），TTL 15 分钟。返回 resetToken。

#### Scenario: 学号存在
- **WHEN** 用户提交有效学号 `2023001`
- **THEN** 系统 SHALL 查询 `tb_user` 表确认学号存在
- **THEN** 系统 SHALL 生成 UUID 作为 resetToken
- **THEN** 系统 SHALL 在 Redis 存储流程状态 `{studentId: "2023001", step: 1}`，TTL 15 分钟
- **THEN** 系统 SHALL 返回 `{ code: 200, data: { resetToken } }`

#### Scenario: 学号不存在
- **WHEN** 用户提交不存在的学号 `9999999`
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "学号不存在" }`

#### Scenario: 学号格式无效
- **WHEN** 用户提交空字符串或格式不合法的学号
- **THEN** 系统 SHALL 返回参数校验错误

### Requirement: 邮箱验证接口
系统 SHALL 提供公开接口 `POST /api/v1/auth/reset-password/verify-email`，接收 resetToken 和邮箱，验证邮箱是否与学号关联。验证通过后更新 Redis 流程状态 step=2 和 email 字段。

#### Scenario: 邮箱匹配学号
- **WHEN** 用户提交有效 resetToken 和邮箱 `user@example.com`，且该邮箱与 Redis 中记录的学号关联
- **THEN** 系统 SHALL 从 Redis 读取流程状态，确认 step >= 1
- **THEN** 系统 SHALL 查询 `tb_user` 确认邮箱与学号匹配
- **THEN** 系统 SHALL 更新 Redis 流程状态 `{step: 2, email: "user@example.com"}`
- **THEN** 系统 SHALL 返回 `{ code: 200, data: { resetToken } }`

#### Scenario: 邮箱不匹配
- **WHEN** 用户提交的邮箱与学号不关联
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "邮箱与学号不匹配" }`

#### Scenario: resetToken 无效或过期
- **WHEN** 用户提交的 resetToken 在 Redis 中不存在（过期或无效）
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "重置流程已过期，请重新开始" }`

#### Scenario: 跳步访问（step 未到 1）
- **WHEN** Redis 中流程状态 step < 1
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "请先完成上一步验证" }`

### Requirement: 发送验证码接口
系统 SHALL 提供公开接口 `POST /api/v1/auth/reset-password/send-code`，接收 resetToken，向流程中已验证的邮箱发送 6 位验证码。使用现有 `VerificationCodeDomainService`，scene=`reset_password`。

#### Scenario: 成功发送验证码
- **WHEN** 用户提交有效 resetToken，Redis 流程状态 step >= 2
- **THEN** 系统 SHALL 从 Redis 获取已验证邮箱
- **THEN** 系统 SHALL 调用 `VerificationCodeDomainService.generateCode(email, ip, "reset_password")`
- **THEN** 系统 SHALL 更新 Redis 流程状态 step=3
- **THEN** 系统 SHALL 返回 `{ code: 200, msg: "验证码已发送" }`

#### Scenario: 60 秒内重复发送
- **WHEN** 同一邮箱在 60 秒内再次请求发送验证码
- **THEN** 系统 SHALL 返回 `{ code: 429, msg: "发送过于频繁，请稍后再试" }`

#### Scenario: resetToken 无效或过期
- **WHEN** resetToken 在 Redis 中不存在
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "重置流程已过期，请重新开始" }`

#### Scenario: 跳步访问
- **WHEN** Redis 流程状态 step < 2
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "请先完成上一步验证" }`

### Requirement: 重置密码接口
系统 SHALL 提供公开接口 `POST /api/v1/auth/reset-password/reset`，接收 resetToken、验证码、新密码、确认密码。验证通过后更新 `tb_user` 表中的密码（BCrypt 编码），清除 Redis 流程状态。

#### Scenario: 成功重置密码
- **WHEN** 用户提交有效 resetToken、正确验证码、匹配的新密码和确认密码
- **THEN** 系统 SHALL 验证 Redis 流程状态 step >= 3
- **THEN** 系统 SHALL 验证验证码正确且未过期
- **THEN** 系统 SHALL 使用 `BCryptPasswordEncoder` 编码新密码
- **THEN** 系统 SHALL 更新 `tb_user` 表对应用户的 password 字段
- **THEN** 系统 SHALL 删除 Redis 流程状态
- **THEN** 系统 SHALL 吊销该用户所有已登录设备的 JWT Token（调用 `AuthTokenService.revokeAllUserTokens`）
- **THEN** 系统 SHALL 返回 `{ code: 200, msg: "密码重置成功" }`

#### Scenario: 验证码错误
- **WHEN** 验证码不正确
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "验证码错误" }`

#### Scenario: 验证码已过期
- **WHEN** 验证码超过 5 分钟有效期
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "验证码已过期" }`

#### Scenario: 新密码与确认密码不匹配
- **WHEN** 新密码和确认密码不一致
- **THEN** 系统 SHALL 返回参数校验错误

#### Scenario: resetToken 无效或过期
- **WHEN** resetToken 在 Redis 中不存在
- **THEN** 系统 SHALL 返回 `{ code: 400, msg: "重置流程已过期，请重新开始" }`

### Requirement: 密码重置接口为公开接口
所有密码重置接口 SHALL 无需认证，并加入 CSRF 白名单。

#### Scenario: 未登录用户访问密码重置接口
- **WHEN** 未登录用户调用任一密码重置接口
- **THEN** 系统 SHALL 正常处理请求，无需 CSRF Token

### Requirement: 前端忘记密码页面
系统 SHALL 提供 `/forgot-password` 页面，包含 4 步引导式表单，风格与登录页一致（暗色主题 + 橙色强调色）。

#### Scenario: 页面加载显示步骤 1
- **WHEN** 用户访问 `/forgot-password`
- **THEN** 页面 SHALL 显示步骤 1（输入学号），步骤指示器高亮第 1 步

#### Scenario: 步骤切换无刷新
- **WHEN** 用户完成某一步骤点击"下一步"
- **THEN** 页面 SHALL 使用 CSS transition 平滑切换到下一步骤内容
- **THEN** 步骤指示器 SHALL 更新高亮状态

#### Scenario: 返回登录
- **WHEN** 用户点击"返回登录"链接
- **THEN** 页面 SHALL 导航到 `/login`

#### Scenario: 桌面端布局
- **WHEN** 在宽度 >= 768px 的设备上访问
- **THEN** 页面 SHALL 显示左右分栏布局（左表单 + 右装饰区），与登录页一致

#### Scenario: 移动端布局
- **WHEN** 在宽度 < 768px 的设备上访问
- **THEN** 页面 SHALL 显示全宽表单布局，隐藏右侧装饰区

### Requirement: 验证码输入与发送按钮同页面
步骤 3（输入验证码）SHALL 在同一页面显示验证码输入框和"发送验证码"按钮。

#### Scenario: 发送验证码按钮倒计时
- **WHEN** 用户点击"发送验证码"按钮
- **THEN** 按钮 SHALL 显示 60 秒倒计时
- **THEN** 倒计时期间按钮 SHALL 处于禁用状态

#### Scenario: 验证码发送失败
- **WHEN** 发送验证码请求失败
- **THEN** 页面 SHALL 显示错误提示
- **THEN** 发送按钮 SHALL 恢复可点击状态

### Requirement: 密码输入框支持显示/隐藏
步骤 4 的新密码和确认密码输入框 SHALL 提供眼睛图标切换密码可见性。

#### Scenario: 切换密码可见性
- **WHEN** 用户点击密码输入框旁的眼睛图标
- **THEN** 输入框 SHALL 切换密码的显示/隐藏状态
