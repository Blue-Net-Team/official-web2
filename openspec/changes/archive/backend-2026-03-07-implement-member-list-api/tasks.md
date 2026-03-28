## 1. 数据传输对象

- [x] 1.1 创建 MemberBriefDTO 成员简要信息DTO
- [x] 1.2 创建 MemberDetailDTO 成员详细信息DTO
- [x] 1.3 创建 DirectionLeaderDTO 方向负责人DTO
- [x] 1.4 创建 MemberListQueryDTO 列表查询参数DTO
- [x] 1.5 创建 ResponseMessage 泛型响应包装类（已存在，跳过）

## 2. 仓库层实现

- [x] 2.1 扩展 UserRepository 接口，添加成员查询方法
- [x] 2.2 实现 UserRepositoryImpl 中的成员查询方法
- [x] 2.3 添加分页查询成员列表方法（按入学年份降序排序）
- [x] 2.4 添加按方向筛选成员方法
- [x] 2.5 添加查询方向负责人方法
- [x] 2.6 实现入学年份推断逻辑（从学号前4位提取）

## 3. 领域层实现

- [x] 3.1 创建 MemberDomainService 接口
- [x] 3.2 实现 MemberDomainServiceImpl 领域服务
- [x] 3.3 实现获取成员列表方法（含分页、方向筛选）
- [x] 3.4 实现获取成员详情方法
- [x] 3.5 实现获取方向负责人方法

## 4. 应用层实现

- [x] 4.1 创建 MemberService 接口
- [x] 4.2 实现 MemberServiceImpl 应用服务
- [x] 4.3 创建 MemberConverter 转换器（VO → DTO）
- [x] 4.4 实现获取成员列表方法
- [x] 4.5 实现获取成员详情方法
- [x] 4.6 实现获取方向负责人方法

## 5. 控制层实现

- [x] 5.1 创建 MemberController 公开接口控制器
- [x] 5.2 实现获取成员列表接口 (GET /api/v1/members)
- [x] 5.3 实现获取成员详情接口 (GET /api/v1/members/{id})
- [x] 5.4 实现获取方向负责人接口 (GET /api/v1/members/direction-leaders)
- [x] 5.5 添加 Swagger/OpenAPI 文档注解

## 6. 权限配置

- [x] 6.1 添加 member:list 权限（公开）
- [x] 6.2 添加 member:detail 权限（公开）
- [x] 6.3 添加 member:direction-leaders 权限（公开）

## 7. 测试

- [x] 7.1 编译检查通过
- [x] 7.2 编写 MemberDomainService 单元测试
- [x] 7.3 编写 MemberService 单元测试
- [x] 7.4 编写 MemberController 集成测试
- [x] 7.5 测试分页功能
- [x] 7.6 测试方向筛选功能
- [x] 7.7 测试方向负责人查询功能
- [x] 7.8 测试入学年份排序功能（新人在前）
