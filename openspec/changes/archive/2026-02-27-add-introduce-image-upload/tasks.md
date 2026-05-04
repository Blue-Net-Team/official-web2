## 1. 领域层修改

- [x] 1.1 IntroduceImageDomainService 新增 addIntroduceImage 方法
- [x] 1.2 IntroduceImageDomainServiceImpl 实现 addIntroduceImage 方法
- [x] 1.3 CompetitionDomainService 新增 updateLogo 方法
- [x] 1.4 CompetitionDomainServiceImpl 实现 updateLogo 方法

## 2. 应用层修改

- [x] 2.1 FileService 新增 uploadIntroduceImage 方法声明
- [x] 2.2 FileService 新增 uploadCompetitionImage 方法声明
- [x] 2.3 FileService 新增 uploadCompetitionLogo 方法声明
- [x] 2.4 FileServiceImpl 实现 uploadIntroduceImage 方法
- [x] 2.5 FileServiceImpl 实现 uploadCompetitionImage 方法
- [x] 2.6 FileServiceImpl 实现 uploadCompetitionLogo 方法

## 3. 控制层修改

- [x] 3.1 FileUploadController 新增 uploadIntroduceImage 接口
- [x] 3.2 FileUploadController 新增 uploadCompetitionImage 接口
- [x] 3.3 FileUploadController 新增 uploadCompetitionLogo 接口

## 4. 测试验证

- [x] 4.1 编译通过无错误
- [x] 4.2 验证介绍图片上传接口功能
- [x] 4.3 验证竞赛合照上传接口功能
- [x] 4.4 验证竞赛Logo上传接口功能

## 5. 测试覆盖

- [x] 5.1 FileServiceImpl 单元测试（13个用例）
- [x] 5.2 IntroduceImageDomainServiceImpl 单元测试（14个用例）
- [x] 5.3 CompetitionDomainServiceImpl 单元测试（17个用例）
- [x] 5.4 FileUploadController 集成测试（21个用例）
