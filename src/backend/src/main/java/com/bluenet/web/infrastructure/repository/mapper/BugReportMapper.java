package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.BugReportDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugReportMapper extends BaseMapper<BugReportDO> {
}
