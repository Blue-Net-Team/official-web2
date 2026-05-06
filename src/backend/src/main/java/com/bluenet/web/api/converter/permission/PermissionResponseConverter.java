package com.bluenet.web.api.converter.permission;

import com.bluenet.web.api.dto.permission.PermissionDTO;
import com.bluenet.web.api.dto.permission.PermissionTreeDTO;
import com.bluenet.web.application.PermissionResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限响应转换器
 * <p>
 * 负责将权限 Result 转换为接口 DTO
 * </p>
 */
@Component
public class PermissionResponseConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public PermissionDTO toDTO(PermissionResult result) {
        return PermissionDTO.builder()
                .id(result.id())
                .value(result.value())
                .name(result.name())
                .url(result.url())
                .method(result.method())
                .accessLevel(result.accessLevel())
                .assignedRoles(result.assignedRoles() != null ? result.assignedRoles() : List.of())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<PermissionDTO> toDTOList(List<PermissionResult> results) {
        return results.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将权限结果列表构建为树形结构 DTO
     * <p>
     * 按权限标识符的冒号分隔符构建层级，例如 assessment:create → [assessment → create]
     * </p>
     */
    public List<PermissionTreeDTO> buildPermissionTree(List<PermissionResult> permissions) {
        Map<String, PermissionTreeDTO> nodeMap = new HashMap<>();
        List<PermissionTreeDTO> rootNodes = new ArrayList<>();

        for (PermissionResult permission : permissions) {
            String[] parts = permission.value().split(":");
            StringBuilder currentPath = new StringBuilder();
            PermissionTreeDTO parentNode = null;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                String nodeKey = currentPath.length() == 0 ? part : currentPath + ":" + part;

                if (!nodeMap.containsKey(nodeKey)) {
                    PermissionTreeDTO node = PermissionTreeDTO.builder()
                            .key(nodeKey)
                            .title(part)
                            .leaf(i == parts.length - 1)
                            .children(new ArrayList<>())
                            .permissionCount(0)
                            .build();

                    if (i == parts.length - 1) {
                        node.setValue(permission.value());
                        node.setPermissionId(permission.id());
                        node.setPermissionCount(1);
                        node.setAccessLevel(permission.accessLevel());
                    }

                    nodeMap.put(nodeKey, node);

                    if (parentNode == null) {
                        rootNodes.add(node);
                    } else {
                        parentNode.getChildren().add(node);
                    }
                }

                parentNode = nodeMap.get(nodeKey);

                if (currentPath.length() > 0) {
                    currentPath.append(":");
                }
                currentPath.append(part);
            }
        }

        for (PermissionTreeDTO node : nodeMap.values()) {
            if (!node.isLeaf()) {
                node.setPermissionCount(countPermissions(node));
            }
        }

        return rootNodes;
    }

    private int countPermissions(PermissionTreeDTO node) {
        if (node.isLeaf()) {
            return 1;
        }
        int total = 0;
        for (PermissionTreeDTO child : node.getChildren()) {
            total += countPermissions(child);
        }
        return total;
    }
}
