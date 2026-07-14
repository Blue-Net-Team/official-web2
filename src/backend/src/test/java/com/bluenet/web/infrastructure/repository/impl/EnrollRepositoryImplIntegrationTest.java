package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.infrastructure.repository.dataobject.EnrollDO;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.application.result.enroll.EnrollStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EnrollRepositoryImpl 集成测试。
 */
@DisplayName("EnrollRepositoryImpl 集成测试")
class EnrollRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private EnrollMapper enrollMapper;

    @Autowired
    private CollegeRepository collegeRepository;

    private final AtomicLong counter = new AtomicLong(1);

    private Long createCollege(String name) {
        College college = CollegeFixture.createCollege(name);
        collegeRepository.save(college);
        return college.getId();
    }

    private Enroll createEnroll(String studentId, Direction direction, EnrollStatus status) {
        Enroll enroll = Enroll.create(
                "报名" + studentId,
                studentId,
                "encodedPassword",
                null,
                null,
                "计算机科学与技术",
                Gender.MALE,
                direction,
                null,
                studentId + "@example.com",
                "自我介绍" + studentId);
        if (status == EnrollStatus.APPROVED) {
            enroll.approve();
        } else if (status == EnrollStatus.REJECTED) {
            enroll.reject();
        }
        enrollRepository.save(enroll);
        return enroll;
    }

    private String nextStudentId() {
        return String.format("2025%05d", counter.getAndIncrement());
    }

    @Test
    @DisplayName("save: 新报名应插入并回写ID")
    void save_newEnroll_shouldInsertAndReturnId() {
        String studentId = nextStudentId();
        Enroll enroll = createEnroll(studentId, Direction.COMPUTER_VISION, EnrollStatus.PENDING);

        assertThat(enroll.getId()).isNotNull();
        EnrollDO dataObject = enrollMapper.selectById(enroll.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getStudentId()).isEqualTo(studentId);
        assertThat(dataObject.getStatus()).isEqualTo(EnrollStatus.PENDING);
    }

    @Test
    @DisplayName("save: 已有报名应更新字段")
    void save_existingEnroll_shouldUpdateFields() {
        String studentId = nextStudentId();
        Enroll enroll = createEnroll(studentId, Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        Long collegeId = createCollege("报名学院");
        enroll.updateInfo(
                "新用户名",
                studentId,
                collegeId,
                "软件工程",
                Gender.FEMALE,
                Direction.EMBEDDED,
                null,
                studentId + "@new.com",
                "新自我介绍",
                null,
                "newPassword");

        enrollRepository.save(enroll);

        EnrollDO updated = enrollMapper.selectById(enroll.getId());
        assertThat(updated.getUsername()).isEqualTo("新用户名");
        assertThat(updated.getCollegeId()).isEqualTo(collegeId);
        assertThat(updated.getDirection()).isEqualTo(Direction.EMBEDDED);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        String studentId = nextStudentId();
        Enroll enroll = createEnroll(studentId, Direction.STRUCTURAL_DESIGN, EnrollStatus.PENDING);

        Optional<Enroll> found = enrollRepository.findById(enroll.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStudentId()).isEqualTo(studentId);

        assertThat(enrollRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findByStudentId: 应按学号查询")
    void findByStudentId_shouldReturnEnroll() {
        String studentId = nextStudentId();
        Enroll enroll = createEnroll(studentId, Direction.COMPUTER_VISION, EnrollStatus.PENDING);

        Optional<Enroll> found = enrollRepository.findByStudentId(studentId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(enroll.getId());

        assertThat(enrollRepository.findByStudentId("9999999999")).isEmpty();
    }

    @Test
    @DisplayName("existsByStudentId: 应正确判断学号是否已报名")
    void existsByStudentId_shouldWork() {
        String studentId = nextStudentId();
        createEnroll(studentId, Direction.COMPUTER_VISION, EnrollStatus.PENDING);

        assertThat(enrollRepository.existsByStudentId(studentId)).isTrue();
        assertThat(enrollRepository.existsByStudentId("NOTEXIST")).isFalse();
    }

    @Test
    @DisplayName("findAll: 应分页返回全部报名")
    void findAll_shouldPaginate() {
        createEnroll(nextStudentId(), Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        createEnroll(nextStudentId(), Direction.EMBEDDED, EnrollStatus.PENDING);

        Page<Enroll> page = enrollRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("findByStatus: 应按状态分页查询")
    void findByStatus_shouldFilterByStatus() {
        createEnroll(nextStudentId(), Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        createEnroll(nextStudentId(), Direction.EMBEDDED, EnrollStatus.APPROVED);

        Page<Enroll> page = enrollRepository.findByStatus(EnrollStatus.APPROVED, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(EnrollStatus.APPROVED);
    }

    @Test
    @DisplayName("findByDirection: 应按方向分页查询")
    void findByDirection_shouldFilterByDirection() {
        createEnroll(nextStudentId(), Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        createEnroll(nextStudentId(), Direction.STRUCTURAL_DESIGN, EnrollStatus.PENDING);

        Page<Enroll> page = enrollRepository.findByDirection(Direction.COMPUTER_VISION, PageRequest.of(0, 10));

        assertThat(page.getContent()).allMatch(e -> e.getDirection() == Direction.COMPUTER_VISION);
    }

    @Test
    @DisplayName("findByStatusAndDirection: 应同时按状态和方向分页查询")
    void findByStatusAndDirection_shouldFilterBoth() {
        createEnroll(nextStudentId(), Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        createEnroll(nextStudentId(), Direction.COMPUTER_VISION, EnrollStatus.APPROVED);
        createEnroll(nextStudentId(), Direction.EMBEDDED, EnrollStatus.PENDING);

        Page<Enroll> page = enrollRepository.findByStatusAndDirection(
                EnrollStatus.PENDING,
                Direction.COMPUTER_VISION,
                PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(EnrollStatus.PENDING);
        assertThat(page.getContent().get(0).getDirection()).isEqualTo(Direction.COMPUTER_VISION);
    }

    @Test
    @DisplayName("search: 应按关键字搜索")
    void search_shouldFilterByKeyword() {
        String studentId = nextStudentId();
        createEnroll(studentId, Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        createEnroll(nextStudentId(), Direction.EMBEDDED, EnrollStatus.PENDING);

        Page<Enroll> page = enrollRepository.search(studentId, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStudentId()).isEqualTo(studentId);
    }

    @Test
    @DisplayName("getStatistics: 应返回报名统计")
    void getStatistics_shouldReturnStats() {
        createEnroll(nextStudentId(), Direction.COMPUTER_VISION, EnrollStatus.PENDING);
        createEnroll(nextStudentId(), Direction.EMBEDDED, EnrollStatus.APPROVED);

        EnrollStatistics statistics = enrollRepository.getStatistics();

        assertThat(statistics.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(statistics.getByStatus()).containsKey(EnrollStatus.PENDING.getValue());
        assertThat(statistics.getByDirection()).containsKey(Direction.COMPUTER_VISION);
    }
}
