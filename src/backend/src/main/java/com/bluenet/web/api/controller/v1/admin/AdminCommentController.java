package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_judgement.CommentDTO;
import com.bluenet.web.api.dto.assessment_judgement.CommentRequestDTO;
import com.bluenet.web.api.converter.assessment_judgement.CommentResponseConverter;
import com.bluenet.web.application.service.CommentAppService;
import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考核评论管理控制器
 */
@Tag(name = "考核评论管理", description = "对文件上传题答案进行评论和评分")
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminCommentController {
    private final CommentAppService commentAppService;
    private final CommentResponseConverter commentResponseConverter;

    @Operation(summary = "添加评论", description = "团队成员及以上对文件上传题答案添加评论和参考评分")
    @RequiresPermission(name = "添加考核评论", value = "assessment-comment:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<CommentDTO> addComment(@Valid @RequestBody CommentRequestDTO request) {
        CommentVO comment = commentAppService
                .addComment(request.getAnswerId(), request.getContent(), request.getScore());
        return ResponseMessage.success(commentResponseConverter.toDTO(comment));
    }

    @Operation(summary = "查询评论列表", description = "按答案ID查询所有评论")
    @RequiresPermission(name = "查询考核评论", value = "assessment-comment:query", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<List<CommentDTO>> listComments(
            @Parameter(description = "答案ID", required = true) @RequestParam Long answerId) {
        List<CommentVO> comments = commentAppService.listComments(answerId);
        return ResponseMessage.success(
                comments.stream()
                        .map(commentResponseConverter::toDTO)
                        .toList());
    }

    @Operation(summary = "更新评论", description = "评论者更新自己的评论")
    @RequiresPermission(name = "更新考核评论", value = "assessment-comment:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<CommentDTO> updateComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO request) {
        CommentVO comment = commentAppService.updateComment(id, request.getContent(), request.getScore());
        return ResponseMessage.success(commentResponseConverter.toDTO(comment));
    }

    @Operation(summary = "删除评论", description = "评论者删除自己的评论")
    @RequiresPermission(name = "删除考核评论", value = "assessment-comment:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        commentAppService.deleteComment(id);
        return ResponseMessage.success();
    }
}
