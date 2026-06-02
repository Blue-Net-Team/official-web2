package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.knowledge.KnowledgeCommands;
import com.bluenet.web.application.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.service.KnowledgeBaseAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.messaging.KnowledgeParsePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
        FileVO fileVO = fileDomainService.saveFile(FileType.KNOWLEDGE, originalFilename, getInputStream(file));

        // 创建知识库文档记录
        String title = command.title() != null && !command.title().isBlank()
                ? command.title()
                : originalFilename;
        KnowledgeDoc doc = KnowledgeDoc.create(fileVO.getId(), title);
        knowledgeDocRepository.save(doc);

        // 生成预签名下载 URL 并发布解析任务
        String downloadUrl = fileDomainService.getPresignedDownloadUrl(FileType.KNOWLEDGE, fileVO.getName());
        knowledgeParsePublisher.publish(doc.getId(), fileVO.getId(), downloadUrl, false);

        log.info("知识库文档上传成功，docId={}, fileId={}", doc.getId(), fileVO.getId());
        return new KnowledgeDocResult.Uploaded(doc.getId(), doc.getStatus());
    }

    @Override
    @Transactional
    public void reparse(KnowledgeCommands.ReparseDocumentCommand command) {
        KnowledgeDoc doc = knowledgeDocRepository.findById(command.docId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + command.docId()));

        FileVO fileVO = fileDomainService.getFileById(doc.getFileId());

        doc.markForReparse();
        knowledgeDocRepository.update(doc);

        String downloadUrl = fileDomainService.getPresignedDownloadUrl(FileType.KNOWLEDGE, fileVO.getName());
        knowledgeParsePublisher.publish(doc.getId(), fileVO.getId(), downloadUrl, true);

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

        knowledgeDocRepository.updateStatus(doc.getId(), DocParseStatus.CANCELING, null, null);
        log.info("知识库文档取消解析已请求，docId={}", doc.getId());
    }

    @Override
    @Transactional
    public void deleteDocument(KnowledgeCommands.DeleteDocumentCommand command) {
        KnowledgeDoc doc = knowledgeDocRepository.findById(command.docId())
                .orElseThrow(() -> new DataNotFound("文档不存在，ID: " + command.docId()));

        Long fileId = doc.getFileId();

        // 删除关联的 chunks
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
        int rows = knowledgeTagRepository.updateDescription(tagId, description);
        if (rows == 0) {
            throw new DataNotFound("标签不存在，ID: " + tagId);
        }
        log.info("知识库标签描述更新成功，tagId={}", tagId);
    }

    private java.io.InputStream getInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (java.io.IOException e) {
            throw new RuntimeException("读取文件流失败", e);
        }
    }
}
