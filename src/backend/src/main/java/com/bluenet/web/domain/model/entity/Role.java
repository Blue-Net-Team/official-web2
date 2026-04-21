package com.bluenet.web.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;

    public static Role buildSuperAdmin(Long id) {
        return Role.builder()
                .id(id)
                .name("超级管理员")
                .build();
    }

    public static Role buildSuperAdmin() {
        return buildSuperAdmin(1L);
    }
}
