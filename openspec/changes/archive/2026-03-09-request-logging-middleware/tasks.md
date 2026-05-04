## 1. 拦截器实现

- [x] 1.1 创建RequestLoggingInterceptor类，实现HandlerInterceptor接口
- [x] 1.2 实现afterCompletion方法，记录请求方法、URI和响应状态
- [x] 1.3 根据响应状态码设置适当的日志级别（INFO/WARN/ERROR）

## 2. 拦截器注册

- [x] 2.1 创建WebMvcConfig配置类（如果不存在）
- [x] 2.2 注册RequestLoggingInterceptor到Spring MVC
- [x] 2.3 配置拦截器路径模式（拦截所有API请求）

## 3. 测试

- [x] 3.1 编写RequestLoggingInterceptor单元测试
- [x] 3.2 验证日志输出格式符合要求
- [x] 3.3 测试不同响应状态码的日志级别