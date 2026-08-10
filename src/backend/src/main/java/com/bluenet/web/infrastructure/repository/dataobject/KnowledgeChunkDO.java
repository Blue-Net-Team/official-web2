package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.infrastructure.repository.handler.PgTextArrayTypeHandler;
import com.bluenet.web.infrastructure.repository.handler.PgVectorTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库分段 Mapper 数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_rag_chunks")
public class KnowledgeChunkDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long docId;

    @TableField(typeHandler = PgVectorTypeHandler.class)
    private float[] chunkVector;

    private String content;

    @TableField(typeHandler = PgTextArrayTypeHandler.class)
    private List<String> tags;

    private String source;
}
