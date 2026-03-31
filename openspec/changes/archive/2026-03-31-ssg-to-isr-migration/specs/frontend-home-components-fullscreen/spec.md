## ADDED Requirements

### Requirement: Home 页面 ISR 支持
Home 页面 SHALL 设置 `export const revalidate`，从 `@/config/isr` 导入 ISR.home，启用增量静态再生成。

#### Scenario: Home 页面启用 ISR
- **WHEN** 访问 Home 页面
- **THEN** 页面在 revalidate 时间到期后自动在后台重新生成
- **AND** 重新生成后的下一个请求返回更新后的竞赛数据
