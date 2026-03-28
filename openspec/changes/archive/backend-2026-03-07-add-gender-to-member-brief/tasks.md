## 1. DTO 修改

- [x] 1.1 在 `MemberBriefDTO` 中添加 `gender` 字段（类型为 `Gender`，可为空）

## 2. 转换器修改

- [x] 2.1 在 `MemberConverter.toBriefDTO()` 方法中添加 `.gender(vo.getGender())` 映射

## 3. 测试

- [x] 3.1 更新 `MemberConverterTest` 添加 gender 字段映射测试
- [x] 3.2 更新 `MemberControllerIntegrationTest` 验证接口返回性别字段
