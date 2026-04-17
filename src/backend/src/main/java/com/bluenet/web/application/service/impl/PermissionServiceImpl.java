package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.permission.PermissionDTO;
import com.bluenet.web.api.dto.permission.PermissionQueryDTO;
import com.bluenet.web.api.dto.permission.PermissionTreeDTO;
import com.bluenet.web.application.converter.PermissionConverter;
import com.bluenet.web.application.service.PermissionService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.vo.PermissionVO;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionConverter permissionConverter;

    @Override
    public PageDTO<PermissionDTO> getPermissions(PermissionQueryDTO query) {
        int pageNum = query.getPage() != null ? query.getPage() : 0;
        int pageSize = query.getSize() != null ? query.getSize() : 20;
        pageSize = Math.min(Math.max(pageSize, 1), 100);

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<PermissionVO> permissionPage = permissionRepository.findAll(
                query.getKeyword(),
                query.getFormat(),
                pageable);

        if (permissionPage.getContent().isEmpty()) {
            return PageDTO.from(permissionPage.map(vo -> permissionConverter.convertToDTO(vo, List.of())));
        }

        List<Long> permissionIds = permissionPage.getContent()
                .stream()
                .map(PermissionVO::getId)
                .collect(Collectors.toList());
        Map<Long, List<String>> rolesMap = rolePermissionRepository.findRoleNamesByPermissionIds(permissionIds);

        Page<PermissionDTO> dtoPage = permissionPage
                .map(vo -> permissionConverter.convertToDTO(vo, rolesMap.getOrDefault(vo.getId(), List.of())));

        return PageDTO.from(dtoPage);
    }

    @Override
    public PermissionDTO getPermissionDetail(Long id) {
        PermissionVO permission = permissionRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("权限不存在"));

        List<String> assignedRoles = rolePermissionRepository.findRoleNamesByPermissionId(id);
        return permissionConverter.convertToDTO(permission, assignedRoles);
    }

    @Override
    public List<PermissionTreeDTO> getPermissionTree() {
        List<PermissionVO> allPermissions = permissionRepository.findAll();
        return permissionConverter.buildPermissionTree(allPermissions);
    }
}
