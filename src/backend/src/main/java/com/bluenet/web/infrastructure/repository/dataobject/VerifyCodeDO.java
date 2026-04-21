package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
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
@TableName("tb_verify_code")
public class VerifyCodeDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 验证码发送目标，例如邮箱地址。
     */
    private String target;
    /**
     * 验证码、模板编码或业务唯一编码。
     */
    private String code;

    /**
     * 验证码或临时凭证过期时间。
     */
    private LocalDateTime expireAt;
    /**
     * 验证码实际使用时间。
     */
    private LocalDateTime usedAt;

    /**
     * 验证码或模板适用的业务场景。
     */
    private String scene;
}
