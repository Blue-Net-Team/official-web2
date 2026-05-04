## 1. 配置模块

- [x] 1.1 创建 `src/apis/config.ts`，从环境变量读取配置并导出 `API_BASE_URL`
- [x] 1.2 修改 `src/apis/client.ts`，使用 `config.ts` 中的 `API_BASE_URL`

## 2. 类型定义

- [x] 2.1 完善 `src/apis/schema/enumerate.ts`，添加 `UserRole`、`Direction`、`Gender` 枚举类型
- [x] 2.2 完善 `src/apis/schema/type.ts`，添加 `ResponseMessage<T>` 通用响应类型
- [x] 2.3 完善 `src/apis/schema/type.ts`，添加 `UserInfo` 用户信息类型
- [x] 2.4 完善 `src/apis/schema/type.ts`，添加 `StudentIdLoginRequestDTO` 登录请求类型
- [x] 2.5 完善 `src/apis/schema/type.ts`，添加 `UserAuthResponseDTO` 登录响应类型

## 3. 认证服务

- [x] 3.1 创建 `src/apis/services/auth.service.ts`
- [x] 3.2 实现 `authService.login()` 学号登录函数（公开接口，使用 publicClient）
- [x] 3.3 实现 `authService.logout()` 用户退出函数（需认证，使用 apiClient）

## 4. 验证

- [x] 4.1 运行 TypeScript 类型检查，确保无类型错误
- [x] 4.2 运行 ESLint 检查，确保无 lint 错误
