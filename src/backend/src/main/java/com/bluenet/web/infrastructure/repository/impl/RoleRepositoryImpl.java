package com.bluenet.web.infrastructure.repository.impl;

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

    @Override
    public Optional<RoleVO> findByName(String name) {
        Role role = roleMapper.selectByName(name);
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
