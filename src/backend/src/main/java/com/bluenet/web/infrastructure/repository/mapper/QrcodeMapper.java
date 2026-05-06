package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.QrcodeDO;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QrcodeMapper extends BaseMapper<QrcodeDO> {
    /**
     * 按条件查询二维码 数据行。
     *
     * @param fileId
     *            文件主键。
     * @return 匹配条件的二维码 数据行；不存在时为 null。
     */
    QrcodeDO selectByFileId(@Param("fileId") Long fileId);

    /**
     * 按条件查询二维码 数据行。
     *
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的二维码 结果集合。
     */
    List<QrcodeDO> selectByType(@Param("type") QrcodeType type);

    /**
     * 按条件查询考核群二维码 数据行。
     *
     * @param direction
     *            方向（可选）。
     * @param epoch
     *            考核轮次（可选）。
     * @return 满足条件的二维码 结果集合。
     */
    List<QrcodeDO> selectAssessmentQrcodes(@Param("direction") String direction,
            @Param("epoch") Integer epoch);

    /**
     * 按考核轮次查询考核群二维码 数据行。
     *
     * @param epoch
     *            考核轮次。
     * @return 满足条件的二维码 结果集合。
     */
    List<QrcodeDO> findAssessmentByEpoch(@Param("epoch") Integer epoch);
}
