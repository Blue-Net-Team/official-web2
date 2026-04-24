## Context

当前系统的消息通知体系具备以下基础：
- `tb_message_template` 表已存在但处于闲置状态，模板内容全部硬编码在 `application/message/template/` 下的 `@Component` 类中
- `TemplateVariableSubstitutor` 已支持 `{{variable}}` 语法替换
- `MessageDispatcher` 策略模式已支持 EMAIL 通道异步发送
- 验证码体系（`VerificationCodeDomainService` + `tb_verify_code`）已成熟，scene 字段为 String 类型

本次变更在现有基础设施上扩展，不引入新的外部依赖。

## Goals / Non-Goals

**Goals:**
- 将 3 个独立的验证码模板类合并为 1 个通用模板 + 场景枚举，降低新增场景成本
- 新增报名拒绝邮件通知，填补业务断点
- 改造考核结果发布，最后一轮使用录取/淘汰文案
- 提供消息模板管理后台 API，支持运营人员在线编辑模板内容
- 修复数据库设计文档与实际表结构的不一致

**Non-Goals:**
- 不将硬编码模板迁移到数据库（模板内容仍硬编码，管理后台只提供未来扩展的接口框架）
- 不改造二维码管理体系（考核群二维码本次不管）
- 不新增短信/站内信等消息通道
- 不修改修改密码流程（该功能已完成，不需要验证码）

## Decisions

### 1. 验证码模板合并为通用模板 + 场景枚举
- **选择**：新增 `EmailVerificationCodeTemplate` 通用模板类 + `VerificationCodeScene` 枚举，删除 3 个独立模板类
- **理由**：3 个模板的 HTML 结构完全一致（标题、说明、验证码、底部提示），仅文案不同。枚举方式新增场景只需添加一行配置
- **替代方案**：保持独立类。拒绝理由：每新增一个验证码场景就要新建一个类，维护成本高

### 2. 报名拒绝邮件在 `rejectEnrollment` 中同步触发
- **选择**：在 `rejectEnrollment` 事务方法内直接调用 `messageDispatcher.dispatchAsync()` 发送邮件
- **理由**：操作简单，与现有 `approveEnrollment` 中的邮件发送模式一致
- **替代方案**：通过领域事件异步触发。拒绝理由：当前系统没有领域事件基础设施，引入过重

### 3. 模板管理后台接口不操作数据库模板表
- **选择**：Admin API 直接操作内存中的模板组件（通过反射/注册表读取模板元数据），不读写 `tb_message_template`
- **理由**：`tb_message_template` 表和 ORM 已存在但完全未使用，若直接对接数据库需要大量数据迁移和初始化工作。本次先提供接口框架，数据库化作为后续迭代
- **替代方案**：直接对接数据库。拒绝理由：工作量大，需要初始化 SQL、数据同步、DO/Mapper/Repository 全链路，超出本次范围

### 4. 最后一轮判断通过查询该方向已配置的最大 epoch 实现
- **选择**：在 `publishDecisions` 中通过 `AssessmentTimeRepository` 查询该方向的最大 epoch 值，与当前 epoch 比较
- **理由**：数据库表以 `(direction, epoch, grade)` 为唯一键，不同方向可配置不同轮次数，通过 `MAX(epoch)` 即可判断
- **替代方案**：在 `AssessmentDecision` 中增加 `isFinalRound` 字段。拒绝理由：增加冗余字段，且需要管理员额外维护

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| 删除 3 个模板类可能影响下游调用方 | 全局搜索确认只有 `AuthAppServiceImpl`、`ResetPasswordAppServiceImpl`、`UserInfoAppServiceImpl` 使用，均已识别 |
| 模板管理后台不操作数据库，后期数据库化时需废弃当前实现 | 接口设计中预留 `code` 字段作为唯一标识，后续数据库化时接口契约可保持不变 |
| `VerificationCodeScene` 枚举新增场景需发版部署 | 当前验证码场景变更频率极低，枚举方式足够灵活 |
| 最后一轮判断依赖数据库查询，若考核时间配置不完整可能误判 | 添加防御性编程：查询不到最大 epoch 时默认当前为最后一轮 |

## Migration Plan

1. 部署新代码（通用模板类、场景枚举、新增模板、Admin API）
2. 运行全量测试确认通过
3. 无需数据库迁移（无表结构变更）
4. 回滚：还原代码即可，无数据影响

## Open Questions

- 模板管理后台的「预览」功能是否需要实际发送邮件到测试地址，还是仅在服务端渲染 HTML 返回？（建议后者，更安全）
- `AssessmentTimeRepository` 目前无 `findMaxEpochByDirection` 方法，是否需要新增，还是通过现有 `findByFilters` 自行计算？
