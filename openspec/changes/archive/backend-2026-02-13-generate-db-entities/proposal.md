## Why

蓝网官方网站后端项目需要完整的领域模型层来支撑核心业务功能。当前项目骨架已搭建但实体层为空，需要根据产品功能手册中的表设计生成完整的实体类、枚举、MyBatis映射和数据库迁移脚本，为后续业务开发奠定基础。

## What Changes

- 生成18个领域实体类，完整映射产品手册中的表设计
- 创建8个枚举类用于类型约束（方向、状态、文件类型等）
- 实现18个MyBatis Mapper接口及对应的XML映射文件
- 编写Flyway初始迁移脚本V1__init_schema.sql创建所有表
- 建立统一的枚举值映射规范（数据库使用小写下划线格式）
- 实现QuestionContent抽象继承体系支持多态JSON存储

## Capabilities

### New Capabilities

- `user-management`: 用户管理模块，包含用户实体、角色权限体系、用户经历和成就
- `enrollment`: 报名系统，处理外部用户报名申请和审核流程
- `evaluation-system`: 考核系统，包含考核时间安排、题库管理、答案提交和评分
- `file-storage`: 文件存储管理，支持头像、考题附件、考生作品等类型
- `audit-logging`: 审计日志，记录所有操作便于安全审计
- `message-notification`: 消息通知，邮件模板管理和验证码功能
- `college-management`: 学院基础数据管理

### Modified Capabilities

- 无（本项目为初始开发，无现有能力需要修改）

## Impact

- **代码结构**: 在 `domain/model/entity/` 添加18个实体类，`domain/model/enumerate/` 添加8个枚举类
- **数据访问层**: 在 `infrastructure/repository/mapper/` 添加Java Mapper接口，在 `resources/infrastructure/repository/mapper/` 添加XML映射
- **数据库**: 通过Flyway在PostgreSQL中创建18张表及相关约束
- **依赖**: 需要MyBatis Plus支持枚举TypeHandler和JSON字段映射
- **架构规范**: 确立自增主键策略、枚举映射规范、多态JSON存储模式
