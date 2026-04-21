package com.bluenet.web.infrastructure.security.scanner;

import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;
import com.bluenet.web.infrastructure.security.util.PermissionResolver;
import com.bluenet.web.infrastructure.security.util.PermissionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限扫描器 启动时扫描所有Controller方法的@RequiresPermission注解，同步到数据库 支持批量处理和物理删除幽灵数据
 */
@Component
@Order(100) // 在其他组件初始化之后执行
public class PermissionScanner implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(PermissionScanner.class);

    private final RequestMappingHandlerMapping handlerMapping;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public PermissionScanner(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
            PermissionMapper permissionMapper,
            RolePermissionMapper rolePermissionMapper) {
        this.handlerMapping = handlerMapping;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Starting permission scan...");
        long startTime = System.currentTimeMillis();

        try {
            scanAndSync();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Permission scan completed in {}ms", duration);
        } catch (Exception e) {
            logger.error("Permission scan failed", e);
            throw new RuntimeException("Failed to scan permissions", e);
        }
    }

    /**
     * 扫描并同步权限
     */
    private void scanAndSync() {
        // 1. 扫描所有Controller方法
        List<PermissionInfo> scannedPermissions = scanControllerMethods();
        logger.info("Found {} controller methods with @RequiresPermission", scannedPermissions.size());

        // 2. 批量加载数据库现有权限
        List<Permission> existingPermissions = permissionMapper.selectList(null)
                .stream()
                // 扫描器内部沿用领域对象做差异比较，Mapper 只暴露 PermissionDO。
                .map(permission -> RepositoryObjectConverter.copy(permission, Permission.class))
                .toList();
        Map<String, Permission> existingMap = existingPermissions.stream()
                .collect(Collectors.toMap(Permission::getValue, p -> p, (p1, p2) -> p1));

        logger.info("Database has {} existing permissions", existingPermissions.size());

        // 3. 对比差异
        Set<String> scannedValues = scannedPermissions.stream()
                .map(PermissionInfo::getValue)
                .collect(Collectors.toSet());

        List<PermissionInfo> toInsert = new ArrayList<>();
        List<PermissionInfo> toUpdate = new ArrayList<>();
        List<Permission> toDelete = new ArrayList<>();

        // 找出新增和需要更新的
        for (PermissionInfo info : scannedPermissions) {
            Permission existing = existingMap.get(info.getValue());
            if (existing == null) {
                toInsert.add(info);
            } else if (!isSame(info, existing)) {
                toUpdate.add(info);
            }
        }

        // 找出需要删除的（幽灵数据）
        for (Permission existing : existingPermissions) {
            if (!scannedValues.contains(existing.getValue())) {
                toDelete.add(existing);
            }
        }

        // 4. 执行数据库操作
        performDatabaseOperations(toInsert, toUpdate, toDelete);

        // 5. 记录日志
        logger.info("New permissions: {}", toInsert.size());
        toInsert.forEach(p -> logger.info("  - New: {}", p.getValue()));

        logger.info("Updated permissions: {}", toUpdate.size());
        toUpdate.forEach(p -> logger.info("  - Updated: {}", p.getValue()));

        logger.info("Deleted permissions (ghost data): {}", toDelete.size());
        toDelete.forEach(p -> logger.info("  - Deleted: {}", p.getValue()));
    }

    /**
     * 扫描Controller方法
     */
    private List<PermissionInfo> scanControllerMethods() {
        List<PermissionInfo> permissions = new ArrayList<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // 解析权限注解
            PermissionResolver.PermissionInfo permissionInfo = PermissionResolver.resolve(handlerMethod);

            if (!permissionInfo.hasPermission()) {
                continue;
            }

            // 验证权限值格式（直接验证字符串，不创建注解实例）
            PermissionValidator.validate(
                    permissionInfo.getValue(),
                    handlerMethod.getBeanType().getName(),
                    handlerMethod.getMethod().getName());

            // 提取URL（只取第一个）
            String url = extractUrl(mappingInfo);

            // 提取HTTP方法
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            if (methods.isEmpty()) {
                methods = EnumSet.of(RequestMethod.GET); // 默认GET
            }

            // 为每个HTTP方法创建权限
            for (RequestMethod method : methods) {
                permissions.add(
                        new PermissionInfo(permissionInfo.getValue(), permissionInfo.getName(), url, method.name(),
                                permissionInfo.getAccess().name()));
            }
        }

        return permissions;
    }

    /**
     * 提取URL路径
     */
    private String extractUrl(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPathPatternsCondition() != null) {
            return mappingInfo.getPathPatternsCondition().getPatternValues().iterator().next();
        }
        return "";
    }

    /**
     * 检查权限信息是否与数据库记录相同
     */
    private boolean isSame(PermissionInfo info, Permission existing) {
        return Objects.equals(info.getName(), existing.getName()) && Objects.equals(info.getUrl(), existing.getUrl())
                && Objects.equals(info.getMethod(), existing.getMethod())
                && Objects.equals(info.getAccessLevel(), existing.getAccessLevel());
    }

    /**
     * 执行数据库操作
     */
    private void performDatabaseOperations(List<PermissionInfo> toInsert, List<PermissionInfo> toUpdate,
            List<Permission> toDelete) {
        // 插入新权限
        for (PermissionInfo info : toInsert) {
            PermissionDO permission = new PermissionDO();
            permission.setValue(info.getValue());
            permission.setName(info.getName());
            permission.setUrl(info.getUrl());
            permission.setMethod(info.getMethod());
            permission.setAccessLevel(info.getAccessLevel());
            permissionMapper.insert(permission);
        }

        // 更新现有权限
        for (PermissionInfo info : toUpdate) {
            PermissionDO existing = permissionMapper
                    .selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PermissionDO>()
                                    .eq(PermissionDO::getValue, info.getValue()));
            if (existing != null) {
                existing.setName(info.getName());
                existing.setUrl(info.getUrl());
                existing.setMethod(info.getMethod());
                existing.setAccessLevel(info.getAccessLevel());
                permissionMapper.updateById(existing);
            }
        }

        // 物理删除幽灵数据（同时级联删除role_permission关联）
        for (Permission ghost : toDelete) {
            // 先删除关联
            rolePermissionMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermissionDO>()
                            .eq(RolePermissionDO::getPermissionId, ghost.getId()));
            // 再删除权限
            permissionMapper.deleteById(ghost.getId());
        }
    }

    /**
     * 权限信息内部类
     */
    private static class PermissionInfo {
        private final String value;
        private final String name;
        private final String url;
        private final String method;
        private final String accessLevel;

        public PermissionInfo(String value, String name, String url, String method, String accessLevel) {
            this.value = value;
            this.name = name;
            this.url = url;
            this.method = method;
            this.accessLevel = accessLevel;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public String getAccessLevel() {
            return accessLevel;
        }
    }
}
