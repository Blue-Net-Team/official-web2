package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.domain.model.enumerate.FileType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FileDO> {
    /**
     * 按条件查询文件 数据行。
     *
     * @param filename
     *            对象存储中的文件名。
     * @param fileType
     *            文件业务类型。
     * @return 匹配条件的文件 数据行；不存在时为 null。
     */
    FileDO selectByNameAndType(String filename, FileType fileType);

    /**
     * 查询孤儿文件记录。
     *
     * @param pendingThreshold
     *            PENDING 状态超时阈值，created_at 早于该时间的 PENDING 文件被视为孤儿。
     * @return 孤儿文件数据行列表。
     */
    List<FileDO> selectOrphanFiles(@Param("pendingThreshold") LocalDateTime pendingThreshold);
}
