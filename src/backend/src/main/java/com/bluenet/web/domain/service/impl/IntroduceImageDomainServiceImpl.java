package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.repository.IntroduceImageRepository;
import com.bluenet.web.domain.service.IntroduceImageDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntroduceImageDomainServiceImpl implements IntroduceImageDomainService {
    private final IntroduceImageRepository introduceImageRepository;

    @Override
    public List<IntroduceImageVO> getIntroduceImages(ImageType type, Direction direction) {
        return introduceImageRepository.findByTypeAndDirection(type, direction);
    }

    @Override
    public List<IntroduceImageVO> getCompetitionImages(Long competitionId) {
        return introduceImageRepository.findByTypeAndCompetitionId(ImageType.COMPETITION, competitionId);
    }

    @Override
    public int countCompetitionImages(Long competitionId) {
        return introduceImageRepository.countByTypeAndCompetitionId(ImageType.COMPETITION, competitionId);
    }

    @Override
    public Long addCompetitionImage(Long competitionId, Long fileId, String description) {
        IntroduceImage image = new IntroduceImage();
        image.setType(ImageType.COMPETITION);
        image.setCompetitionId(competitionId);
        image.setFileId(fileId);
        image.setDescription(description);
        image.setSortOrder(0);
        return introduceImageRepository.save(image);
    }

    @Override
    public void removeCompetitionImage(Long imageId) {
        introduceImageRepository.deleteById(imageId);
    }

    @Override
    public boolean existsById(Long imageId) {
        return introduceImageRepository.existsById(imageId);
    }

    @Override
    public Long addIntroduceImage(ImageType type, Long fileId, Direction direction, String description) {
        IntroduceImage image = new IntroduceImage();
        image.setType(type);
        image.setFileId(fileId);
        image.setDirection(direction);
        image.setDescription(description);
        image.setSortOrder(0);
        return introduceImageRepository.save(image);
    }
}
