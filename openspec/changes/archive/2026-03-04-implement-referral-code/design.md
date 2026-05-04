## Context

当前系统缺少内推码功能的完整实现。根据业务需求，团队成员应拥有唯一的8位内推码，用于追踪报名来源。现有代码中 `EnrollRepositoryImpl.getReferralUserName()` 错误地使用 `selectByStudentId()` 查询推荐人。

### 现有基础设施
- `tb_enroll` 表有 `internal_referral_code` 字段（报名时填写）
- `EnrollVO` 有 `referralUserId` 和 `referralUserName` 字段
- 缺少 `tb_user.internal_referral_code` 字段

## Goals / Non-Goals

**Goals:**
- 为 `tb_user` 表添加 `internal_referral_code` 字段
- 实现内推码自动生成逻辑（8位大写字母+数字，确保唯一性）
- 实现通过内推码查询推荐人的功能
- 修复现有代码中的错误查询逻辑
- 审核通过创建用户时自动生成内推码

**Non-Goals:**
- 不修改现有报名 API 的接口定义
- 不实现内推码统计功能
- 不实现内推码管理接口（查询/重新生成等）

## Decisions

### 1. 内推码生成算法

**选择**: 使用随机大写字母+数字组合，检查数据库确保唯一性

**格式**: 8位大写字母（A-Z）+ 数字（0-9）

**生成流程**:
1. 生成8位随机字符串
2. 查询数据库检查是否已存在
3. 如已存在，重新生成（最多重试10次）
4. 返回唯一的内推码

**理由**: 简单可靠，8位提供约 2.8 万亿种组合（36^8），碰撞概率极低

### 2. 数据库字段设计

**字段**: `internal_referral_code VARCHAR(8)`

**索引**: 部分唯一索引（WHERE internal_referral_code IS NOT NULL）

**理由**:
- 允许 NULL（非成员用户无内推码）
- 唯一索引确保内推码不重复

### 3. 现有数据迁移

**策略**: 在迁移脚本中为现有成员（member/direction_admin/super_admin）自动生成内推码

**SQL**: 使用 PostgreSQL 的 `MD5(RANDOM()::TEXT || id::TEXT)` 生成随机码

### 4. 查询推荐人逻辑

**问题**: `EnrollRepositoryImpl.getReferralUserName()` 和 `getReferralUserId()` 错误地使用 `selectByStudentId()` 查询推荐人

**变更**:
1. **新增** `UserMapper.selectByInternalReferralCode(String code)` 方法
2. **修改** `EnrollRepositoryImpl` 中的调用点，使用新的 `selectByInternalReferralCode()` 方法
3. **保留** `selectByStudentId()` 方法不变（其他功能可能使用）

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 随机码碰撞 | 最多重试10次，8位空间足够大，碰撞概率极低 |
| 迁移失败 | 迁移脚本使用事务，失败自动回滚 |
| 现有成员无内推码 | 迁移脚本自动为现有成员生成 |
