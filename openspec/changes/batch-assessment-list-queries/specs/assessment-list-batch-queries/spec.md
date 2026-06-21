## ADDED Requirements

### Requirement: 考核列表查询使用批量查询与内存聚合
系统在实现 `GET /api/v1/assessment-times` 接口时，SHALL 通过批量查询获取题目总数、已完成答题数以及淘汰决策所需数据，并在应用层内存中完成聚合，避免对分页内每条记录单独发起数据库查询。

#### Scenario: 列表包含多条考核时只发出固定次数的批量查询
- **WHEN** 用户请求包含 N 条考核记录的列表页
- **THEN** 系统 SHALL 在循环外通过 `IN` 子句批量查询题目总数、已完成答题数、用户淘汰决策及决策关联的考核场次
- **AND** 循环内 SHALL 只从内存中读取已加载的数据，不再访问数据库

#### Scenario: 批量查询结果缺失时默认补零
- **WHEN** 某个考核场次没有题目或当前用户没有答题记录
- **THEN** 系统 SHALL 在内存聚合时将该场次的 `totalQuestions` 和 `completedQuestions` 视为 0
- **AND** 响应数据 SHALL 与逐条查询结果一致

#### Scenario: 淘汰判断基于预加载数据完成
- **WHEN** 当前用户为考生且存在往期淘汰决策
- **THEN** 系统 SHALL 一次性查询该用户的所有淘汰决策及其关联考核场次
- **AND** 对列表中每个考核条目 SHALL 在内存中判断是否存在满足方向和年级匹配且轮次更早的淘汰决策

## MODIFIED Requirements

无。

## REMOVED Requirements

无。

## RENAMED Requirements

无。
