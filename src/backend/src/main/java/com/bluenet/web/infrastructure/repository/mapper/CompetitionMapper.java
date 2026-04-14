package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
    List<CompetitionBriefVO> selectEnabledCompetitionsWithLimit(@Param("limit") int limit);
}
