package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import com.bluenet.web.domain.repository.MemberRepository;

@DisplayName("MemberDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class MemberDomainServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberDomainServiceImpl memberDomainService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_USERNAME = "张三";
    private static final String TEST_NICKNAME = "小张";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Integer TEST_ENROLLMENT_YEAR = 2021;

    private MemberVO createTestMemberVO() {
        return MemberVO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .nickname(TEST_NICKNAME)
                .direction(TEST_DIRECTION)
                .job("后端开发")
                .avatarFileId(123L)
                .college("计算机学院")
                .major("计算机科学与技术")
                .enrollmentYear(TEST_ENROLLMENT_YEAR)
                .build();
    }

    private MemberVO createTestMemberVO(Long id, String username, Direction direction, Integer enrollmentYear) {
        return MemberVO.builder()
                .id(id)
                .username(username)
                .nickname(username + "昵称")
                .direction(direction)
                .job("开发")
                .enrollmentYear(enrollmentYear)
                .build();
    }

    @Nested
    @DisplayName("getMemberList 方法测试")
    class GetMemberListTests {

        @Test
        @DisplayName("正常情况：无方向筛选，应返回分页成员列表")
        void getMemberList_withoutDirection_shouldReturnPagedMembers() {
            List<MemberVO> members = new ArrayList<>();
            members.add(createTestMemberVO(1L, "张三", Direction.COMPUTER_VISION, 2021));
            members.add(createTestMemberVO(2L, "李四", Direction.EMBEDDED, 2022));

            Page<MemberVO> expectedPage = new PageImpl<>(members);
            Pageable pageable = PageRequest.of(0, 20);

            when(memberRepository.findAll(null, pageable)).thenReturn(expectedPage);

            Page<MemberVO> result = memberDomainService.getMemberList(null, pageable);

            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals("张三", result.getContent().get(0).getUsername());
            verify(memberRepository).findAll(null, pageable);
        }

        @Test
        @DisplayName("正常情况：有方向筛选，应返回筛选后的成员列表")
        void getMemberList_withDirection_shouldReturnFilteredMembers() {
            List<MemberVO> members = new ArrayList<>();
            members.add(createTestMemberVO(1L, "张三", Direction.COMPUTER_VISION, 2021));
            members.add(createTestMemberVO(2L, "王五", Direction.COMPUTER_VISION, 2022));

            Page<MemberVO> expectedPage = new PageImpl<>(members);
            Pageable pageable = PageRequest.of(0, 20);

            when(memberRepository.findAll(Direction.COMPUTER_VISION, pageable)).thenReturn(expectedPage);

            Page<MemberVO> result = memberDomainService.getMemberList(Direction.COMPUTER_VISION, pageable);

            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            result.getContent().forEach(m -> assertEquals(Direction.COMPUTER_VISION, m.getDirection()));
            verify(memberRepository).findAll(Direction.COMPUTER_VISION, pageable);
        }

        @Test
        @DisplayName("空数据：应返回空页")
        void getMemberList_noMembers_shouldReturnEmptyPage() {
            Page<MemberVO> expectedPage = new PageImpl<>(new ArrayList<>());
            Pageable pageable = PageRequest.of(0, 20);

            when(memberRepository.findAll(null, pageable)).thenReturn(expectedPage);

            Page<MemberVO> result = memberDomainService.getMemberList(null, pageable);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            verify(memberRepository).findAll(null, pageable);
        }

        @Test
        @DisplayName("分页参数：第二页数据应正确返回")
        void getMemberList_secondPage_shouldReturnCorrectData() {
            List<MemberVO> members = new ArrayList<>();
            members.add(createTestMemberVO(21L, "成员21", Direction.EMBEDDED, 2023));

            Page<MemberVO> expectedPage = new PageImpl<>(members, PageRequest.of(1, 20), 21);
            Pageable pageable = PageRequest.of(1, 20);

            when(memberRepository.findAll(null, pageable)).thenReturn(expectedPage);

            Page<MemberVO> result = memberDomainService.getMemberList(null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(21, result.getTotalElements());
            assertEquals(2, result.getTotalPages());
            verify(memberRepository).findAll(null, pageable);
        }
    }

    @Nested
    @DisplayName("getMemberById 方法测试")
    class GetMemberByIdTests {

        @Test
        @DisplayName("正常情况：应返回成员VO")
        void getMemberById_existingMember_shouldReturnVO() {
            MemberVO member = createTestMemberVO();
            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.of(member));

            Optional<MemberVO> result = memberDomainService.getMemberById(TEST_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_ID, result.get().getId());
            assertEquals(TEST_USERNAME, result.get().getUsername());
            verify(memberRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("成员不存在：应返回空Optional")
        void getMemberById_nonExistingMember_shouldReturnEmpty() {
            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            Optional<MemberVO> result = memberDomainService.getMemberById(TEST_ID);

            assertTrue(result.isEmpty());
            verify(memberRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("边界情况：ID为null时，应正常处理")
        void getMemberById_nullId_shouldHandleGracefully() {
            when(memberRepository.findById(null)).thenReturn(Optional.empty());

            Optional<MemberVO> result = memberDomainService.getMemberById(null);

            assertTrue(result.isEmpty());
            verify(memberRepository).findById(null);
        }
    }

    @Nested
    @DisplayName("getDirectionLeaders 方法测试")
    class GetDirectionLeadersTests {

        @Test
        @DisplayName("正常情况：应返回所有方向的负责人列表")
        void getDirectionLeaders_withLeaders_shouldReturnList() {
            List<MemberVO> leaders = new ArrayList<>();
            leaders.add(createTestMemberVO(1L, "张组长", Direction.COMPUTER_VISION, 2020));
            leaders.add(createTestMemberVO(2L, "李组长", Direction.STRUCTURAL_DESIGN, 2019));
            leaders.add(createTestMemberVO(3L, "王组长", Direction.EMBEDDED, 2020));

            when(memberRepository.findDirectionLeaders()).thenReturn(leaders);

            List<MemberVO> result = memberDomainService.getDirectionLeaders();

            assertNotNull(result);
            assertEquals(3, result.size());
            verify(memberRepository).findDirectionLeaders();
        }

        @Test
        @DisplayName("部分方向无负责人：应返回已有的负责人")
        void getDirectionLeaders_partialLeaders_shouldReturnExistingLeaders() {
            List<MemberVO> leaders = new ArrayList<>();
            leaders.add(createTestMemberVO(1L, "张组长", Direction.COMPUTER_VISION, 2020));

            when(memberRepository.findDirectionLeaders()).thenReturn(leaders);

            List<MemberVO> result = memberDomainService.getDirectionLeaders();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Direction.COMPUTER_VISION, result.get(0).getDirection());
            verify(memberRepository).findDirectionLeaders();
        }

        @Test
        @DisplayName("无负责人：应返回空列表")
        void getDirectionLeaders_noLeaders_shouldReturnEmptyList() {
            when(memberRepository.findDirectionLeaders()).thenReturn(new ArrayList<>());

            List<MemberVO> result = memberDomainService.getDirectionLeaders();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(memberRepository).findDirectionLeaders();
        }
    }
}
