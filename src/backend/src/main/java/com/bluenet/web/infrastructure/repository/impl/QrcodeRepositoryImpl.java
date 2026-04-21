package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.repository.QrcodeRepository;
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

    /**
     * 保存新的二维码 记录。
     *
     * @param qrcodeVO
     *            二维码视图对象。
     */
    @Override
    public void save(QrcodeVO qrcodeVO) {
        log.info("保存二维码: {}", qrcodeVO);
        Qrcode qrcode = Qrcode.builder()
                .id(qrcodeVO.getId())
                .fileId(qrcodeVO.getFileId())
                .type(qrcodeVO.getType())
                .build();
        if (qrcode.getId() == null) {
            RepositoryObjectConverter.insert(qrcodeMapper, qrcode, QrcodeDO.class);
        } else {
            RepositoryObjectConverter.updateById(qrcodeMapper, qrcode, QrcodeDO.class);
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
        Qrcode qrcode = RepositoryObjectConverter.toDomain(qrcodeMapper.selectById(id), Qrcode.class);
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
        Qrcode qrcode = RepositoryObjectConverter.toDomain(qrcodeMapper.selectByFileId(fileId), Qrcode.class);
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
        return RepositoryObjectConverter.toDomainList(qrcodeMapper.selectByType(type), Qrcode.class);
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

    /**
     * 在二维码 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param qrcode
     *            二维码领域对象或视图对象。
     * @return 转换后的目标模型对象。
     */
    private QrcodeVO convertToVO(Qrcode qrcode) {
        return QrcodeVO.builder().id(qrcode.getId()).fileId(qrcode.getFileId()).type(qrcode.getType()).build();
    }

    /**
     * 在二维码 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param qrcodeVO
     *            二维码视图对象。
     * @return 转换后的目标模型对象。
     */
    private Qrcode convertToEntity(QrcodeVO qrcodeVO) {
        return Qrcode.builder().id(qrcodeVO.getId()).fileId(qrcodeVO.getFileId()).type(qrcodeVO.getType()).build();
    }
}
