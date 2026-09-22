package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库分片-标签关联中间表 Mapper 数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_rag_chunk_tags")
public class KnowledgeChunkTagDO {

    private Long chunkId;

    private Long tagId;
}
