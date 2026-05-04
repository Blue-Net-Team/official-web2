## Why

当前数据库的所有表、列和索引都缺少注释说明，导致新成员理解数据模型困难，也影响数据库文档的自动生成。根据《官网功能与开发手册》的规范要求，需要为所有数据库对象添加中文注释，提升代码可读性和维护性。

## What Changes

- 为所有18张表添加表级注释（COMMENT ON TABLE）
- 为所有列添加列级注释（COMMENT ON COLUMN），包括主键、外键、普通字段
- 为所有索引添加注释说明其用途（COMMENT ON INDEX）
- 使用 Flyway 创建新的数据库迁移脚本
- 遵循开发手册中定义的表结构和字段含义

## Capabilities

### New Capabilities
- `db-schema-documentation`: 数据库schema文档化能力，为所有表、列、索引添加注释说明

### Modified Capabilities
<!-- 无现有能力需要修改 -->

## Impact

- **数据库**: PostgreSQL 所有表对象
- **迁移脚本**: 新增 Flyway 迁移文件
- **文档**: 与开发手册中的表设计定义保持一致
- **团队**: 提升新成员上手速度，便于后续维护
