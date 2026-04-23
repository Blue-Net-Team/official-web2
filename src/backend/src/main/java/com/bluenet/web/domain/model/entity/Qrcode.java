package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    /**
     * 构造新二维码聚合根 —— 带领域校验
     *
     * @param fileId
     *            关联文件ID
     * @param type
     *            二维码类型
     * @return 新的二维码实体
     * @throws IllegalArgumentException
     *             如果类型为空
     */
    public static Qrcode create(Long fileId, QrcodeType type) {
        if (type == null) {
            throw new IllegalArgumentException("二维码类型不能为空");
        }
        return new Qrcode(null, fileId, type, null, null, null);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            二维码ID
     * @param fileId
     *            关联文件ID
     * @param type
     *            二维码类型
     * @param epoch
     *            考核轮次
     * @param direction
     *            方向
     * @param isShared
     *            是否三方向共用
     * @return 重建的二维码实体
     */
    public static Qrcode reconstruct(Long id, Long fileId, QrcodeType type, Integer epoch, String direction,
            Boolean isShared) {
        return new Qrcode(id, fileId, type, epoch, direction, isShared);
    }

    // 用于咨询群的简化构造
    public static Qrcode forConsultation(Long fileId) {
        return create(fileId, QrcodeType.CONSULTATION);
    }
}
