## ADDED Requirements

### Requirement: README 内容同步
根据开发手册的内容同步更新 README.md 文档。

#### Scenario: 功能描述同步
- **WHEN** 对比 README 与开发手册的功能清单
- **THEN** README 中的核心功能描述与开发手册保持一致

#### Scenario: 架构图更新
- **WHEN** 开发手册中包含系统架构图
- **THEN** README 中的技术架构图与开发手册保持一致

#### Scenario: 数据库设计文档化
- **WHEN** 开发手册中包含 ER 图
- **THEN** README 中应包含数据库设计的 ER 图展示

### Requirement: 文档结构规范化
改进 README 的结构和可读性，使其更适合作为项目入口文档。

#### Scenario: 目录结构清晰
- **WHEN** 阅读 README 文档
- **THEN** 可以通过清晰的标题层级快速定位信息

#### Scenario: 图表渲染正确
- **WHEN** 在 GitHub 或其他支持 Mermaid 的平台上查看 README
- **THEN** 所有 Mermaid 图表（架构图、ER 图等）应正确渲染

#### Scenario: 文档索引完整
- **WHEN** 阅读 README 到文档索引章节
- **THEN** 可以看到指向项目其他文档（如开发手册、权限表等）的链接
