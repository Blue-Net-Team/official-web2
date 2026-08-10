## 1. 数据库迁移

- [x] 1.1 检查线上/测试环境 `tb_competition` 表是否存在重复 `name`；如存在，人工清洗数据
- [x] 1.2 编写 Flyway 迁移脚本，为 `tb_competition.name` 添加 `UNIQUE` 约束（如 `V34__add_competition_name_unique.sql`）
- [x] 1.3 本地启动后端验证迁移成功，无约束冲突

## 2. 后端：竞赛名称唯一性校验

- [x] 2.1 在 `CompetitionRepository` 接口中新增 `existsByName(String name)` 和 `findByName(String name)` 方法
- [x] 2.2 在 `CompetitionMapper` 接口中新增对应的 MyBatis 查询方法
- [x] 2.3 在 `CompetitionRepositoryImpl` 中实现 `existsByName` 和 `findByName`
- [x] 2.4 在 `CompetitionAppServiceImpl.createCompetition()` 中增加名称唯一性校验，重复时抛出 `BadRequest("竞赛名称已存在")`
- [x] 2.5 在 `CompetitionAppServiceImpl.updateCompetition()` 中增加名称唯一性校验（排除自身）
- [x] 2.6 编写/更新单元测试和集成测试，覆盖名称重复场景

## 3. 后端：成就查询匹配优化

- [x] 3.1 确认 `AchievementRepositoryImpl.buildAchievementReadModels()` 中 `relate_to` 与 `tb_competition.name` 的匹配逻辑在名称唯一后工作正常
- [x] 3.2 （可选）对 `relate_to` 值在查询前进行 `trim()`，增强健壮性

## 4. 前端：成就表单关联项控件改造

- [x] 4.1 在 `admin/achievement/AchievementDrawer.tsx` 中引入竞赛服务，获取竞赛列表数据
- [x] 4.2 当成就类型为 `COMPETITION` 时，将 `relateTo` 字段从 `Input` 改为 `Select`（`showSearch` + `allowClear`），选项为竞赛名称
- [x] 4.3 在 Select 的 `onChange` 或表单提交前对 `relateTo` 执行 `trim()`
- [x] 4.4 当成就类型为 `PAPER` / `PATENT` 时，保持原有 `Input` 不变
- [x] 4.5 编辑模式下正确回显已有 `relateTo` 值（包括不在竞赛库中的自定义名称）
- [x] 4.6 前端保存失败时展示后端返回的"竞赛名称已存在"等错误信息

## 5. 验证与测试

- [x] 5.1 后端编译打包，通过所有测试
- [x] 5.2 前端构建无错误
- [x] 5.3 使用 Playwright 验证：创建竞赛成就时可通过下拉选择已有竞赛
- [x] 5.4 使用 Playwright 验证：创建竞赛成就时可输入新竞赛名称并成功保存
- [x] 5.5 使用 Playwright 验证：输入带前后空格的竞赛名，保存后自动 trim
- [x] 5.6 使用 Playwright 验证：创建/更新竞赛时，名称重复会收到友好错误提示
- [x] 5.7 使用 Playwright 验证：论文/专利类型的关联项仍为普通输入框
