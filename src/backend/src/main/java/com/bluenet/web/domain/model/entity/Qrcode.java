package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Qrcode {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;

    private Long fileId; // 外键连接到File表
    private QrcodeType type; // 二维码类型

    // 以下字段仅 ASSESSMENT 类型使用
    private Integer epoch; // 考核轮次
    private String direction; // 方向
    private Boolean isShared; // 是否三方向共用

    public Qrcode() {
    }

    // 用于咨询群的简化构造
    public static Qrcode forConsultation(Long fileId) {
        Qrcode qrcode = new Qrcode();
        qrcode.fileId = fileId;
        qrcode.type = QrcodeType.CONSULTATION;
        return qrcode;
    }
}
