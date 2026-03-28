package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.domain.exception.DataNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.api.dto.member.MemberListQueryDTO;
import com.bluenet.web.application.converter.MemberConverter;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.MemberVO;
import com.bluenet.web.domain.service.MemberDomainService;

@DisplayName("MemberServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private MemberConverter memberConverter;

    @InjectMocks
    private MemberServiceImpl memberService;

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
                .gender(Gender.MALE)
                .githubUsername("zhangsan")
                .wechatQrcode("/api/v1/files/456")
                .enrollmentYear(TEST_ENROLLMENT_YEAR)
                .roleName("MEMBER")
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
                .roleName("MEMBER")
                .build();
    }

    private MemberBriefDTO createTestMemberBriefDTO() {
        return MemberBriefDTO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .nickname(TEST_NICKNAME)
                .direction(TEST_DIRECTION)
                .job("后端开发")
                .avatarFileId(123L)
                .college("计算机学院")
                .major("计算机科学与技术")
                .enrollmentYear(TEST_ENROLLMENT_YEAR)
                .roleName("MEMBER")
                .build();
    }

    private MemberDetailDTO createTestMemberDetailDTO() {
        return MemberDetailDTO.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .nickname(TEST_NICKNAME)
                .direction(TEST_DIRECTION)
                .job("后端开发")
                .avatarFileId(123L)
                .college("计算机学院")
                .major("计算机科学与技术")
                .gender(Gender.MALE)
                .githubUsername("zhangsan")
                .wechatQrcode("/api/v1/files/456")
                .build();
    }

    @Nested
    @DisplayName("getMemberList 方法测试")
    class GetMemberListTests {

        @Test
        @DisplayName("正常情况：应返回分页成员列表DTO")
        void getMemberList_validQuery_shouldReturnPagedDTOs() {
            List<MemberVO> members = new ArrayList<>();
            members.add(createTestMemberVO());

            Page<MemberVO> voPage = new PageImpl<>(members);
            MemberBriefDTO briefDTO = createTestMemberBriefDTO();

            MemberListQueryDTO query = MemberListQueryDTO.builder()
                    .page(0)
                    .size(20)
                    .direction(null)
                    .build();

            when(memberDomainService.getMemberList(eq(null), any(Pageable.class))).thenReturn(voPage);
            when(memberConverter.toBriefDTO(any(MemberVO.class))).thenReturn(briefDTO);

            PageDTO<MemberBriefDTO> result = memberService.getMemberList(query);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(TEST_ID, result.getContent().get(0).getId());
            verify(memberDomainService).getMemberList(eq(null), any(Pageable.class));
            verify(memberConverter).toBriefDTO(any(MemberVO.class));
        }

        @Test
        @DisplayName("方向筛选：应传递方向参数")
        void getMemberList_withDirection_shouldPassDirection() {
            List<MemberVO> members = new ArrayList<>();
            members.add(createTestMemberVO());

            Page<MemberVO> voPage = new PageImpl<>(members);
            MemberBriefDTO briefDTO = createTestMemberBriefDTO();

            MemberListQueryDTO query = MemberListQueryDTO.builder()
                    .page(0)
                    .size(20)
                    .direction(Direction.COMPUTER_VISION)
                    .build();

            when(memberDomainService.getMemberList(eq(Direction.COMPUTER_VISION), any(Pageable.class)))
                    .thenReturn(voPage);
            when(memberConverter.toBriefDTO(any(MemberVO.class))).thenReturn(briefDTO);

            PageDTO<MemberBriefDTO> result = memberService.getMemberList(query);

            assertNotNull(result);
            verify(memberDomainService).getMemberList(eq(Direction.COMPUTER_VISION), any(Pageable.class));
        }

        @Test
        @DisplayName("分页参数：默认值应正确设置")
        void getMemberList_defaultParams_shouldUseDefaults() {
            List<MemberVO> members = new ArrayList<>();
            Page<MemberVO> voPage = new PageImpl<>(members);

            MemberListQueryDTO query = MemberListQueryDTO.builder().build();

            when(memberDomainService.getMemberList(eq(null), any(Pageable.class))).thenReturn(voPage);

            memberService.getMemberList(query);

            verify(memberDomainService).getMemberList(
                    eq(null),
                    argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 20));
        }

        @Test
        @DisplayName("分页参数：超过最大值应被限制")
        void getMemberList_exceedMaxSize_shouldBeLimited() {
            List<MemberVO> members = new ArrayList<>();
            Page<MemberVO> voPage = new PageImpl<>(members);

            MemberListQueryDTO query = MemberListQueryDTO.builder()
                    .page(0)
                    .size(200)
                    .build();

            when(memberDomainService.getMemberList(eq(null), any(Pageable.class))).thenReturn(voPage);

            memberService.getMemberList(query);

            verify(memberDomainService).getMemberList(eq(null), argThat(pageable -> pageable.getPageSize() == 100));
        }

        @Test
        @DisplayName("空数据：应返回空页")
        void getMemberList_noMembers_shouldReturnEmptyPage() {
            Page<MemberVO> voPage = new PageImpl<>(new ArrayList<>());

            MemberListQueryDTO query = MemberListQueryDTO.builder()
                    .page(0)
                    .size(20)
                    .build();

            when(memberDomainService.getMemberList(eq(null), any(Pageable.class))).thenReturn(voPage);

            PageDTO<MemberBriefDTO> result = memberService.getMemberList(query);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("getMemberById 方法测试")
    class GetMemberByIdTests {

        @Test
        @DisplayName("正常情况：应返回成员详情DTO")
        void getMemberById_existingMember_shouldReturnDetailDTO() {
            MemberVO member = createTestMemberVO();
            MemberDetailDTO detailDTO = createTestMemberDetailDTO();

            when(memberDomainService.getMemberById(TEST_ID)).thenReturn(Optional.of(member));
            when(memberConverter.toDetailDTO(member)).thenReturn(detailDTO);

            MemberDetailDTO result = memberService.getMemberById(TEST_ID);

            assertNotNull(result);
            assertEquals(TEST_ID, result.getId());
            assertEquals(TEST_USERNAME, result.getUsername());
            verify(memberDomainService).getMemberById(TEST_ID);
            verify(memberConverter).toDetailDTO(member);
        }

        @Test
        @DisplayName("成员不存在：应抛出GlobalException")
        void getMemberById_nonExistingMember_shouldThrowException() {
            when(memberDomainService.getMemberById(TEST_ID)).thenReturn(Optional.empty());

            DataNotFound exception = assertThrows(
                    DataNotFound.class,
                    () -> memberService.getMemberById(TEST_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
            assertEquals("成员不存在", exception.getMessage());
            verify(memberDomainService).getMemberById(TEST_ID);
            verify(memberConverter, never()).toDetailDTO(any());
        }
    }

    @Nested
    @DisplayName("getDirectionLeaders 方法测试")
    class GetDirectionLeadersTests {

        @Test
        @DisplayName("正常情况：应返回所有方向的负责人DTO列表")
        void getDirectionLeaders_withLeaders_shouldReturnDTOs() {
            List<MemberVO> leaders = new ArrayList<>();
            leaders.add(createTestMemberVO(1L, "张组长", Direction.COMPUTER_VISION, 2020));
            leaders.add(createTestMemberVO(2L, "李组长", Direction.STRUCTURAL_DESIGN, 2019));
            leaders.add(createTestMemberVO(3L, "王组长", Direction.EMBEDDED, 2020));

            List<DirectionLeaderDTO> expectedDTOs = new ArrayList<>();
            expectedDTOs.add(
                    DirectionLeaderDTO.builder()
                            .direction(Direction.COMPUTER_VISION)
                            .directionName("计算机视觉")
                            .leader(
                                    DirectionLeaderDTO.LeaderInfo.builder()
                                            .id(1L)
                                            .username("张组长")
                                            .build())
                            .build());

            when(memberDomainService.getDirectionLeaders()).thenReturn(leaders);
            when(memberConverter.toDirectionLeaderDTOs(leaders)).thenReturn(expectedDTOs);

            List<DirectionLeaderDTO> result = memberService.getDirectionLeaders();

            assertNotNull(result);
            verify(memberDomainService).getDirectionLeaders();
            verify(memberConverter).toDirectionLeaderDTOs(leaders);
        }

        @Test
        @DisplayName("无负责人：应返回空列表")
        void getDirectionLeaders_noLeaders_shouldReturnEmptyList() {
            when(memberDomainService.getDirectionLeaders()).thenReturn(new ArrayList<>());
            when(memberConverter.toDirectionLeaderDTOs(new ArrayList<>())).thenReturn(new ArrayList<>());

            List<DirectionLeaderDTO> result = memberService.getDirectionLeaders();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(memberDomainService).getDirectionLeaders();
            verify(memberConverter).toDirectionLeaderDTOs(new ArrayList<>());
        }
    }
}
