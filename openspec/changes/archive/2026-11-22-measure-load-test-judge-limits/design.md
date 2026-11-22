# Design

复用 `initializeContext` 创建真实考核、用户、算法题和测试数据。新增 prepare-only k6 入口，将移除会话的上下文写为 JSON。每道题保存主 Python 标准解的 benchmark 状态和 max/p95/内存结果，不再在 JS 初始化中固定确认 5000ms。

Python 工具只依赖标准库：读取准备上下文，校验所有测量，使用 `math.ceil(maxTimeMs) + extraMs`（默认 100）计算逐题限时，通过真实管理员登录与 CSRF 调用语言限制确认接口，最后写出已确认上下文。

正式算法场景仅接受该确认上下文，并在 setup 中重新登录考生，避免把认证令牌持久化。生产建议公式、配置状态机和判题实现均不在本变更范围。
