## ADDED Requirements

### Requirement: ER 图展示
在 README 中展示数据库表之间的关系图。

#### Scenario: 核心实体关系完整
- **WHEN** 查看 README 的数据库设计章节
- **THEN** 可以看到包含所有核心实体（USER、ROLE、ENROLL、EVALUATION 等）的 ER 图

#### Scenario: 表关系清晰
- **WHEN** 查看 ER 图
- **THEN** 可以通过连接线和基数标注理解表之间的关系

### Requirement: 架构图增强
添加更多系统流程图以辅助理解系统工作原理。

#### Scenario: 登录流程图
- **WHEN** 查看技术架构章节
- **THEN** 可以看到登录流程的流程图（本地登录、邮箱验证码登录、GitHub OAuth2）

#### Scenario: 文件访问流程图
- **WHEN** 查看技术架构章节
- **THEN** 可以看到文件访问权限控制的流程图

### Requirement: 文档可读性优化
优化文档的排版和格式，提升阅读体验。

#### Scenario: 表格信息清晰
- **WHEN** 查看技术栈或数据表说明
- **THEN** 信息以表格形式清晰展示，包含必要的说明列

#### Scenario: 代码示例完整
- **WHEN** 查看快速开始章节的命令示例
- **THEN** 所有命令都是完整可执行的，包含必要的注释
