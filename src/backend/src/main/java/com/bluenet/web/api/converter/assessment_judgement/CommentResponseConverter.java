package com.bluenet.web.api.converter.assessment_judgement;

import com.bluenet.web.api.dto.assessment_judgement.CommentDTO;
import com.bluenet.web.domain.model.vo.CommentVO;
import org.springframework.stereotype.Component;

@Component
public class CommentResponseConverter {

    public CommentDTO toDTO(CommentVO vo) {
        if (vo == null) {
            return null;
        }
        return CommentDTO.builder()
                .id(vo.getId())
                .answerId(vo.getAnswerId())
                .userId(vo.getUserId())
                .username(vo.getUsername())
                .content(vo.getContent())
                .score(vo.getScore())
                .commentTime(vo.getCommentTime())
                .build();
    }
}
