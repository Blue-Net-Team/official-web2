package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.repository.IntroduceImageRepository;
import com.bluenet.web.infrastructure.repository.mapper.IntroduceImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class IntroduceImageRepositoryImpl implements IntroduceImageRepository {
    private final IntroduceImageMapper introduceImageMapper;

    @Override
    public List<IntroduceImageVO> findByTypeAndDirection(ImageType type, Direction direction) {
        return introduceImageMapper.selectByTypeAndDirection(type, direction);
    }

    @Override
    public List<IntroduceImageVO> findByTypeAndCompetitionId(ImageType type, Long competitionId) {
        return introduceImageMapper.selectByTypeAndCompetitionId(type, competitionId);
    }

    @Override
    public int countByTypeAndCompetitionId(ImageType type, Long competitionId) {
        return introduceImageMapper.countByTypeAndCompetitionId(type, competitionId);
    }

    @Override
    public Long save(IntroduceImage introduceImage) {
        introduceImageMapper.insert(introduceImage);
        return introduceImage.getId();
    }

    @Override
    public void deleteById(Long id) {
        introduceImageMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return introduceImageMapper.selectById(id) != null;
    }
}
