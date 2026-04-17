package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.permission.PermissionDTO;
import com.bluenet.web.api.dto.permission.PermissionTreeDTO;
import com.bluenet.web.domain.model.vo.PermissionVO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionConverter {

    public PermissionDTO convertToDTO(PermissionVO vo, List<String> assignedRoles) {
        return PermissionDTO.builder()
                .id(vo.getId())
                .value(vo.getValue())
                .name(vo.getName())
                .url(vo.getUrl())
                .method(vo.getMethod())
                .assignedRoles(assignedRoles != null ? assignedRoles : List.of())
                .build();
    }

    public List<PermissionDTO> convertToDTOList(List<PermissionVO> voList, Map<Long, List<String>> rolesMap) {
        return voList.stream()
                .map(vo -> convertToDTO(vo, rolesMap.getOrDefault(vo.getId(), List.of())))
                .collect(Collectors.toList());
    }

    /**
     * 将权限 VO 列表构建为树形结构 DTO
     * <p>
     * 按权限标识符的冒号分隔符构建层级，例如 assessment:create → [assessment → create]
     * </p>
     */
    public List<PermissionTreeDTO> buildPermissionTree(List<PermissionVO> permissions) {
        Map<String, PermissionTreeDTO> nodeMap = new HashMap<>();
        List<PermissionTreeDTO> rootNodes = new ArrayList<>();

        for (PermissionVO permission : permissions) {
            String[] parts = permission.getValue().split(":");
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
                        node.setValue(permission.getValue());
                        node.setPermissionId(permission.getId());
                        node.setPermissionCount(1);
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
