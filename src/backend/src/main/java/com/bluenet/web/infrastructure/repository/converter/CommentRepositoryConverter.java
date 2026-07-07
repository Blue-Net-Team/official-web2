package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.infrastructure.repository.dataobject.CommentDO;
import org.springframework.stereotype.Component;

@Component
public class CommentRepositoryConverter {

    public CommentDO toDataObject(Comment entity) {
        if (entity == null) {
            return null;
        }
        return CommentDO.builder()
                .id(entity.getId())
                .answerId(entity.getAnswerId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .score(entity.getScore())
                .commentTime(entity.getCommentTime())
                .build();
    }

    public Comment toEntity(CommentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Comment.reconstruct(
                dataObject.getId(),
                dataObject.getAnswerId(),
                dataObject.getUserId(),
                dataObject.getContent(),
                dataObject.getScore(),
                dataObject.getCommentTime());
    }
}
