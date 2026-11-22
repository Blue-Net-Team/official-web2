# load-test-judge-limits Specification

## Requirements

### Requirement: 十五道压测题必须具有不同算法内容
系统 SHALL 提供 15 种不同题型，每题具有独立题面、标准解、generator 和测试输入；AC 提交 SHALL 使用当前题标准解。

#### Scenario: 准备十五道题
- **WHEN** prepare-only 创建算法题
- **THEN** 应包含 15 个不同标题、标准解和输入，不得全部复用两数相加。

### Requirement: 正式算法压测使用测速确认的限时
系统 SHALL 使用每道题真实 Python benchmark 的最大耗时计算并确认压测限时，不得回退为固定 5000ms。

#### Scenario: 根据最大耗时计算
- **WHEN** 某题 Python benchmark 为 DONE 且 maxTimeMs=81.01
- **THEN** 默认确认限时应为 182ms。

#### Scenario: 测量无效时拒绝继续
- **WHEN** 任一题缺少 benchmark、状态不是 DONE 或 maxTimeMs 非法
- **THEN** 工具应失败且不得执行正式压测。

### Requirement: 准备阶段不执行考生负载且不落盘会话
系统 SHALL 提供独立准备入口，只创建数据并执行标准解 benchmark；写出的上下文不得包含认证令牌、CSRF 或密码。

#### Scenario: 保存准备上下文
- **WHEN** prepare-only 初始化完成
- **THEN** 输出包含题目和测量结果，不包含用户 session 或密码，且未创建 run/submit 任务。

### Requirement: 正式场景只使用已确认上下文
系统 SHALL 在 setup 中校验逐题测速限时并重新登录考生。

#### Scenario: 未提供准备上下文
- **WHEN** 直接启动正式算法场景而未提供已确认上下文
- **THEN** 初始化应失败并提示先执行准备和测速确认步骤。
