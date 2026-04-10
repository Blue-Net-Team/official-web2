package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_verify_code")
public class VerifyCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String target;
    private String code;
    private LocalDateTime expireAt;
    private LocalDateTime usedAt;
    private String scene;
}
