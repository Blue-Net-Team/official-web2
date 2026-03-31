## ADDED Requirements

### Requirement: 竞赛列表页 ISR 支持
竞赛列表页 SHALL 设置 `export const revalidate`，从 `@/config/isr` 导入 ISR.competitions，启用增量静态再生成。

#### Scenario: 竞赛列表页启用 ISR
- **WHEN** 访问竞赛列表页
- **THEN** 页面在 revalidate 时间到期后自动在后台重新生成
- **AND** 重新生成后的下一个请求返回更新后的竞赛数据
