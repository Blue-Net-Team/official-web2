package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;

import java.util.List;

/**
 * 二维码服务接口
 */
public interface QrcodeService {

    /**
     * 获取咨询群二维码列表
     *
     * @return 咨询群二维码列表
     */
    List<ConsultationQrcodeDTO> getConsultationQrcodes();

    /**
     * 创建二维码记录（通过已上传的 fileId）
     *
     * @param fileId
     *            文件ID（必须为 QRCODE 类型）
     */
    void createQrcode(Long fileId);

    /**
     * 删除咨询群二维码
     *
     * @param id
     *            二维码ID
     */
    void deleteConsultationQrcode(Long id);
}
