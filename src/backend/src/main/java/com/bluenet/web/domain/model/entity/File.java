package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;
    /**
     * 业务分类或枚举类型。
     */
    private FileType type;
    /**
     * 资源访问地址。
     */
    @Deprecated
    private String url;
}
