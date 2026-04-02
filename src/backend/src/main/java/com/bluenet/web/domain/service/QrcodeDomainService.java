package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;

import java.util.List;

/**
 * 二维码领域服务接口
 */
public interface QrcodeDomainService {

    /**
     * 保存二维码
     *
     * @param fileVO
     *            文件VO
     * @param type
     *            二维码类型
     */
    void saveQrcode(FileVO fileVO, QrcodeType type);

    /**
     * 获取咨询群二维码列表（按ID升序）
     *
     * @return 咨询群二维码列表
     */
    List<Qrcode> getConsultationQrcodes();

    /**
     * 根据ID删除咨询群二维码（含关联文件删除）
     *
     * @param id
     *            二维码ID
     */
    void deleteConsultationQrcode(Long id);
}
