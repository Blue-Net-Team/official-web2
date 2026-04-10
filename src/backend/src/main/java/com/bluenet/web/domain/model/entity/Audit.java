package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.infrastructure.repository.handler.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "tb_audit", autoResultMap = true)
public class Audit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestMethod;
    private String requestUri;
    private String requestUriPattern;
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String actionArg;
    private Long actionUserId;
    private LocalDateTime actionTime;
    private String ipAddress;
    private String userAgent;
    private Integer httpStatus;
    private String responseMessage;
    private String stackTrace;
    private Long durationMs;
    private Boolean successState;
}
