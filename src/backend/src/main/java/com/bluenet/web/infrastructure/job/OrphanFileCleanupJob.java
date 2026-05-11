package com.bluenet.web.infrastructure.job;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrphanFileCleanupJob {

    private final FileRepository fileRepository;
    private final ObjectStorage objectStorage;

    @Scheduled(cron = "${job.orphan-file-cleanup.cron:0 0 2 * * *}")
    public void cleanup() {
        log.info("开始执行孤儿文件清理任务");

        List<File> orphanFiles;
        try {
            orphanFiles = fileRepository.findOrphanFiles();
        } catch (Exception e) {
            log.error("查询孤儿文件失败，清理任务终止", e);
            return;
        }

        if (orphanFiles.isEmpty()) {
            log.info("未发现孤儿文件，清理任务结束");
            return;
        }

        log.info("发现 {} 个孤儿文件，开始清理", orphanFiles.size());

        int successCount = 0;
        int failureCount = 0;

        for (File file : orphanFiles) {
            try {
                cleanupSingleFile(file);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("清理孤儿文件失败，跳过: fileId={}, filename={}", file.getId(), file.getName(), e);
            }
        }

        log.info("孤儿文件清理任务完成，成功: {}, 失败: {}", successCount, failureCount);
    }

    private void cleanupSingleFile(File file) {
        Long fileId = file.getId();
        String filename = file.getName();
        var fileType = file.getType();

        fileRepository.deleteFileById(fileId);
        log.info("已删除孤儿文件数据库记录: fileId={}, filename={}", fileId, filename);

        try {
            objectStorage.delete(fileType, filename);
            log.info("已删除孤儿文件 OSS 对象: fileId={}, filename={}", fileId, filename);
        } catch (Exception e) {
            log.error("删除孤儿文件 OSS 对象失败，数据库记录已清理: fileId={}, filename={}", fileId, filename, e);
        }
    }
}
