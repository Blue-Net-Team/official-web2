## 1. 数据库迁移与实体

- [x] 1.1 创建 Flyway 迁移脚本：新建 `tb_achievement_external_member` 表
- [x] 1.2 创建 Flyway 迁移脚本：删除 `tb_user_experience` 中 `type='COMPETITION'` 的数据
- [x] 1.3 创建 Flyway 迁移脚本：为 `tb_user_achievement` 的 `achievement_id` 添加索引
- [x] 1.4 创建 `AchievementExternalMember` 领域实体、`AchievementExternalMemberDO`、Mapper、XML
- [x] 1.5 从 `ExperienceType` 枚举中移除 `COMPETITION`

## 2. 后端成就领域层

- [x] 2.1 在 `Achievement` 聚合中新增维护成员关联和外部协作者的行为方法
- [x] 2.2 扩展 `AchievementRepository` 接口：增加保存/查询成员关联、外部协作者、按用户查询成就的方法
- [x] 2.3 在 `AchievementRepositoryImpl` 中实现成员关联与外部协作者的批量写入、替换、级联删除
- [x] 2.4 更新 `AchievementReadModel` 和 DTO，增加 `members` 和 `externalMembers` 字段
- [x] 2.5 更新 `AchievementRepositoryConverter`，处理成员和外部协作者转换

## 3. 后端应用层与 Controller

- [x] 3.1 扩展 `CreateAchievementCommand` / `UpdateAchievementCommand`，增加 `userIds` 和 `externalMembers`
- [x] 3.2 更新 `AchievementAppServiceImpl.createAchievement` / `updateAchievement`，在事务内维护关联数据
- [x] 3.3 更新 `AchievementAppServiceImpl.deleteAchievement`，级联删除成员关联和外部协作者
- [x] 3.4 扩展 `AdminAchievementController` 请求/响应 DTO，增加成员字段
- [x] 3.5 新增公开接口 `GET /api/v1/members/{memberId}/achievements` 及对应 Service/Controller
- [x] 3.6 更新 `AchievementController` 公开列表/详情返回，包含 `members` 和 `externalMembers`
- [x] 3.7 更新 `UserExperienceController` 及 `UserExperienceAppService`，拒绝 `COMPETITION` 类型请求
- [x] 3.8 清理 `UserRepositoryImpl` 中对 `tb_user_achievement` 的越界写入（保留删除用户时的级联清理）

## 4. 后端测试

- [x] 4.1 编写 `Achievement` 聚合单元测试：成员关联与外部协作者维护
- [x] 4.2 编写 `AchievementRepositoryImpl` 集成测试：关联写入、替换、级联删除
- [x] 4.3 编写 `AchievementAppServiceImpl` 集成测试：创建/更新/删除含成员与外部协作者
- [x] 4.4 编写 `AdminAchievementController` 集成测试：字段扩展与校验
- [x] 4.5 编写公开成员成就查询接口集成测试
- [x] 4.6 更新 `UserExperience` 相关测试，移除竞赛类型场景

## 5. 前端类型与 API

- [x] 5.1 更新 `AchievementDTO` 类型定义，增加 `members` 和 `externalMembers`
- [x] 5.2 更新 `admin-achievement.service.ts` 请求/响应类型
- [x] 5.3 新增 `member.service.ts` 方法 `getMemberAchievements(memberId)`
- [x] 5.4 从 `ExperienceType` / 表单类型中移除 `COMPETITION`

## 6. 前端管理端成就抽屉

- [x] 6.1 在 `AchievementDrawer` 表单中新增“系统内成员”`Mentions` 输入框，接入用户搜索 API
- [x] 6.2 在 `AchievementDrawer` 表单中新增“外部协作者”`Select mode="tags"` 输入框
- [x] 6.3 实现编辑时回显已选成员和外部协作者
- [x] 6.4 提交时将 `userIds` 和 `externalMembers` 拼入请求体

## 7. 前端成就展示

- [x] 7.1 更新 `AchievementCard` 组件，展示系统内成员头像/昵称和外部协作者标签
- [x] 7.2 更新 `/achievements` 页面，成就卡片展示关联成员信息

## 8. 前端成员主页与个人中心

- [x] 8.1 从 `ProfileTabs` / `ProfileSidebar` 中移除“竞赛经历”Tab 及计数
- [x] 8.2 在 `ProfileTabs` / `ProfileSidebar` 中新增“个人成就”Tab 及计数
- [x] 8.3 新增 `MemberAchievements` 只读组件，用于成员主页和个人中心
- [x] 8.4 更新 `members/[id]/page.tsx`，用“个人成就”替换“竞赛经历”数据源
- [x] 8.5 更新 `profile/page.tsx`，用“个人成就”替换“竞赛经历”Tab
- [x] 8.6 从 `ExperienceSection` 中移除竞赛相关表单字段和展示逻辑（保留项目/实习）

## 9. 端到端验证

- [x] 9.1 后端编译打包通过：`mvnw clean compile package`
- [x] 9.2 启动 Docker 基础设施与后端服务
- [x] 9.3 运行前端开发服务（检查 3000 端口占用）
- [x] 9.4 使用 Playwright 验证：管理员创建含成员和外部协作者的成就
- [x] 9.5 使用 Playwright 验证：成员主页展示个人成就
- [x] 9.6 使用 Playwright 验证：个人中心“个人成就”只读 Tab 正常
- [x] 9.7 使用 Playwright 验证：项目经历和实习经历不受影响
- [x] 9.8 使用 Playwright 验证：团队成就页展示成员信息

## 10. 文档与归档

- [x] 10.1 更新 `docs/05-参考手册/05-01-数据库设计.md` 中相关表说明
- [x] 10.2 确认所有新增 `@RequiresPermission` 的 `value` 全局唯一
- [ ] 10.3 运行 `openspec archive` 归档变更
