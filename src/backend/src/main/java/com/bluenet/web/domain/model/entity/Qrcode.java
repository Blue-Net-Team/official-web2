package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@TableName("tb_qrcode")
public class Qrcode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId; // 外键连接到File表
    private QrcodeType type; // 二维码类型

}
