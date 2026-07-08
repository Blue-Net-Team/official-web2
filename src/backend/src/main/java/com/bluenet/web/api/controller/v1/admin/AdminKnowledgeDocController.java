package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.knowledge.*;
import com.bluenet.web.api.converter.knowledge.KnowledgeDocRequestConverter;
import com.bluenet.web.api.converter.knowledge.KnowledgeDocResponseConverter;
import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import com.bluenet.web.application.result.knowledge.KnowledgeChunkResult;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.result.knowledge.KnowledgeTagResult;
import com.bluenet.web.application.service.KnowledgeBaseAppService;
import com.bluenet.web.application.service.KnowledgeDocQueryService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理控制器。
 */
@Tag(name = "知识库管理", description = "知识库文档、标签管理接口")
@RestController
@RequestMapping("/api/v1/admin/knowledge")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminKnowledgeDocController {

    private final KnowledgeBaseAppService knowledgeBaseAppService;
    private final KnowledgeDocQueryService knowledgeDocQueryService;
    private final KnowledgeDocRequestConverter requestConverter;
    private final KnowledgeDocResponseConverter responseConverter;

    @Operation(summary = "上传知识库文档", description = "上传 Markdown 文档并触发异步解析")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "仅支持 .md 文件", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "上传知识库文档", value = "knowledge:doc:upload", access = AccessLevel.PROTECTED)
    @PostMapping("/docs")
    public ResponseMessage<KnowledgeDocDetailResponseDTO> uploadDocument(
            @Valid UploadKnowledgeDocRequestDTO request) {
        KnowledgeDocResult.Uploaded uploaded = knowledgeBaseAppService.uploadDocument(
                requestConverter.toUploadCommand(request));
        KnowledgeDocResult.Detail detail = knowledgeDocQueryService.getDocumentDetail(uploaded.docId());
        return ResponseMessage.success(responseConverter.toDetailDTO(detail));
    }

    @Operation(summary = "重新解析文档", description = "触发已有文档的重新解析，覆盖旧分段")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "404", description = "文档不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "重新解析文档", value = "knowledge:doc:reparse", access = AccessLevel.PROTECTED)
    @PostMapping("/docs/{id}/reparse")
    public ResponseMessage<Void> reparseDocument(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        knowledgeBaseAppService.reparse(new KnowledgeCommands.ReparseDocumentCommand(id));
        return ResponseMessage.success();
    }

    @Operation(summary = "取消解析文档", description = "取消待解析或解析中的文档")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "当前状态不允许取消", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "文档不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "取消解析文档", value = "knowledge:doc:cancel", access = AccessLevel.PROTECTED)
    @PostMapping("/docs/{id}/cancel")
    public ResponseMessage<Void> cancelParse(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        knowledgeBaseAppService.cancelParse(new KnowledgeCommands.CancelParseCommand(id));
        return ResponseMessage.success();
    }

    @Operation(summary = "删除知识库文档", description = "删除文档及其关联文件、分段")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "文档不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "删除知识库文档", value = "knowledge:doc:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/docs/{id}")
    public ResponseMessage<Void> deleteDocument(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        knowledgeBaseAppService.deleteDocument(new KnowledgeCommands.DeleteDocumentCommand(id));
        return ResponseMessage.success();
    }

    @Operation(summary = "查询文档列表", description = "分页查询所有知识库文档")
    @RequiresPermission(name = "查询知识库文档列表", value = "knowledge:doc:list", access = AccessLevel.PROTECTED)
    @GetMapping("/docs")
    public ResponseMessage<PageDTO<KnowledgeDocListItemResponseDTO>> listDocuments(Pageable pageable) {
        Page<KnowledgeDocResult.ListItem> page = knowledgeDocQueryService.listDocuments(pageable);
        return ResponseMessage.success(responseConverter.toDocListPageDTO(page));
    }

    @Operation(summary = "查询文档详情", description = "获取单个文档的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "文档不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询知识库文档详情", value = "knowledge:doc:detail", access = AccessLevel.PROTECTED)
    @GetMapping("/docs/{id}")
    public ResponseMessage<KnowledgeDocDetailResponseDTO> getDocumentDetail(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        KnowledgeDocResult.Detail detail = knowledgeDocQueryService.getDocumentDetail(id);
        if (detail == null) {
            return ResponseMessage.error(404, "文档不存在");
        }
        return ResponseMessage.success(responseConverter.toDetailDTO(detail));
    }

    @Operation(summary = "查询文档分段", description = "分页查询指定文档的所有分段")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "文档不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询知识库文档分段", value = "knowledge:doc:chunks", access = AccessLevel.PROTECTED)
    @GetMapping("/docs/{id}/chunks")
    public ResponseMessage<PageDTO<KnowledgeChunkListItemResponseDTO>> listChunks(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id,
            Pageable pageable) {
        Page<KnowledgeChunkResult.ListItem> page = knowledgeDocQueryService.listChunks(id, pageable);
        return ResponseMessage.success(responseConverter.toChunkListPageDTO(page));
    }

    @Operation(summary = "查询标签列表", description = "分页查询所有知识库标签")
    @RequiresPermission(name = "查询知识库标签列表", value = "knowledge:tag:list", access = AccessLevel.PROTECTED)
    @GetMapping("/tags")
    public ResponseMessage<PageDTO<KnowledgeTagListItemResponseDTO>> listTags(Pageable pageable) {
        Page<KnowledgeTagResult.ListItem> page = knowledgeDocQueryService.listTags(pageable);
        return ResponseMessage.success(responseConverter.toTagListPageDTO(page));
    }

    @Operation(summary = "更新标签描述", description = "修改指定标签的描述文本")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "标签不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "更新知识库标签描述", value = "knowledge:tag:update", access = AccessLevel.PROTECTED)
    @PutMapping("/tags/{id}")
    public ResponseMessage<Void> updateTagDescription(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateTagDescriptionRequestDTO request) {
        knowledgeBaseAppService.updateTagDescription(id, request.description());
        return ResponseMessage.success();
    }
}
