package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 角色值对象
 */
@Getter
@AllArgsConstructor
@Builder
public class RoleVO {
    private Long id;
    private String name;
}
