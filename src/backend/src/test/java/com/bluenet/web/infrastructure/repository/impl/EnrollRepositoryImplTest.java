package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.CollegeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.converter.EnrollRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;

@DisplayName("EnrollRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class EnrollRepositoryImplTest {

    @Mock
    private EnrollMapper enrollMapper;

    @Mock
    private CollegeMapper collegeMapper;

    @Mock
    private UserMapper userMapper;

    private EnrollRepositoryImpl enrollRepository;

    private static final Long TEST_ID = 1L;
    private static final String TEST_USERNAME = "张三";
    private static final String TEST_STUDENT_ID = "20210001001";
    private static final Long TEST_COLLEGE_ID = 1L;
    private static final String TEST_COLLEGE_NAME = "计算机学院";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Long TEST_AVATAR_ID = 100L;
    private static final String TEST_REFERRAL_CODE = "ABC12345";

    @BeforeEach
    void setUp() {
        enrollRepository = new EnrollRepositoryImpl(
                enrollMapper, collegeMapper, userMapper, new EnrollRepositoryConverter(),
                new CollegeRepositoryConverter());
    }

    private Enroll createTestEnroll() {
        return Enroll.reconstruct(
                TEST_ID,
                TEST_USERNAME,
                TEST_STUDENT_ID,
                "password",
                TEST_REFERRAL_CODE,
                TEST_COLLEGE_ID,
                TEST_MAJOR,
                null,
                TEST_DIRECTION,
                TEST_AVATAR_ID,
                EnrollStatus.PENDING,
                null,
                null,
                null,
                null,
                null);
    }

    private College createTestCollege() {
        return College.reconstruct(TEST_COLLEGE_ID, TEST_COLLEGE_NAME);
    }

    @Nested
    @DisplayName("findById 方法测试")
    class FindByIdTests {

        @Test
        @DisplayName("正常情况：应返回Enroll")
        void findById_existingEnroll_shouldReturnEnroll() {
            Enroll enroll = createTestEnroll();
            College college = createTestCollege();

            when(enrollMapper.selectById(TEST_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(college, CollegeDO.class));

            Optional<Enroll> result = enrollRepository.findById(TEST_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_ID, result.get().getId());
            assertEquals(TEST_USERNAME, result.get().getUsername());
            assertEquals(TEST_STUDENT_ID, result.get().getStudentId());
            assertEquals(TEST_COLLEGE_NAME, result.get().getCollegeName());
            assertEquals(TEST_AVATAR_ID, result.get().getAvatarId());
            verify(enrollMapper).selectById(TEST_ID);
            verify(collegeMapper).selectById(TEST_COLLEGE_ID);
        }

        @Test
        @DisplayName("报名不存在：应返回空Optional")
        void findById_nonExistingEnroll_shouldReturnEmpty() {
            when(enrollMapper.selectById(TEST_ID)).thenReturn(RepositoryTestObjects.toDataObject(null, EnrollDO.class));

            Optional<Enroll> result = enrollRepository.findById(TEST_ID);

            assertTrue(result.isEmpty());
            verify(enrollMapper).selectById(TEST_ID);
            verify(collegeMapper, never()).selectById(any());
        }

        @Test
        @DisplayName("学院不存在：collegeName应为null")
        void findById_collegeNotExists_shouldReturnNullCollegeName() {
            Enroll enroll = createTestEnroll();

            when(enrollMapper.selectById(TEST_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(null, CollegeDO.class));

            Optional<Enroll> result = enrollRepository.findById(TEST_ID);

            assertTrue(result.isPresent());
            assertNull(result.get().getCollegeName());
        }
    }

    @Nested
    @DisplayName("findByStudentId 方法测试")
    class FindByStudentIdTests {

        @Test
        @DisplayName("正常情况：应返回Enroll")
        void findByStudentId_existingEnroll_shouldReturnEnroll() {
            Enroll enroll = createTestEnroll();
            College college = createTestCollege();

            when(enrollMapper.selectByStudentId(TEST_STUDENT_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(college, CollegeDO.class));

            Optional<Enroll> result = enrollRepository.findByStudentId(TEST_STUDENT_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_STUDENT_ID, result.get().getStudentId());
            verify(enrollMapper).selectByStudentId(TEST_STUDENT_ID);
        }

        @Test
        @DisplayName("学号不存在：应返回空Optional")
        void findByStudentId_nonExistingStudentId_shouldReturnEmpty() {
            when(enrollMapper.selectByStudentId(TEST_STUDENT_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(null, EnrollDO.class));

            Optional<Enroll> result = enrollRepository.findByStudentId(TEST_STUDENT_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("existsByStudentId 方法测试")
    class ExistsByStudentIdTests {

        @Test
        @DisplayName("学号存在：应返回true")
        void existsByStudentId_existingStudentId_shouldReturnTrue() {
            when(enrollMapper.countByStudentId(TEST_STUDENT_ID)).thenReturn(1L);

            boolean result = enrollRepository.existsByStudentId(TEST_STUDENT_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("学号不存在：应返回false")
        void existsByStudentId_nonExistingStudentId_shouldReturnFalse() {
            when(enrollMapper.countByStudentId(TEST_STUDENT_ID)).thenReturn(0L);

            boolean result = enrollRepository.existsByStudentId(TEST_STUDENT_ID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("save 方法测试")
    class SaveTests {

        @Test
        @DisplayName("正常保存：应设置生成的ID")
        void validEnroll_shouldSetId() {
            Enroll enroll = Enroll.create(
                    TEST_USERNAME,
                    TEST_STUDENT_ID,
                    "password",
                    null,
                    TEST_COLLEGE_ID,
                    TEST_MAJOR,
                    null,
                    TEST_DIRECTION,
                    TEST_AVATAR_ID,
                    null,
                    null);

            when(enrollMapper.insert(any(EnrollDO.class))).thenAnswer(invocation -> {
                EnrollDO doObj = invocation.getArgument(0);
                doObj.setId(TEST_ID);
                return 1;
            });

            enrollRepository.save(enroll);

            assertEquals(TEST_ID, enroll.getId());
            verify(enrollMapper).insert(any(EnrollDO.class));
        }
    }

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新：应调用mapper更新")
        void update_validEnroll_shouldCallMapperUpdate() {
            Enroll enroll = Enroll.reconstruct(
                    TEST_ID,
                    TEST_USERNAME,
                    TEST_STUDENT_ID,
                    "password",
                    null,
                    TEST_COLLEGE_ID,
                    TEST_MAJOR,
                    null,
                    TEST_DIRECTION,
                    TEST_AVATAR_ID,
                    EnrollStatus.APPROVED,
                    null,
                    null,
                    null,
                    null,
                    null);

            when(enrollMapper.updateById(any(EnrollDO.class))).thenReturn(1);

            enrollRepository.update(enroll);

            verify(enrollMapper).updateById(any(EnrollDO.class));
        }
    }

    @Nested
    @DisplayName("search 方法测试")
    class SearchTests {

        @Test
        @DisplayName("无筛选条件：应返回所有报名")
        void search_noFilters_shouldReturnAllEnrolls() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollDO> enrollList = new ArrayList<>();
            enrollList.add(RepositoryTestObjects.toDataObject(createTestEnroll(), EnrollDO.class));

            IPage<EnrollDO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(enrollList);
            mockPage.setTotal(1);

            when(enrollMapper.selectPageByConditions(any(IPage.class), any(), any(), any())).thenReturn(mockPage);
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestCollege(), CollegeDO.class));

            Page<Enroll> result = enrollRepository.search(null, null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(enrollMapper).selectPageByConditions(any(IPage.class), any(), any(), any());
        }

        @Test
        @DisplayName("按状态筛选：应返回对应状态的报名")
        void search_withStatusFilter_shouldReturnFilteredEnrolls() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollDO> enrollList = new ArrayList<>();
            Enroll enroll = createTestEnroll();
            enroll.setStatus(EnrollStatus.PENDING);
            enrollList.add(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));

            IPage<EnrollDO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(enrollList);
            mockPage.setTotal(1);

            when(enrollMapper.selectPageByConditions(any(IPage.class), any(), any(), any())).thenReturn(mockPage);
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestCollege(), CollegeDO.class));

            Page<Enroll> result = enrollRepository.search(null, EnrollStatus.PENDING, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(EnrollStatus.PENDING, result.getContent().get(0).getStatus());
        }

        @Test
        @DisplayName("按方向筛选：应返回对应方向的报名")
        void search_withDirectionFilter_shouldReturnFilteredEnrolls() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollDO> enrollList = new ArrayList<>();
            enrollList.add(RepositoryTestObjects.toDataObject(createTestEnroll(), EnrollDO.class));

            IPage<EnrollDO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(enrollList);
            mockPage.setTotal(1);

            when(enrollMapper.selectPageByConditions(any(IPage.class), any(), any(), any())).thenReturn(mockPage);
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestCollege(), CollegeDO.class));

            Page<Enroll> result = enrollRepository.search(null, null, TEST_DIRECTION, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("关键词搜索：应返回匹配的报名")
        void search_withKeyword_shouldReturnMatchingEnrolls() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollDO> enrollList = new ArrayList<>();
            enrollList.add(RepositoryTestObjects.toDataObject(createTestEnroll(), EnrollDO.class));

            IPage<EnrollDO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(enrollList);
            mockPage.setTotal(1);

            when(enrollMapper.selectPageByConditions(any(IPage.class), any(), any(), any())).thenReturn(mockPage);
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestCollege(), CollegeDO.class));

            Page<Enroll> result = enrollRepository.search("张三", null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("组合筛选：应返回同时满足多个条件的报名")
        void search_withMultipleFilters_shouldReturnFilteredEnrolls() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EnrollDO> enrollList = new ArrayList<>();
            enrollList.add(RepositoryTestObjects.toDataObject(createTestEnroll(), EnrollDO.class));

            IPage<EnrollDO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(enrollList);
            mockPage.setTotal(1);

            when(enrollMapper.selectPageByConditions(any(IPage.class), any(), any(), any())).thenReturn(mockPage);
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestCollege(), CollegeDO.class));

            Page<Enroll> result = enrollRepository.search("张三", EnrollStatus.PENDING, TEST_DIRECTION, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("空结果：应返回空页面")
        void search_noResults_shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);

            IPage<EnrollDO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(new ArrayList<>());
            mockPage.setTotal(0);

            when(enrollMapper.selectPageByConditions(any(IPage.class), any(), any(), any())).thenReturn(mockPage);

            Page<Enroll> result = enrollRepository.search("不存在", null, null, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getStatistics 方法测试")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常情况：应返回统计数据")
        void getStatistics_normalCase_shouldReturnStatistics() {
            when(enrollMapper.countAll()).thenReturn(2L);
            when(enrollMapper.countByStatus(any(EnrollStatus.class))).thenReturn(1L);
            when(enrollMapper.countByDirection(any(Direction.class))).thenReturn(1L);

            EnrollStatisticsVO result = enrollRepository.getStatistics();

            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertNotNull(result.getByStatus());
            assertNotNull(result.getByDirection());
            verify(enrollMapper).countAll();
            verify(enrollMapper, times(EnrollStatus.values().length)).countByStatus(any(EnrollStatus.class));
            verify(enrollMapper, times(Direction.values().length)).countByDirection(any(Direction.class));
        }

        @Test
        @DisplayName("无数据：应返回零统计")
        void getStatistics_noData_shouldReturnZeroStatistics() {
            when(enrollMapper.countAll()).thenReturn(0L);

            EnrollStatisticsVO result = enrollRepository.getStatistics();

            assertNotNull(result);
            assertEquals(0L, result.getTotal());
        }
    }

    @Nested
    @DisplayName("推荐人查询测试")
    class ReferralUserTests {

        @Test
        @DisplayName("有内推码且推荐人存在：应返回推荐人信息")
        void findById_withReferralCodeAndUserExists_shouldReturnReferralUserInfo() {
            Enroll enroll = createTestEnroll();
            College college = createTestCollege();
            UserDO referralUser = new UserDO();
            referralUser.setId(999L);
            referralUser.setUsername("推荐人");
            referralUser.setInternalReferralCode(TEST_REFERRAL_CODE);

            when(enrollMapper.selectById(TEST_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(college, CollegeDO.class));
            when(userMapper.selectByInternalReferralCode(TEST_REFERRAL_CODE)).thenReturn(referralUser);

            Optional<Enroll> result = enrollRepository.findById(TEST_ID);

            assertTrue(result.isPresent());
            assertEquals("推荐人", result.get().getReferralUserName());
            assertEquals(999L, result.get().getReferralUserId());
            verify(userMapper, times(2)).selectByInternalReferralCode(TEST_REFERRAL_CODE);
        }

        @Test
        @DisplayName("有内推码但推荐人不存在：推荐人信息应为null")
        void findById_withReferralCodeButUserNotExists_shouldReturnNullReferralUser() {
            Enroll enroll = createTestEnroll();
            College college = createTestCollege();

            when(enrollMapper.selectById(TEST_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(college, CollegeDO.class));
            when(userMapper.selectByInternalReferralCode(TEST_REFERRAL_CODE)).thenReturn(null);

            Optional<Enroll> result = enrollRepository.findById(TEST_ID);

            assertTrue(result.isPresent());
            assertNull(result.get().getReferralUserName());
            assertNull(result.get().getReferralUserId());
            verify(userMapper, times(2)).selectByInternalReferralCode(TEST_REFERRAL_CODE);
        }

        @Test
        @DisplayName("无内推码：推荐人信息应为null")
        void findById_withoutReferralCode_shouldReturnNullReferralUser() {
            Enroll enroll = createTestEnroll();
            enroll.setInternalReferralCode(null);
            College college = createTestCollege();

            when(enrollMapper.selectById(TEST_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(enroll, EnrollDO.class));
            when(collegeMapper.selectById(TEST_COLLEGE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(college, CollegeDO.class));

            Optional<Enroll> result = enrollRepository.findById(TEST_ID);

            assertTrue(result.isPresent());
            assertNull(result.get().getReferralUserName());
            assertNull(result.get().getReferralUserId());
            verify(userMapper, never()).selectByInternalReferralCode(any());
        }
    }
}
