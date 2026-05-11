package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.infrastructure.repository.converter.CommentRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.CommentDO;
import com.bluenet.web.infrastructure.repository.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
    private final CommentMapper commentMapper;
    private final CommentRepositoryConverter converter;

    @Override
    public void save(Comment comment) {
        CommentDO dataObject = converter.toDataObject(comment);
        commentMapper.insert(dataObject);
        comment.setId(dataObject.getId());
    }

    @Override
    public Optional<Comment> findById(Long id) {
        Comment comment = converter.toEntity(commentMapper.selectById(id));
        return Optional.ofNullable(comment);
    }

    @Override
    public List<CommentVO> findByAnswerId(Long answerId) {
        List<CommentDO> dataObjects = commentMapper.selectList(
                new QueryWrapper<CommentDO>().eq("answer_id", answerId)
                        .orderByAsc("comment_time"));
        return dataObjects.stream()
                .map(converter::toEntity)
                .map(converter::toVO)
                .toList();
    }

    @Override
    public boolean existsByAnswerIdAndUserId(Long answerId, Long userId) {
        return commentMapper.exists(
                new QueryWrapper<CommentDO>()
                        .eq("answer_id", answerId)
                        .eq("user_id", userId));
    }

    @Override
    public void update(Comment comment) {
        commentMapper.updateById(converter.toDataObject(comment));
    }

    @Override
    public void deleteById(Long id) {
        commentMapper.deleteById(id);
    }
}
