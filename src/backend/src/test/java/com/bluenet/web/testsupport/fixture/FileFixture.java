package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;

/**
 * 文件测试夹具。
 */
public final class FileFixture {

    private FileFixture() {
    }

    public static File work(String name) {
        return File.create(name, FileType.WORK);
    }

    public static File avatar(String name) {
        return File.create(name, FileType.AVATAR);
    }

    public static File normalImg(String name) {
        return File.create(name, FileType.NORMAL_IMG);
    }

    public static File assessmentAttachment(String name) {
        return File.create(name, FileType.ASSESSMENT_ATTACHMENT);
    }

    public static File qrcode(String name) {
        return File.create(name, FileType.QRCODE);
    }

    /**
     * 仅保存文件元数据，不操作对象存储。 若 url 为空，自动填充占位地址以满足数据库非空约束。
     */
    public static File save(FileRepository fileRepository, File file) {
        if (file.getUrl() == null) {
            file.setUrl("http://localhost/test/" + file.getName());
        }
        return fileRepository.save(file);
    }

    /**
     * 创建并保存指定名称与类型的文件元数据。
     */
    public static File save(FileRepository fileRepository, String name, FileType type) {
        return save(fileRepository, File.create(name, type));
    }

    public static File withStatus(File file, FileStatus status) {
        return File.reconstruct(
                file.getId(),
                file.getName(),
                file.getType(),
                null,
                status,
                file.getCreatedAt());
    }
}
