package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.RoleRepositoryConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

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
     * 按主键查询角色实体。
     *
     * @param id
     *            角色主键。
     * @return 查询到的角色实体；不存在时为空。
     */
    @Override
    public Optional<Role> findById(Long id) {
        return Optional.ofNullable(converter.toEntity(roleMapper.selectById(id)));
    }

    /**
     * 按名称查询角色 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 查询到的角色 结果；不存在时为空。
     */
    @Override
    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(converter.toEntity(roleMapper.selectByName(name)));
    }
}
