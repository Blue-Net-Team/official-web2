## 1. 值对象与数据传输对象

- [x] 1.1 创建 CollegeVO 值对象
- [x] 1.2 创建 CollegeDTO 数据传输对象
- [x] 1.3 创建 CreateCollegeRequestDTO 请求对象
- [x] 1.4 创建 UpdateCollegeRequestDTO 请求对象
- [x] 1.5 创建 ResponseMessage 泛型响应包装类（已存在，跳过）

## 2. 仓库层实现

- [x] 2.1 创建 CollegeRepository 接口
- [x] 2.2 实现 CollegeRepositoryImpl 仓库实现类
- [x] 2.3 添加学院名称唯一性检查方法
- [x] 2.4 添加关联用户/报名检查方法

## 3. 领域层实现

- [x] 3.1 创建 CollegeDomainService 接口
- [x] 3.2 实现 CollegeDomainServiceImpl 领域服务
- [x] 3.3 实现获取所有学院方法
- [x] 3.4 实现创建学院方法（含名称唯一性校验）
- [x] 3.5 实现更新学院方法
- [x] 3.6 实现删除学院方法（含关联检查）

## 4. 应用层实现

- [x] 4.1 创建 CollegeService 接口
- [x] 4.2 实现 CollegeServiceImpl 应用服务
- [x] 4.3 创建 CollegeConverter 转换器
- [x] 4.4 实现获取学院列表方法
- [x] 4.5 实现创建学院方法
- [x] 4.6 实现更新学院方法
- [x] 4.7 实现删除学院方法

## 5. 控制层实现

- [x] 5.1 创建 CollegeController 公开接口控制器
- [x] 5.2 创建 AdminCollegeController 管理接口控制器
- [x] 5.3 实现获取学院列表接口 (GET /api/v1/colleges)
- [x] 5.4 实现创建学院接口 (POST /api/v1/admin/colleges)
- [x] 5.5 实现更新学院接口 (PUT /api/v1/admin/colleges/{id})
- [x] 5.6 实现删除学院接口 (DELETE /api/v1/admin/colleges/{id})
- [x] 5.7 添加 Swagger/OpenAPI 文档注解

## 6. 权限配置

- [x] 6.1 添加 college:list 权限（公开）
- [x] 6.2 添加 college:create 权限（管理员）
- [x] 6.3 添加 college:update 权限（管理员）
- [x] 6.4 添加 college:delete 权限（管理员）

## 7. 测试

- [x] 7.1 编写 CollegeRepository 单元测试
- [x] 7.2 编写 CollegeDomainService 单元测试
- [x] 7.3 编写 CollegeService 单元测试
- [x] 7.4 编写 CollegeController 集成测试
- [x] 7.5 编写 AdminCollegeController 集成测试
