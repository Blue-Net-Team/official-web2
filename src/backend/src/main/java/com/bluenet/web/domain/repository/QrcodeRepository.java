package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.QrcodeVO;

import java.util.List;
import java.util.Optional;

/**
 * 二维码 Repository 接口
 */
public interface QrcodeRepository {

    /**
     * 保存新的二维码 记录。
     *
     * @param qrcode
     *            二维码领域对象或视图对象。
     */
    void save(QrcodeVO qrcode);

    /**
     * 按主键查询二维码 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的二维码 结果；不存在时为空。
     */
    Optional<Qrcode> findById(Long id);

    /**
     * 按文件主键查询关联记录。
     *
     * @param fileId
     *            文件主键。
     * @return 查询到的二维码 结果；不存在时为空。
     */
    Optional<Qrcode> findByFileId(Long fileId);

    /**
     * 按业务类型查询二维码 记录。
     *
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的二维码 结果集合。
     */
    List<Qrcode> findByType(QrcodeType type);

    /**
     * 删除指定二维码 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);
}
