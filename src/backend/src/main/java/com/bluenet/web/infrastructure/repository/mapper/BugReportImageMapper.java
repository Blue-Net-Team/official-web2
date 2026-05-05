package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.BugReportImageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BugReportImageMapper extends BaseMapper<BugReportImageDO> {

    /**
     * 按 Bug 报告 ID 查询关联图片
     *
     * @param bugReportId
     *            Bug 报告主键
     * @return 关联图片列表
     */
    @Select("SELECT * FROM tb_bug_report_image WHERE bug_report_id = #{bugReportId}")
    List<BugReportImageDO> selectByBugReportId(@Param("bugReportId") Long bugReportId);
}
