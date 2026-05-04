## Why

当前系统缺少完整的内推码功能实现。文档规定"每个团队成员拥有系统生成的唯一内推码（8位随机大写字母+数字）"，但 `tb_user` 表没有该字段，导致报名时填写的内推码无法正确关联到推荐人。此外，现有代码错误地使用学号查询方法来查找推荐人。

## What Changes

- 为 `tb_user` 表添加 `internal_referral_code` 字段（VARCHAR(8)，唯一索引）
- 为现有团队成员自动生成内推码
- 创建内推码生成服务（8位大写字母+数字，确保唯一性）
- 修复 `EnrollRepositoryImpl` 中错误的查询逻辑
- 审核通过创建用户时自动生成内推码

## Capabilities

### New Capabilities

- `referral-code-generator`: 内推码生成服务，负责生成唯一的8位大写字母+数字内推码

### Modified Capabilities

- `enrollment-api`: 报名 API 的推荐人查询逻辑需要修复，使用正确的内推码查询方法

## Impact

- **数据库**: `tb_user` 表新增字段，需要迁移脚本
- **实体层**: `User` 实体新增 `internalReferralCode` 字段
- **Mapper层**: `UserMapper` 新增 `selectByInternalReferralCode()` 方法
- **领域层**: 新增 `ReferralCodeGenerator` 服务
- **仓储层**: `EnrollRepositoryImpl` 修改查询逻辑
- **现有数据**: 现有团队成员需要在迁移时自动生成内推码
