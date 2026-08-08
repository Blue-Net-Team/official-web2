package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.enrollform.EnrollFormCommands;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;
import com.bluenet.web.application.service.EnrollFormAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * 报名表应用服务实现。
 * <p>
 * 单张全局报名表：当前表为 tb_file 中 type='enroll-form' 且 status='active' 的最新记录。
 * 该类型文件不被孤儿清理任务处理，生命周期由本服务显式维护。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollFormAppServiceImpl implements EnrollFormAppService {

    /** 报名表允许的扩展名（小写）。 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final FileRepository fileRepository;
    private final FileDomainService fileDomainService;

    /**
     * 查询当前报名表。
     *
     * @return 当前报名表结果；不存在时为空
     */
    @Override
    public Optional<EnrollFormResult> getCurrentEnrollForm() {
        Optional<File> current = fileRepository.findLatestByType(FileType.ENROLL_FORM);
        return current.map(file -> new EnrollFormResult(file.getId(), file.getCreatedAt()));
    }

    /**
     * 设置或更新报名表。
     *
     * @param command
     *            设置报名表命令
     */
    @Override
    @Transactional
    public void setEnrollForm(EnrollFormCommands.SetEnrollFormCommand command) {
        File file = fileDomainService.getFileById(command.fileId());
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.ENROLL_FORM) {
            throw new BadRequest("文件类型不匹配，期望 ENROLL_FORM");
        }
        if (file.getStatus() != FileStatus.ACTIVE) {
            throw new BadRequest("文件尚未完成上传确认，不能设为报名表");
        }
        String extension = getExtension(file.getName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequest("报名表仅支持 pdf/doc/docx 格式");
        }

        // 全部校验通过后，再删除旧报名表文件（含对象存储对象）。
        // 新文件此时已是 ACTIVE，按"最新即为当前"的语义它本身就是当前表，
        // 因此查旧表必须排除新文件自身。
        Optional<File> previous = fileRepository.findLatestByTypeExcludingId(FileType.ENROLL_FORM, file.getId());
        if (previous.isPresent()) {
            Long oldFileId = previous.get().getId();
            fileRepository.deleteFileById(oldFileId);
            log.info("旧报名表已删除，fileId={}", oldFileId);
        }
        log.info("报名表设置成功，fileId={}", file.getId());
    }

    /**
     * 删除当前报名表。
     */
    @Override
    @Transactional
    public void deleteEnrollForm() {
        File current = fileRepository.findLatestByType(FileType.ENROLL_FORM)
                .orElseThrow(() -> new DataNotFound("当前没有报名表"));
        fileRepository.deleteFileById(current.getId());
        log.info("报名表删除成功，fileId={}", current.getId());
    }

    /**
     * 提取文件名的小写扩展名。
     *
     * @param filename
     *            文件名
     * @return 小写扩展名；无扩展名时返回空字符串
     */
    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
