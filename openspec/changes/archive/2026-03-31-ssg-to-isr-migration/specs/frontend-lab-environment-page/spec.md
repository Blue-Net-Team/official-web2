## ADDED Requirements

### Requirement: 实验室环境页 ISR 支持
实验室环境页 SHALL 设置 `export const revalidate`，从 `@/config/isr` 导入 ISR.labEnvironment，启用增量静态再生成。

#### Scenario: 实验室环境页启用 ISR
- **WHEN** 访问实验室环境页
- **THEN** 页面在 revalidate 时间到期后自动在后台重新生成
- **AND** 重新生成后的下一个请求返回更新后的场地和设备数据
