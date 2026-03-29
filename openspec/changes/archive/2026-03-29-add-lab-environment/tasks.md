## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本 V20__add_venue_and_equipment_tables.sql
- [x] 1.2 在迁移脚本中创建 tb_venue 表（id, name, subtitle, description, image_file_id, sort_order）
- [x] 1.3 在迁移脚本中创建 tb_equipment 表（id, name, brand, description, image_file_id, sort_order）
- [x] 1.4 添加必要的索引和注释

## 2. 后端 - 场地管理（Venue）

- [x] 2.1 创建 Venue 实体类（domain/model/entity/Venue.java）
- [x] 2.2 创建 VenueVO 值对象（domain/model/vo/VenueVO.java）
- [x] 2.3 创建 VenueRepository 接口（domain/repository/VenueRepository.java）
- [x] 2.4 创建 VenueDomainService 接口和实现（domain/service/VenueDomainService.java）
- [x] 2.5 创建 VenueMapper（infrastructure/repository/mapper/VenueMapper.java）
- [x] 2.6 创建 VenueRepositoryImpl（infrastructure/repository/impl/VenueRepositoryImpl.java）
- [x] 2.7 创建 VenueDTO 和请求 DTO（api/dto/venue/）
- [x] 2.8 创建 VenueConverter（application/converter/VenueConverter.java）
- [x] 2.9 创建 VenueService 接口和实现（application/service/VenueService.java）
- [x] 2.10 创建 VenueController 公开接口（api/controller/v1/VenueController.java）
- [x] 2.11 创建 AdminVenueController 管理接口（api/controller/v1/admin/AdminVenueController.java）

## 3. 后端 - 设备管理（Equipment）

- [x] 3.1 创建 Equipment 实体类（domain/model/entity/Equipment.java）
- [x] 3.2 创建 EquipmentVO 值对象（domain/model/vo/EquipmentVO.java）
- [x] 3.3 创建 EquipmentRepository 接口（domain/repository/EquipmentRepository.java）
- [x] 3.4 创建 EquipmentDomainService 接口和实现（domain/service/EquipmentDomainService.java）
- [x] 3.5 创建 EquipmentMapper（infrastructure/repository/mapper/EquipmentMapper.java）
- [x] 3.6 创建 EquipmentRepositoryImpl（infrastructure/repository/impl/EquipmentRepositoryImpl.java）
- [x] 3.7 创建 EquipmentDTO 和请求 DTO（api/dto/equipment/）
- [x] 3.8 创建 EquipmentConverter（application/converter/EquipmentConverter.java）
- [x] 3.9 创建 EquipmentService 接口和实现（application/service/EquipmentService.java）
- [x] 3.10 创建 EquipmentController 公开接口（api/controller/v1/EquipmentController.java）
- [x] 3.11 创建 AdminEquipmentController 管理接口（api/controller/v1/admin/AdminEquipmentController.java）

## 4. 后端 - 枚举修改

- [x] 4.1 修改 ImageType 枚举，移除 LABORATORY 和 EQUIPMENT
- [x] 4.2 更新 IntroduceImage 相关代码以适配枚举变更

## 5. 后端 - 权限配置

- [x] 5.1 添加场地管理相关权限（venue:create, venue:update, venue:delete）
- [x] 5.2 添加设备管理相关权限（equipment:create, equipment:update, equipment:delete）

## 6. 前端 - API 服务

- [x] 6.1 创建 venue.ts API 服务（获取场地列表）
- [x] 6.2 创建 equipment.ts API 服务（获取设备列表）
- [x] 6.3 创建类型定义（Venue, Equipment 接口）

## 7. 前端 - 页面实现

- [x] 7.1 创建 /lab-environment 路由页面
- [x] 7.2 实现 Hero 区域组件（固定文本）
- [x] 7.3 实现场地展示区组件（VenueSection）
- [x] 7.4 实现设备展示区组件（EquipmentSection）
- [x] 7.5 实现场地卡片组件（VenueCard）
- [x] 7.6 实现设备卡片组件（EquipmentCard）
- [x] 7.7 添加加载状态和空状态处理
- [x] 7.8 实现响应式布局适配

## 8. 测试

- [x] 8.1 编写 VenueDomainService 单元测试
- [x] 8.2 编写 EquipmentDomainService 单元测试
- [x] 8.3 编写 VenueController 集成测试
- [x] 8.4 编写 EquipmentController 集成测试
- [x] 8.5 验证前端页面渲染正确
