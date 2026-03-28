package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("tb_role")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
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
