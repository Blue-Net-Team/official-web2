package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeStandardSolutionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JudgeStandardSolutionMapper extends BaseMapper<JudgeStandardSolutionDO> {
    /**
     * 删除某个判题配置下的全部标准解。
     *
     * @param configId
     *            判题配置主键。
     */
    void deleteByConfigId(@Param("configId") Long configId);

    /**
     * 查询某个判题配置下的全部标准解。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解数据对象列表。
     */
    List<JudgeStandardSolutionDO> selectByConfigId(@Param("configId") Long configId);
}
