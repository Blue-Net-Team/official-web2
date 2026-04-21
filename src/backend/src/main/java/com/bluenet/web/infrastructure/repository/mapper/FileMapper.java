package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.domain.model.enumerate.FileType;
import org.apache.ibatis.annotations.Mapper;

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
}
