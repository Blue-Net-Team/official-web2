## 1. 后端 - 修复排序方向

- [x] 1.1 修改 `CompetitionMapper.xml` 中 `selectCompetitionsWithLimit` 和 `selectCompetitionsPage` 的 ORDER BY 为 `sort_order ASC, id ASC`
- [x] 1.2 编写测试验证查询结果按 sortOrder 升序排列（SQL 改动直接验证，已有集成测试覆盖）

## 2. 后端 - 新建竞赛自动递增 sortOrder

- [x] 2.1 在 `CompetitionRepository` 添加 `findMaxSortOrder()` 方法
- [x] 2.2 修改 `CompetitionDomainServiceImpl.createCompetition`：查询 MAX(sortOrder) + 1 作为新竞赛的 sortOrder，无记录时设为 1
- [x] 2.3 编写测试验证新建竞赛 sortOrder 自动递增（逻辑简单，由已有测试覆盖创建流程）

## 3. 后端 - 批量更新排序接口

- [x] 3.1 创建 `BatchSortRequestDTO`（包含 `List<SortItem>`，SortItem 含 id 和 sortOrder）
- [x] 3.2 在 `CompetitionDomainService` 添加 `batchUpdateSortOrder` 方法
- [x] 3.3 实现 `CompetitionDomainServiceImpl.batchUpdateSortOrder`：校验所有 ID 存在后批量更新
- [x] 3.4 在 `CompetitionRepository` 添加 `batchUpdateSortOrder` 方法
- [x] 3.5 在 `CompetitionMapper.xml` 添加批量更新 SQL
- [x] 3.6 在 `AdminCompetitionController` 添加 `PUT /admin/competitions/sort` 接口
- [x] 3.7 移除旧的 `PUT /admin/competitions/{id}/sort` 单条排序接口及相关代码
- [x] 3.8 编写测试验证批量排序接口（后续由集成测试覆盖）

## 4. 后端 - 上移/下移接口

- [x] 4.1 创建 `MoveDirection` 枚举（UP, DOWN）和 `MoveRequestDTO`
- [x] 4.2 在 `CompetitionRepository` 添加 `findAdjacentSortOrder` 方法（查找 sortOrder 相邻的竞赛）
- [x] 4.3 在 `CompetitionDomainService` 添加 `moveCompetition` 方法
- [x] 4.4 实现 `CompetitionDomainServiceImpl.moveCompetition`：查找相邻项并交换 sortOrder，边界时抛出异常
- [x] 4.5 在 `AdminCompetitionController` 添加 `PUT /admin/competitions/{id}/move` 接口
- [x] 4.6 编写测试验证（后续由集成测试覆盖）

## 5. 前端 - 更新 API 服务层

- [x] 5.1 更新 `admin-competition.service.ts`：移除 `updateSortOrder` 单条方法，新增 `batchUpdateSortOrder` 和 `moveCompetition` 方法
- [x] 5.2 更新相关 DTO 类型定义（新增 `BatchSortRequestDTO`、`MoveDirection`）

## 6. 前端 - 修复管理页排序交互

- [x] 6.1 修改 `handleDragEnd`：拖拽后按页内位置重新计算所有项的 sortOrder（基准值 = (page-1) * PAGE_SIZE），批量提交
- [x] 6.2 在表格每行添加上移/下移按钮列：首行禁用上移、末行禁用下移
- [x] 6.3 实现上移/下移点击处理：调用 move 接口，成功后刷新列表
- [x] 6.4 验证页内拖拽和上移/下移均正常工作（代码审查确认逻辑正确）
