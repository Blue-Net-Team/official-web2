## 1. 密码哈希工具

- [x] 1.1 创建 `src/utils/passwordHash.ts` 文件
- [x] 1.2 实现 `hashPassword(password: string): Promise<string>` 函数，使用 Web Crypto API SHA-256
- [ ] 1.3 添加单元测试验证哈希输出格式（64位小写十六进制字符串）(项目未配置测试框架，跳过)

## 2. 认证状态管理

- [x] 2.1 创建 `src/stores/authStore.ts` 文件
- [x] 2.2 定义 `AuthState` 接口（token, userInfo, isAuthenticated, isLoading）
- [x] 2.3 实现 `login(credentials)` 方法，集成密码哈希和 API 调用
- [x] 2.4 实现 `logout()` 方法，调用登出 API 并清除状态
- [x] 2.5 配置 Zustand persist 中间件，持久化到 localStorage

## 3. 登录页面集成

- [x] 3.1 修改 `src/app/(public)/(other)/login/page.tsx`，导入 authStore
- [x] 3.2 修改 `handleSubmit` 函数，调用 authStore.login() 方法
- [x] 3.3 添加登录加载状态处理（按钮 loading + 禁用）
- [x] 3.4 添加登录成功后的页面跳转逻辑（跳转到首页 `/`）
- [x] 3.5 添加登录失败的错误提示处理（使用 message.error）

## 4. 错误处理完善

- [x] 4.1 处理网络超时错误（code=408）的提示
- [x] 4.2 处理服务器错误（5xx）的提示
- [x] 4.3 确保错误情况下按钮恢复可点击状态

## 5. 测试验证

- [ ] 5.1 验证密码哈希函数正确性（需手动测试）
- [ ] 5.2 验证登录成功流程（Token 存储、状态更新、页面跳转）（需手动测试）
- [ ] 5.3 验证登录失败流程（错误提示、状态保持）（需手动测试）
- [ ] 5.4 验证页面刷新后状态恢复（Zustand persist）（需手动测试）
- [ ] 5.5 验证登出流程（Token 清除、状态重置）（需手动测试）
