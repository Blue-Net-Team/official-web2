package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_qrcode")
public class QrcodeDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联文件记录标识。
     */
    private Long fileId;
    /**
     * 业务分类或枚举类型。
     */
    private QrcodeType type;

    /**
     * 考核批次或轮次编号。
     */
    private Integer epoch;
    /**
     * 用户或考核所属技术方向。
     */
    private String direction;

    /**
     * 资源或经历是否对外公开展示。
     */
    private Boolean isShared;
}
