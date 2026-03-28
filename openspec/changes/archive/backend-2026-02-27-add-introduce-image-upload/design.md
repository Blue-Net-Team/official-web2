## Context

当前系统已有文件上传基础设施（FileDomainService、MinIO存储），但缺少介绍图片上传接口。介绍图片用于官网首页展示，包括实验室介绍、方向介绍、竞赛介绍（合照和Logo）、团队合照等。

现有架构：
- `FileUploadController` 已有头像、考题附件、二维码等上传接口
- `IntroduceImageDomainService` 已有 `addCompetitionImage` 方法
- `CompetitionDomainService` 有 `updateCompetition` 方法可更新 logoFileId

## Goals / Non-Goals

**Goals:**
- 新增 3 个文件上传接口，一步到位完成文件上传和数据关联
- 遵循 DDD 四层架构规范
- 复用现有文件存储基础设施

**Non-Goals:**
- 不修改现有 ImageType 枚举
- 不修改 IntroduceImage 表结构
- 不实现图片删除功能（已有接口）

## Decisions

### 1. 上传流程设计：一步到位

**选择**：一次请求完成上传和关联

**理由**：
- 简化前端调用
- 与现有 `uploadAvatar` 风格一致
- 减少网络请求次数

**替代方案**：两步走（先上传获取 fileId，再关联）
- 缺点：前端需要维护中间状态，增加复杂度

### 2. 竞赛 Logo 与合照分离

**选择**：Logo 存储在 `Competition.logoFileId`，合照存储在 `IntroduceImage` 表

**理由**：
- Logo 是竞赛属性，一对一关系
- 合照是介绍图片，一对多关系
- 与现有数据模型一致

### 3. 权限级别

**选择**：`AccessLevel.PROTECTED`（管理员权限）

**理由**：
- 介绍图片属于后台管理功能
- 仅管理员可上传

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| direction 参数仅在 type=DIRECTION 时有效 | 应用层参数校验，非 DIRECTION 类型时忽略 direction |
| 竞赛不存在时上传失败 | 应用层调用 `existsById` 校验 |
| 文件保存与数据库更新原子性 | 应用层方法添加 `@Transactional` |
