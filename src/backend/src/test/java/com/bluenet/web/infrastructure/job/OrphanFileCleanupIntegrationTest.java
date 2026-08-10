package com.bluenet.web.infrastructure.job;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrphanFileCleanupIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("findOrphanFiles returns PENDING files older than threshold")
    void findOrphanFiles_PendingTimeout() {
        FileDO oldPending = FileDO.builder()
                .name("old-pending.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/old-pending.jpg")
                .status(FileStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        fileMapper.insert(oldPending);

        FileDO recentPending = FileDO.builder()
                .name("recent-pending.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/recent-pending.jpg")
                .status(FileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(recentPending);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertTrue(orphanIds.contains(oldPending.getId()), "超时的 PENDING 文件应被识别为孤儿");
        assertFalse(orphanIds.contains(recentPending.getId()), "未超时的 PENDING 文件不应被识别为孤儿");
    }

    @Test
    @DisplayName("findOrphanFiles returns REJECTED files")
    void findOrphanFiles_Rejected() {
        FileDO rejected = FileDO.builder()
                .name("rejected.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/rejected.jpg")
                .status(FileStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(rejected);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertTrue(orphanIds.contains(rejected.getId()), "REJECTED 文件应被识别为孤儿");
    }

    @Test
    @DisplayName("findOrphanFiles returns ACTIVE files with no references")
    void findOrphanFiles_ActiveUnreferenced() {
        FileDO unreferenced = FileDO.builder()
                .name("unreferenced.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/unreferenced.jpg")
                .status(FileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(unreferenced);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertTrue(orphanIds.contains(unreferenced.getId()), "无引用的 ACTIVE 文件应被识别为孤儿");
    }

    @Test
    @DisplayName("findOrphanFiles excludes ACTIVE files referenced by business tables")
    void findOrphanFiles_ActiveReferenced() {
        FileDO referenced = FileDO.builder()
                .name("referenced-avatar.jpg")
                .type(FileType.AVATAR)
                .url("/uploads/referenced-avatar.jpg")
                .status(FileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(referenced);

        UserDO user = new UserDO();
        user.setStudentId("202401010001");
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setUsername("测试用户");
        user.setAvatarId(referenced.getId());
        userMapper.insert(user);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertFalse(orphanIds.contains(referenced.getId()), "被引用的 ACTIVE 文件不应被识别为孤儿");
    }

    @Test
    @DisplayName("findOrphanFiles returns mixed orphan types correctly")
    void findOrphanFiles_MixedTypes() {
        FileDO oldPending = FileDO.builder()
                .name("old-pending.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/old-pending.jpg")
                .status(FileStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        fileMapper.insert(oldPending);

        FileDO rejected = FileDO.builder()
                .name("rejected.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/rejected.jpg")
                .status(FileStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(rejected);

        FileDO unreferenced = FileDO.builder()
                .name("unreferenced.jpg")
                .type(FileType.NORMAL_IMG)
                .url("/uploads/unreferenced.jpg")
                .status(FileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(unreferenced);

        FileDO referenced = FileDO.builder()
                .name("referenced-avatar.jpg")
                .type(FileType.AVATAR)
                .url("/uploads/referenced-avatar.jpg")
                .status(FileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(referenced);

        UserDO user = new UserDO();
        user.setStudentId("202401010002");
        user.setUsername("testuser2");
        user.setPassword("password");
        user.setEmail("test2@example.com");
        user.setUsername("测试用户2");
        user.setAvatarId(referenced.getId());
        userMapper.insert(user);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertTrue(orphanIds.contains(oldPending.getId()), "应包含 PENDING 超时文件");
        assertTrue(orphanIds.contains(rejected.getId()), "应包含 REJECTED 文件");
        assertTrue(orphanIds.contains(unreferenced.getId()), "应包含无引用的 ACTIVE 文件");
        assertFalse(orphanIds.contains(referenced.getId()), "不应包含被引用的 ACTIVE 文件");
        assertEquals(3, orphanIds.size(), "应返回 3 个孤儿文件");
    }

    @Test
    @DisplayName("findOrphanFiles excludes ACTIVE enroll-form files regardless of references")
    void findOrphanFiles_ActiveEnrollFormExcluded() {
        FileDO enrollForm = FileDO.builder()
                .name("enroll_form-uuid.pdf")
                .type(FileType.ENROLL_FORM)
                .url("/uploads/enroll_form-uuid.pdf")
                .status(FileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(enrollForm);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertFalse(orphanIds.contains(enrollForm.getId()), "ACTIVE 的报名表文件不应被识别为孤儿");
    }

    @Test
    @DisplayName("findOrphanFiles still returns PENDING-timeout and REJECTED enroll-form files")
    void findOrphanFiles_NonActiveEnrollFormStillOrphan() {
        FileDO oldPending = FileDO.builder()
                .name("enroll_form-pending.pdf")
                .type(FileType.ENROLL_FORM)
                .url("/uploads/enroll_form-pending.pdf")
                .status(FileStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        fileMapper.insert(oldPending);

        FileDO rejected = FileDO.builder()
                .name("enroll_form-rejected.pdf")
                .type(FileType.ENROLL_FORM)
                .url("/uploads/enroll_form-rejected.pdf")
                .status(FileStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();
        fileMapper.insert(rejected);

        List<File> orphans = fileRepository.findOrphanFiles();

        List<Long> orphanIds = orphans.stream().map(File::getId).toList();
        assertTrue(orphanIds.contains(oldPending.getId()), "PENDING 超时的报名表文件仍应被清理");
        assertTrue(orphanIds.contains(rejected.getId()), "REJECTED 的报名表文件仍应被清理");
    }
}
