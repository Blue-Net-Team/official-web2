package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;

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

    @SuppressWarnings("deprecation")
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
