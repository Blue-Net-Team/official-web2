package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_audit")
public class Audit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String action;
    private String actionArg;
    private Long actionUserId;
    private LocalDateTime actionTime;
    private String ipAddress;
    private String userAgent;
    private String remarks;
    private Boolean successState;
}
