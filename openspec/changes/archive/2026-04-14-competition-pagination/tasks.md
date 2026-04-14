## 1. 测试编写（TDD 红灯阶段）

### Task 1: 编写 CompetitionDomainServiceImpl 分页单元测试

#### 测试边界
- 输入条件：page（页码）、size（每页数量）
- 前置状态：Mock CompetitionRepository 返回分页数据
- 后置状态：验证 Repository 被正确调用，返回数据正确转换

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 正常分页查询 | page=0, size=10 | 调用 findCompetitionsPage(PageRequest.of(0,10))，返回对应 Page | - |
| TC-002 | 自定义分页参数 | page=2, size=5 | 调用 findCompetitionsPage(PageRequest.of(2,5)) | - |
| TC-003 | size 超过上限 | page=0, size=100 | Application Service 层 clamp 到 50，调用 findCompetitionsPage(PageRequest.of(0,50)) | - |

#### 实现步骤（严格按顺序）
- [x] 1.1 编写 CompetitionDomainServiceImpl.getCompetitionPage 单元测试（红灯阶段）

### Task 2: 编写 CompetitionServiceImpl 分页单元测试

#### 测试边界
- 输入条件：page、size 参数（含 null 默认值处理）
- 前置状态：Mock CompetitionDomainService 和 CompetitionConverter
- 后置状态：验证 Page<CompetitionVO> → PageDTO<CompetitionResponseDTO> 转换正确

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-004 | 默认参数 | page=null, size=null | 使用默认 page=0, size=10 | - |
| TC-005 | 自定义参数 | page=1, size=5 | 透传参数，返回 PageDTO | - |
| TC-006 | size 超上限 clamp | page=0, size=100 | size 被 clamp 为 50 | - |
| TC-007 | 空数据 | page=0, size=10 | 返回空 PageDTO（content=[], totalElements=0） | - |

#### 实现步骤（严格按顺序）
- [x] 2.1 编写 CompetitionServiceImpl.getCompetitionPage 单元测试（红灯阶段）

### Task 3: 编写 CompetitionController 分页集成测试

#### 测试边界
- 输入条件：HTTP GET 请求到 `/api/v1/competitions/page`
- 前置状态：数据库中有竞赛数据
- 后置状态：返回正确的 PageDTO 格式响应

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-008 | 默认参数请求 | GET /api/v1/competitions/page | HTTP 200，PageDTO 格式，page=0, size=10 | - |
| TC-009 | 自定义分页参数 | GET /api/v1/competitions/page?page=0&size=1 | HTTP 200，content 含1条记录 | - |
| TC-010 | 验证排序 | 多条不同 sort_order 的数据 | sort_order 大的排在前面 | - |
| TC-011 | 验证字段完整性 | 返回竞赛数据 | 包含 id/name/shortName/level/month/organizer/summary/logoFileId/coverFileId | - |
| TC-012 | 无数据 | 空数据库 | HTTP 200，content=[]，totalElements=0 | - |

#### 实现步骤（严格按顺序）
- [x] 3.1 编写 CompetitionController 分页接口集成测试（红灯阶段）

## 4. DDD 分层实现

### Task 4: 基础设施层 - Mapper 分页查询

#### 测试边界
- 输入条件：MyBatis-Plus Page 对象
- 前置状态：数据库有竞赛数据
- 后置状态：返回 IPage<CompetitionVO>，包含正确的分页数据和总数

#### 实现步骤（严格按顺序）
- [x] 4.1 在 CompetitionMapper.java 中新增 `IPage<CompetitionVO> selectCompetitionsPage(Page<CompetitionVO> page)` 方法
- [x] 4.2 在 CompetitionMapper.xml 中新增分页 SQL（复用 CompetitionVOResultMap，排序 sort_order DESC, created_at DESC）

### Task 5: 基础设施层 - Repository 分页方法

#### 实现步骤（严格按顺序）
- [x] 5.1 在 CompetitionRepository 接口中新增 `Page<CompetitionVO> findCompetitionsPage(Pageable pageable)` 方法
- [x] 5.2 在 CompetitionRepositoryImpl 中实现分页方法（MyBatis-Plus IPage → Spring Data Page 转换）

### Task 6: 领域层 - Domain Service 分页方法

#### 实现步骤（严格按顺序）
- [x] 6.1 在 CompetitionDomainService 接口中新增 `Page<CompetitionVO> getCompetitionPage(Pageable pageable)` 方法
- [x] 6.2 在 CompetitionDomainServiceImpl 中实现，委托 Repository

### Task 7: 应用层 - Converter 和 Service

#### 实现步骤（严格按顺序）
- [x] 7.1 在 CompetitionConverter 中新增 `Page<CompetitionResponseDTO> convertToDTOPage(Page<CompetitionVO> voPage)` 方法
- [x] 7.2 在 CompetitionService 接口中新增 `PageDTO<CompetitionResponseDTO> getCompetitionPage(Integer page, Integer size)` 方法
- [x] 7.3 在 CompetitionServiceImpl 中实现（参数默认值、clamp、调用 Domain Service、Converter 转换、PageDTO.from）

### Task 8: 接口层 - Controller 端点

#### 实现步骤（严格按顺序）
- [x] 8.1 在 CompetitionController 中新增 `GET /api/v1/competitions/page` 端点，使用 `@RequiresPermission(access = AccessLevel.PUBLIC)`

## 9. 测试验证与重构（绿灯阶段）

- [x] 9.1 运行所有单元测试确认通过
- [x] 9.2 运行集成测试确认通过
- [x] 9.3 运行全量测试确认无回归
- [x] 9.4 重构优化（如有必要）
