package com.bluenet.web.api.converter.assessment_judgement;

import com.bluenet.web.api.dto.assessment_judgement.CommentDTO;
import com.bluenet.web.application.result.comment.CommentResult;
import org.springframework.stereotype.Component;

@Component
public class CommentResponseConverter {

    public CommentDTO toDTO(CommentResult result) {
        if (result == null) {
            return null;
        }
        return CommentDTO.builder()
                .id(result.id())
                .answerId(result.answerId())
                .userId(result.userId())
                .username(result.username())
                .content(result.content())
                .score(result.score())
                .commentTime(result.commentTime())
                .build();
    }
}
