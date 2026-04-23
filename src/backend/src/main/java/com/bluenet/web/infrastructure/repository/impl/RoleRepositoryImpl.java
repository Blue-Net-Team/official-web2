package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.RoleRepositoryConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.domain.model.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper roleMapper;
    private final RoleRepositoryConverter converter;

    /**
     * 按名称查询角色 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 查询到的角色 结果；不存在时为空。
     */
    @Override
    public Optional<RoleVO> findByName(String name) {
        Role role = converter.toEntity(roleMapper.selectByName(name));
        if (role == null) {
            return Optional.empty();
        }
        return Optional.of(
                RoleVO.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build());
    }
}
