package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.infrastructure.repository.handler.PgVectorTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库标签 Mapper 数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_rag_tags")
public class KnowledgeTagDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    @TableField(typeHandler = PgVectorTypeHandler.class)
    private float[] tagVector;

    private String tagDescription;

    private Integer chunksCount;
}
