package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IntroduceImageMapper extends BaseMapper<IntroduceImage> {
    List<IntroduceImageVO> selectByTypeAndDirection(@Param("type") ImageType type,
            @Param("direction") Direction direction);
    List<IntroduceImageVO> selectByTypeAndCompetitionId(@Param("type") ImageType type,
            @Param("competitionId") Long competitionId);
    int countByTypeAndCompetitionId(@Param("type") ImageType type, @Param("competitionId") Long competitionId);
}
