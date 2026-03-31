## ADDED Requirements

### Requirement: ISR revalidate 统一配置
系统 SHALL 提供 `src/frontend/src/config/isr.ts` 文件，集中管理所有页面的 ISR revalidate 时间（秒），支持通过环境变量覆盖默认值。

#### Scenario: 使用默认 revalidate 值
- **WHEN** 未设置任何 ISR 相关环境变量
- **THEN** 所有页面的 revalidate 默认值为 3600（1 小时）

#### Scenario: 通过环境变量覆盖默认值
- **WHEN** 设置环境变量 `NEXT_PUBLIC_ISR_REVALIDATE=1800`
- **THEN** 所有未单独配置的页面 revalidate 为 1800 秒

#### Scenario: 按页面单独覆盖 revalidate 值
- **WHEN** 设置环境变量 `NEXT_PUBLIC_ISR_HOME=600`
- **THEN** Home 页面的 revalidate 为 600 秒，其他页面不受影响

#### Scenario: 配置项覆盖优先级
- **WHEN** 同时设置 `NEXT_PUBLIC_ISR_REVALIDATE=1800` 和 `NEXT_PUBLIC_ISR_HOME=600`
- **THEN** Home 页面使用 600，其他页面使用 1800

### Requirement: ISR 配置结构定义
`src/frontend/src/config/isr.ts` SHALL 导出名为 `ISR` 的常量对象，包含以下字段：
- `default`: 全局默认 revalidate 秒数
- `home`: Home 页面 revalidate 秒数
- `competitions`: 竞赛列表页 revalidate 秒数
- `labEnvironment`: 实验室环境页 revalidate 秒数
- `direction`: 方向详情页 revalidate 秒数

每个字段 SHALL 优先读取对应环境变量，未设置时 fallback 到 `default`，`default` 未设置时 fallback 到 3600。

#### Scenario: 配置对象字段完整
- **WHEN** 应用启动
- **THEN** ISR 对象包含 default、home、competitions、labEnvironment、direction 五个字段
