package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeTestcaseConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JudgeTestcaseConfigMapper extends BaseMapper<JudgeTestcaseConfigDO> {
    /**
     * 插入测试用例生成配置。
     *
     * @param config
     *            测试用例生成配置数据对象。
     */
    void insertConfig(@Param("config") JudgeTestcaseConfigDO config);

    /**
     * 删除某个判题配置下的全部测试用例生成配置。
     *
     * @param configId
     *            判题配置主键。
     */
    void deleteByConfigId(@Param("configId") Long configId);

    /**
     * 查询某个判题配置下的全部测试用例生成配置。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例生成配置列表。
     */
    List<JudgeTestcaseConfigDO> selectByConfigId(@Param("configId") Long configId);
}
