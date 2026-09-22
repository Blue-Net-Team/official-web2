package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.service.KnowledgeBaseAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.entity.KnowledgeTag;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkTagRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.messaging.KnowledgeParsePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库应用服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseAppServiceImpl implements KnowledgeBaseAppService {

    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;
    private final KnowledgeDocRepository knowledgeDocRepository;
    private final KnowledgeParsePublisher knowledgeParsePublisher;
    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final KnowledgeChunkTagRepository knowledgeChunkTagRepository;
    private final KnowledgeTagRepository knowledgeTagRepository;

    @Override
    @Transactional
    public KnowledgeDocResult.Uploaded uploadDocument(KnowledgeCommands.UploadDocumentCommand command) {
        MultipartFile file = command.file();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
            throw new BadRequest("仅支持上传 .md 文件");
        }

        // 保存文件到 OSS
        File savedFile = fileDomainService.saveFile(FileType.KNOWLEDGE, originalFilename, getInputStream(file));

        // 创建知识库文档记录
        String title = command.title() != null && !command.title().isBlank()
                ? command.title()
                : originalFilename;
        KnowledgeDoc doc = KnowledgeDoc.create(savedFile.getId(), title);
        knowledgeDocRepository.save(doc);

        // 生成预签名下载 URL 并发布解析任务
        String downloadUrl = fileDomainService.getPresignedDownloadUrl(FileType.KNOWLEDGE, savedFile.getName());
        knowledgeParsePublisher.publish(doc.getId(), savedFile.getId(), downloadUrl, false);

        log.info("知识库文档上传成功，docId={}, fileId={}", doc.getId(), savedFile.getId());
        return new KnowledgeDocResult.Uploaded(doc.getId(), doc.getStatus());
    }

    @Override
    @Transactional
    public void reparse(KnowledgeCommands.ReparseDocumentCommand command) {
        KnowledgeDoc doc = knowledgeDocRepository.findById(command.docId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + command.docId()));

        File file = fileDomainService.getFileById(doc.getFileId());

        doc.markForReparse();
        knowledgeDocRepository.save(doc);

        String downloadUrl = fileDomainService.getPresignedDownloadUrl(FileType.KNOWLEDGE, file.getName());
        knowledgeParsePublisher.publish(doc.getId(), file.getId(), downloadUrl, true);

        log.info("知识库文档重新解析已触发，docId={}", doc.getId());
    }

    @Override
    @Transactional
    public void cancelParse(KnowledgeCommands.CancelParseCommand command) {
        KnowledgeDoc doc = knowledgeDocRepository.findById(command.docId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + command.docId()));

        if (doc.getStatus() != DocParseStatus.PENDING && doc.getStatus() != DocParseStatus.PARSING) {
            throw new BadRequest("当前状态不允许取消解析: " + doc.getStatus().getValue());
        }

        doc.updateStatus(DocParseStatus.CANCELING, null, null);
        knowledgeDocRepository.save(doc);
        log.info("知识库文档取消解析已请求，docId={}", doc.getId());
    }

    @Override
    @Transactional
    public void deleteDocument(KnowledgeCommands.DeleteDocumentCommand command) {
        KnowledgeDoc doc = knowledgeDocRepository.findById(command.docId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + command.docId()));

        Long fileId = doc.getFileId();

        // 删除关联的 chunks 及其标签关联
        knowledgeChunkTagRepository.deleteByDocId(doc.getId());
        knowledgeChunkRepository.deleteByDocId(doc.getId());

        // 删除文档记录
        knowledgeDocRepository.deleteById(doc.getId());

        // 删除文件元数据和 OSS 对象
        fileRepository.deleteFileById(fileId);

        log.info("知识库文档删除成功，docId={}, fileId={}", doc.getId(), fileId);
    }

    @Override
    @Transactional
    public void updateTagDescription(Long tagId, String description) {
        KnowledgeTag tag = knowledgeTagRepository.findById(tagId)
                .orElseThrow(() -> new DataNotFound("标签不存在，ID: " + tagId));
        tag.updateDescription(description);
        knowledgeTagRepository.save(tag);
        log.info("知识库标签描述更新成功，tagId={}", tagId);
    }

    @Override
    @Transactional
    public void updateChunk(KnowledgeCommands.UpdateChunkCommand command) {
        KnowledgeChunk chunk = knowledgeChunkRepository.findById(command.chunkId())
                .orElseThrow(() -> new DataNotFound("分片不存在，ID: " + command.chunkId()));

        KnowledgeDoc doc = knowledgeDocRepository.findById(chunk.getDocId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + chunk.getDocId()));
        if (doc.getStatus() == DocParseStatus.PENDING
                || doc.getStatus() == DocParseStatus.PARSING
                || doc.getStatus() == DocParseStatus.CANCELING) {
            throw new DataConflict("文档正在解析中，不允许编辑分片: " + doc.getStatus().getValue());
        }

        List<Long> tagIds = command.tagIds() != null ? command.tagIds() : List.of();
        for (Long tagId : tagIds) {
            if (!knowledgeTagRepository.findById(tagId).isPresent()) {
                throw new BadRequest("标签不存在，ID: " + tagId);
            }
        }

        chunk.updateContent(command.content(), tagIds);
        knowledgeChunkRepository.updateContent(chunk);
        knowledgeChunkTagRepository.replaceByChunkId(chunk.getId(), chunk.getTagIds());

        knowledgeParsePublisher.publishReembed(chunk.getId());
        log.info("知识库分片编辑成功，chunkId={}", chunk.getId());
    }

    @Override
    @Transactional
    public void replaceDocFile(KnowledgeCommands.ReplaceDocFileCommand command) {
        MultipartFile file = command.file();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
            throw new BadRequest("仅支持上传 .md 文件");
        }

        KnowledgeDoc doc = knowledgeDocRepository.findById(command.docId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + command.docId()));

        if (doc.getStatus() == DocParseStatus.PENDING
                || doc.getStatus() == DocParseStatus.PARSING
                || doc.getStatus() == DocParseStatus.CANCELING) {
            throw new DataConflict("文档正在解析中，不允许更换附件: " + doc.getStatus().getValue());
        }

        // 保存新文件到 OSS；旧文件孤儿化，由凌晨清理任务回收
        File savedFile = fileDomainService.saveFile(FileType.KNOWLEDGE, originalFilename, getInputStream(file));

        doc.setFileId(savedFile.getId());
        doc.markForReparse();
        knowledgeDocRepository.save(doc);

        String downloadUrl = fileDomainService.getPresignedDownloadUrl(FileType.KNOWLEDGE, savedFile.getName());
        knowledgeParsePublisher.publish(doc.getId(), savedFile.getId(), downloadUrl, true);

        log.info("知识库文档附件已更换，docId={}, newFileId={}", doc.getId(), savedFile.getId());
    }

    @Override
    @Transactional
    public Long createTag(KnowledgeCommands.CreateTagCommand command) {
        if (knowledgeTagRepository.existsByName(command.tagName(), null)) {
            throw new BadRequest("标签名已存在: " + command.tagName());
        }
        KnowledgeTag tag = KnowledgeTag.create(command.tagName(), command.description());
        knowledgeTagRepository.save(tag);

        knowledgeParsePublisher.publishTagUpsert(tag.getId(), false);
        log.info("知识库标签创建成功，tagId={}, tagName={}", tag.getId(), tag.getTagName());
        return tag.getId();
    }

    @Override
    @Transactional
    public void updateTag(KnowledgeCommands.UpdateTagCommand command) {
        KnowledgeTag tag = knowledgeTagRepository.findById(command.tagId())
                .orElseThrow(() -> new DataNotFound("标签不存在，ID: " + command.tagId()));

        boolean renamed = command.tagName() != null && !command.tagName().equals(tag.getTagName());
        if (renamed) {
            if (knowledgeTagRepository.existsByName(command.tagName(), command.tagId())) {
                throw new BadRequest("标签名已存在: " + command.tagName());
            }
            tag.rename(command.tagName());
        }
        if (command.description() != null) {
            tag.updateDescription(command.description());
        }
        knowledgeTagRepository.save(tag);

        if (renamed) {
            knowledgeParsePublisher.publishTagUpsert(tag.getId(), false);
        }
        log.info("知识库标签更新成功，tagId={}", tag.getId());
    }

    @Override
    @Transactional
    public int deleteTag(KnowledgeCommands.DeleteTagCommand command) {
        KnowledgeTag tag = knowledgeTagRepository.findById(command.tagId())
                .orElseThrow(() -> new DataNotFound("标签不存在，ID: " + command.tagId()));

        long dissociated = knowledgeChunkTagRepository.countByTagId(tag.getId());
        knowledgeChunkTagRepository.deleteByTagId(tag.getId());
        knowledgeTagRepository.deleteById(tag.getId());

        // 标签删除后其余标签的引用计数可能漂移，触发一次重算（消费端发现标签已删除时仅执行重算）
        knowledgeParsePublisher.publishTagUpsert(tag.getId(), true);
        log.info("知识库标签删除成功，tagId={}, dissociatedChunks={}", tag.getId(), dissociated);
        return (int) dissociated;
    }

    private java.io.InputStream getInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (java.io.IOException e) {
            throw new RuntimeException("读取文件流失败", e);
        }
    }
}
