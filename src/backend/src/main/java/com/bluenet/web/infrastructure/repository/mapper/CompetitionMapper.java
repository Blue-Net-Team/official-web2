package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
    List<CompetitionVO> selectCompetitionsWithLimit(@Param("limit") int limit);

    IPage<CompetitionVO> selectCompetitionsPage(Page<CompetitionVO> page);

    Integer selectMaxSortOrder();

    void updateSortOrderById(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    Competition selectAdjacentUp(@Param("sortOrder") Integer sortOrder);

    Competition selectAdjacentDown(@Param("sortOrder") Integer sortOrder);
}
