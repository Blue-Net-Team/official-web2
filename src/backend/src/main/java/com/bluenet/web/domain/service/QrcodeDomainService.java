package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;

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
}
