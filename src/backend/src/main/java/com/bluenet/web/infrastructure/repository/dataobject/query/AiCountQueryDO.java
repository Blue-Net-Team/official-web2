package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

/**
 * 通用分布计数投影查询数据对象。
 *
 * <p>
 * 供意图分布、动作分布、工具使用分布与拒答原因分布复用。
 * </p>
 */
@Data
public class AiCountQueryDO {
    /**
     * 计数维度名称，可能为 null（例如事件的意图字段缺失）。
     */
    private String label;
    /**
     * 次数。
     */
    private Long count;
}
