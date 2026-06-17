## 1. 用户仓储：新增按角色枚举批量更新

- [x] 1.1 在 `UserRepository` 接口新增 `batchUpdateRole(List<Long> userIds, RoleType roleType)`
- [x] 1.2 在 `UserRepositoryImpl` 实现该方法：
  - 校验 `userIds` 非空
  - 通过 `RoleMapper.selectByName(roleType.getName())` 获取 `RoleDO`
  - 未找到角色时抛 `GlobalException`
  - 循环 `userMapper.updateById` 更新每个用户的 `role_id`
  - 记录 info 日志

## 2. 新增决策发布应用服务

- [x] 2.1 新建 `AssessmentDecisionPublicationService`
- [x] 2.2 注入 `UserRepository`、`MessageDispatcher`、`AssessmentDecisionNotificationTemplate`
- [x] 2.3 实现 `publish(AssessmentDecisionVO decision, AssessmentTime assessmentTime)` 方法：
  - 标注 `@Transactional`
  - 查询用户，不存在时抛 `DataNotFound`
  - 判断是否需要升级角色：
    - `assessmentTime.isGlobalFinalAssessment()`
    - `decision.getPassed() == true`
    - `RoleType.CANDIDATE.getName().equals(user.getRoleName())`
  - 需要升级时：查 `MEMBER` 角色，调用 `userRepository.batchUpdateRole(List.of(userId), RoleType.MEMBER)`
  - 构建并异步发送邮件

## 3. 重构发布决策编排器

- [x] 3.1 在 `AssessmentJudgementAppServiceImpl` 注入 `AssessmentDecisionPublicationService`
- [x] 3.2 重构 `publishDecisions(Long assessmentTimeId)`：
  - 保留权限校验和结果发布逻辑
  - 遍历决策列表
  - 对每个决策调用 `publicationService.publish(decision, assessmentTime)`
  - 捕获异常并记录 `ERROR` 日志，继续处理下一个
  - 返回成功数
- [x] 3.3 移除 `publishDecisions` 中内联的邮件构建和发送逻辑

## 4. 单元测试

- [x] 4.1 新增 `AssessmentDecisionPublicationServiceTest`：
  - 全局最终考核通过 + 当前角色 CANDIDATE → 升级为 MEMBER 并发送邮件
  - 全局最终考核通过 + 当前角色 MEMBER → 不升级，仍发送邮件（幂等）
  - 全局最终考核通过 + 当前角色 DIRECTION_ADMIN → 不升级
  - 方向考核通过 → 不升级，仅发送邮件
  - 全局最终考核淘汰 → 不升级，仅发送邮件
  - 用户不存在 → 抛异常
- [x] 4.2 扩展 `AssessmentJudgementAppServiceImplTest`：
  - 多个考生中一个失败 → 继续处理，返回成功数
  - 确认新服务被正确调用

## 5. 编译与验证

- [x] 5.1 后端单元测试全部通过
- [x] 5.2 后端编译打包成功
- [ ] 5.3 如需要，运行 Docker 和 Playwright 端到端验证
