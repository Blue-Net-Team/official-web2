## 1. 数据库迁移

- [x] 1.1 执行SQL脚本添加gender字段到tb_user表
  - 路径: `src/main/resources/db/migration/V4__add_gender_to_tb_user.sql`
  ```sql
  ALTER TABLE tb_user
  ADD COLUMN gender VARCHAR(20) NOT NULL DEFAULT 'unknown'
  COMMENT '性别：male-男, female-女, unknown-未知';
  ```

## 2. 枚举定义

- [x] 2.1 创建 `Gender.java` 枚举类
  - 路径: `src/main/java/com/bluenet/web/domain/model/enumerate/Gender.java`
  - 包含 MALE("male", "男"), FEMALE("female", "女"), UNKNOWN("unknown", "未知")
  - 使用 `@EnumValue` 注解标记 value 字段
  - 遵循现有枚举模式（参考 Direction.java）

## 3. 实体类更新

- [x] 3.1 更新 `User.java` 实体类
  - 路径: `src/main/java/com/bluenet/web/domain/model/entity/User.java`
  - 添加 `private Gender gender;` 字段
  - 确保与现有字段风格一致

## 4. DTO/VO 更新

- [x] 4.1 更新 `UserVO.java`
  - 路径: `src/main/java/com/bluenet/web/domain/model/vo/user/UserVO.java`
  - 添加 `private Gender gender;` 字段

- [x] 4.2 更新 `UserInfo.java`
  - 路径: `src/main/java/com/bluenet/web/api/dto/user/UserInfo.java`
  - 添加 `private Gender gender;` 字段

## 5. 验证与测试

- [x] 5.1 编译项目确保无语法错误
- [x] 5.2 验证数据库迁移成功，现有数据的gender字段为默认值 'unknown'
- [x] 5.3 测试用户查询接口返回包含gender字段
- [x] 5.4 测试用户更新接口可正确设置gender字段
- [x] 5.5 验证枚举值映射正确（male/female/unknown）
- [x] 5.6 测试不传gender时默认使用unknown

## 6. 文档更新

- [x] 6.1 更新API文档（如有），说明gender字段的取值范围
- [x] 6.2 在代码注释中说明gender字段的用途和取值
