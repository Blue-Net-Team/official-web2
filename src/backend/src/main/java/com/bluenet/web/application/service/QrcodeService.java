package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import org.springframework.web.multipart.MultipartFile;

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
     * 上传咨询群二维码
     *
     * @param file
     *            二维码图片文件
     * @return 文件信息
     */
    FileInfo uploadConsultationQrcode(MultipartFile file);

    /**
     * 删除咨询群二维码
     *
     * @param id
     *            二维码ID
     */
    void deleteConsultationQrcode(Long id);
}
