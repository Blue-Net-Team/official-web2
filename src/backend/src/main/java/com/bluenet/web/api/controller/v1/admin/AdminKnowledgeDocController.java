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

    @Operation(summary = "更新标签", description = "修改指定标签的名称和/或描述；重命名会触发标签向量重新计算")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "标签名重复", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "标签不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "更新知识库标签", value = "knowledge:tag:update", access = AccessLevel.PROTECTED)
    @PutMapping("/tags/{id}")
    public ResponseMessage<Void> updateTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequestDTO request) {
        knowledgeBaseAppService
                .updateTag(new KnowledgeCommands.UpdateTagCommand(id, request.tagName(), request.description()));
        return ResponseMessage.success();
    }

    @Operation(summary = "新建知识库标签", description = "创建标签并异步生成标签向量")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "标签名为空或已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "新建知识库标签", value = "knowledge:tag:create", access = AccessLevel.PROTECTED)
    @PostMapping("/tags")
    public ResponseMessage<Long> createTag(
            @Valid @RequestBody CreateTagRequestDTO request) {
        Long tagId = knowledgeBaseAppService.createTag(
                new KnowledgeCommands.CreateTagCommand(request.tagName(), request.description()));
        return ResponseMessage.success(tagId);
    }

    @Operation(summary = "删除知识库标签", description = "删除标签并自动解除其与全部分片的关联")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功，返回被解除关联的分片数量"),
            @ApiResponse(responseCode = "404", description = "标签不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "删除知识库标签", value = "knowledge:tag:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/tags/{id}")
    public ResponseMessage<Integer> deleteTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id) {
        int dissociated = knowledgeBaseAppService.deleteTag(new KnowledgeCommands.DeleteTagCommand(id));
        return ResponseMessage.success(dissociated);
    }

    @Operation(summary = "编辑知识库分片", description = "修改分片内容与标签，保存后异步重新向量化")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "编辑成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败或标签不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "分片不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "409", description = "文档正在解析中", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "编辑知识库分片", value = "knowledge:chunk:update", access = AccessLevel.PROTECTED)
    @PutMapping("/chunks/{id}")
    public ResponseMessage<Void> updateChunk(
            @Parameter(description = "分片ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateChunkRequestDTO request) {
        knowledgeBaseAppService.updateChunk(
                new KnowledgeCommands.UpdateChunkCommand(id, request.content(), request.tagIds()));
        return ResponseMessage.success();
    }

    @Operation(summary = "删除知识库分段", description = "删除单个分段及其标签关联，文档分段计数减一；文档解析中禁止删除")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "分片不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "409", description = "文档正在解析中", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "删除知识库分段", value = "knowledge:chunk:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/chunks/{id}")
    public ResponseMessage<Void> deleteChunk(
            @Parameter(description = "分片ID", required = true) @PathVariable Long id) {
        knowledgeBaseAppService.deleteChunk(new KnowledgeCommands.DeleteChunkCommand(id));
        return ResponseMessage.success();
    }

    @Operation(summary = "重新上传文档附件", description = "为已有文档更换 .md 附件并触发完整重新解析，文档ID不变")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更换成功，重新解析已触发"),
            @ApiResponse(responseCode = "400", description = "仅支持 .md 文件", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "文档不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "409", description = "文档正在解析中", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "重新上传文档附件", value = "knowledge:doc:replace-file", access = AccessLevel.PROTECTED)
    @PostMapping("/docs/{id}/file")
    public ResponseMessage<Void> replaceDocFile(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        knowledgeBaseAppService.replaceDocFile(new KnowledgeCommands.ReplaceDocFileCommand(id, file));
        return ResponseMessage.success();
    }
}
