## 1. 数据库迁移

- [x] 1.1 创建V14__add_profile_fields.sql迁移文件，添加bio字段到tb_user表
- [x] 1.2 执行数据库迁移验证字段添加成功

## 2. 领域层实现

- [x] 2.1 创建ProfileVO值对象，包含用户基本信息、Tab计数
- [x] 2.2 创建ExperienceVO值对象，包含经历详情
- [x] 2.3 创建ProjectContent、CompetitionContent、InternshipContent JSON内容类
- [x] 2.4 扩展UserDomainService，添加用户画像查询和更新方法
- [x] 2.5 创建UserExperienceDomainService接口和实现类

## 3. 仓库层实现

- [x] 3.1 扩展UserRepository，添加查询用户画像方法
- [x] 3.2 创建UserExperienceRepository接口和实现类
- [x] 3.3 扩展User实体，添加bio字段

## 4. 应用层实现

- [x] 4.1 扩展UserInfoService，添加getProfile、updateProfile方法
- [x] 4.2 创建UserExperienceService应用服务
- [x] 4.3 实现getExperiences方法，返回经历列表
- [x] 4.4 实现createExperience方法，创建经历
- [x] 4.5 实现updateExperience方法，更新经历
- [x] 4.6 实现deleteExperience方法，删除经历

## 5. 控制层实现

### UserInfoController扩展
- [x] 5.1 实现GET /api/v1/user/profile接口
- [x] 5.2 实现PUT /api/v1/user/profile接口
- [x] 5.3 创建ProfileDTO、UpdateProfileRequestDTO数据传输对象

### UserExperienceController新建
- [x] 5.4 创建UserExperienceController控制器
- [x] 5.5 实现GET /api/v1/user/experiences接口
- [x] 5.6 实现POST /api/v1/user/experiences接口
- [x] 5.7 实现PUT /api/v1/user/experiences/{id}接口
- [x] 5.8 实现DELETE /api/v1/user/experiences/{id}接口
- [x] 5.9 创建ExperienceDTO、CreateExperienceRequestDTO、UpdateExperienceRequestDTO数据传输对象

### 通用
- [x] 5.10 添加@RequiresPermission注解和API文档注解

## 6. 权限配置

- [x] 6.1 添加profile相关权限到tb_permission表（通过@RequiresPermission注解自动扫描）
- [x] 6.2 配置角色权限关联（所有登录用户自动获得权限，AccessLevel.AUTHENTICATED）

## 7. 测试

- [x] 7.1 编写UserDomainService扩展方法单元测试
- [x] 7.2 编写UserExperienceDomainServiceTest单元测试
- [x] 7.3 编写UserInfoController扩展接口集成测试
- [x] 7.4 编写UserExperienceControllerTest集成测试
- [x] 7.5 执行所有测试验证功能正确性

## 8. 文档与验证

- [x] 8.1 验证Swagger API文档生成正确
- [x] 8.2 验证前后端数据格式一致性
