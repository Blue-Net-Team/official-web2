package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.repository.QrcodeRepository;
import com.bluenet.web.infrastructure.repository.converter.QrcodeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.QrcodeDO;
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 二维码 Repository 实现
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class QrcodeRepositoryImpl implements QrcodeRepository {

    private final QrcodeMapper qrcodeMapper;
    private final QrcodeRepositoryConverter converter;

    /**
     * 保存新的二维码 记录。
     *
     * @param qrcode
     *            二维码领域对象。
     */
    @Override
    public void save(Qrcode qrcode) {
        log.info("保存二维码: {}", qrcode);
        QrcodeDO dataObject = converter.toDataObject(qrcode);
        if (qrcode.getId() == null) {
            qrcodeMapper.insert(dataObject);
            qrcode.setId(dataObject.getId());
        } else {
            qrcodeMapper.updateById(dataObject);
        }
    }

    /**
     * 按主键查询二维码 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的二维码 结果；不存在时为空。
     */
    @Override
    public Optional<Qrcode> findById(Long id) {
        Qrcode qrcode = converter.toEntity(qrcodeMapper.selectById(id));
        if (qrcode == null) {
            log.warn("二维码不存在: id={}", id);
            return Optional.empty();
        }
        return Optional.of(qrcode);
    }

    /**
     * 按文件主键查询关联记录。
     *
     * @param fileId
     *            文件主键。
     * @return 查询到的二维码 结果；不存在时为空。
     */
    @Override
    public Optional<Qrcode> findByFileId(Long fileId) {
        Qrcode qrcode = converter.toEntity(qrcodeMapper.selectByFileId(fileId));
        if (qrcode == null) {
            log.warn("二维码不存在: fileId={}", fileId);
            return Optional.empty();
        }
        return Optional.of(qrcode);
    }

    /**
     * 按业务类型查询二维码 记录。
     *
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的二维码 结果集合。
     */
    @Override
    public List<Qrcode> findByType(QrcodeType type) {
        return converter.toEntityList(qrcodeMapper.selectByType(type));
    }

    /**
     * 删除指定二维码 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        qrcodeMapper.deleteById(id);
        log.info("删除二维码: id={}", id);
    }

}
