package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.vo.QrcodeVO;

import java.util.Optional;

/**
 * 二维码 Repository 接口
 */
public interface QrcodeRepository {

    /**
     * 保存二维码
     *
     * @param qrcode
     *            二维码实体
     */
    void save(QrcodeVO qrcode);

    /**
     * 根据ID查询二维码
     *
     * @param id
     *            二维码ID
     * @return 二维码实体
     */
    Optional<Qrcode> findById(Long id);

    /**
     * 根据文件ID查询二维码
     *
     * @param fileId
     *            文件ID
     * @return 二维码实体
     */
    Optional<Qrcode> findByFileId(Long fileId);
}
