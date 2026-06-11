package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bluenet.web.application.MemberResult;
import com.bluenet.web.application.command.member.MemberCommands;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.service.UserExperienceDomainService;
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

@DisplayName("MemberAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class MemberAppServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserExperienceDomainService userExperienceDomainService;

    @InjectMocks
    private MemberAppServiceImpl memberAppService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_USERNAME = "张三";
    private static final String TEST_NICKNAME = "小张";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Integer TEST_ENROLLMENT_YEAR = 2021;

    private Member createTestMember() {
        return Member.reconstruct(
                TEST_ID,
                "2021010001",
                TEST_USERNAME,
                TEST_NICKNAME,
                TEST_DIRECTION,
                "后端开发",
                123L,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                RoleType.MEMBER,
                "MEMBER",
                "热爱编程",
                "zhangsan",
                456L,
                TEST_ENROLLMENT_YEAR,
                null);
    }

    private Member createTestMember(Long id, String username, Direction direction, Integer enrollmentYear) {
        return Member.reconstruct(
                id,
                null,
                username,
                username + "昵称",
                direction,
                "开发",
                null,
                null,
                null,
                null,
                RoleType.MEMBER,
                "MEMBER",
                null,
                null,
                null,
                enrollmentYear,
                null);
    }

    @Nested
    @DisplayName("getMemberList 方法测试")
    class GetMemberListTests {

        @Test
        @DisplayName("正常情况：应返回分页成员结果")
        void getMemberList_validCommand_shouldReturnPagedResults() {
            List<Member> members = new ArrayList<>();
            members.add(createTestMember());

            Page<Member> memberPage = new PageImpl<>(members);

            MemberCommands.GetMemberListCommand command = new MemberCommands.GetMemberListCommand(null, 0, 20);

            when(memberRepository.findAll(isNull(), any(Pageable.class))).thenReturn(memberPage);

            Page<MemberResult> result = memberAppService.getMemberList(command);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(TEST_ID, result.getContent().get(0).id());
            verify(memberRepository).findAll(isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("方向筛选：应传递方向参数")
        void getMemberList_withDirection_shouldPassDirection() {
            List<Member> members = new ArrayList<>();
            members.add(createTestMember());

            Page<Member> memberPage = new PageImpl<>(members);

            MemberCommands.GetMemberListCommand command = new MemberCommands.GetMemberListCommand(
                    Direction.COMPUTER_VISION, 0, 20);

            when(memberRepository.findAll(eq(Direction.COMPUTER_VISION), any(Pageable.class)))
                    .thenReturn(memberPage);

            Page<MemberResult> result = memberAppService.getMemberList(command);

            assertNotNull(result);
            verify(memberRepository).findAll(eq(Direction.COMPUTER_VISION), any(Pageable.class));
        }

        @Test
        @DisplayName("分页参数：默认值应正确设置")
        void getMemberList_defaultParams_shouldUseDefaults() {
            List<Member> members = new ArrayList<>();
            Page<Member> memberPage = new PageImpl<>(members);

            MemberCommands.GetMemberListCommand command = new MemberCommands.GetMemberListCommand(null, null, null);

            when(memberRepository.findAll(isNull(), any(Pageable.class))).thenReturn(memberPage);

            memberAppService.getMemberList(command);

            verify(memberRepository).findAll(
                    isNull(),
                    argThat((Pageable pageable) -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 20));
        }

        @Test
        @DisplayName("分页参数：超过最大值应被限制")
        void getMemberList_exceedMaxSize_shouldBeLimited() {
            List<Member> members = new ArrayList<>();
            Page<Member> memberPage = new PageImpl<>(members);

            MemberCommands.GetMemberListCommand command = new MemberCommands.GetMemberListCommand(null, 0, 200);

            when(memberRepository.findAll(isNull(), any(Pageable.class))).thenReturn(memberPage);

            memberAppService.getMemberList(command);

            verify(memberRepository).findAll(isNull(), argThat((Pageable pageable) -> pageable.getPageSize() == 100));
        }

        @Test
        @DisplayName("空数据：应返回空页")
        void getMemberList_noMembers_shouldReturnEmptyPage() {
            Page<Member> memberPage = new PageImpl<>(new ArrayList<>());

            MemberCommands.GetMemberListCommand command = new MemberCommands.GetMemberListCommand(null, 0, 20);

            when(memberRepository.findAll(isNull(), any(Pageable.class))).thenReturn(memberPage);

            Page<MemberResult> result = memberAppService.getMemberList(command);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("getMemberById 方法测试")
    class GetMemberByIdTests {

        @Test
        @DisplayName("正常情况：应返回成员结果")
        void getMemberById_existingMember_shouldReturnResult() {
            Member member = createTestMember();

            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.of(member));

            MemberResult result = memberAppService.getMemberById(TEST_ID);

            assertNotNull(result);
            assertEquals(TEST_ID, result.id());
            assertEquals(TEST_USERNAME, result.username());
            verify(memberRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("成员不存在：应抛出GlobalException")
        void getMemberById_nonExistingMember_shouldThrowException() {
            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            DataNotFound exception = assertThrows(
                    DataNotFound.class,
                    () -> memberAppService.getMemberById(TEST_ID));

            assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
            assertEquals("成员不存在", exception.getMessage());
            verify(memberRepository).findById(TEST_ID);
        }
    }

    @Nested
    @DisplayName("getDirectionLeaders 方法测试")
    class GetDirectionLeadersTests {

        @Test
        @DisplayName("正常情况：应返回所有方向负责人结果列表")
        void getDirectionLeaders_withLeaders_shouldReturnResults() {
            List<Member> leaders = new ArrayList<>();
            leaders.add(createTestMember(1L, "张组长", Direction.COMPUTER_VISION, 2020));
            leaders.add(createTestMember(2L, "李组长", Direction.STRUCTURAL_DESIGN, 2019));
            leaders.add(createTestMember(3L, "王组长", Direction.EMBEDDED, 2020));

            when(memberRepository.findDirectionLeaders()).thenReturn(leaders);

            List<MemberResult> result = memberAppService.getDirectionLeaders();

            assertNotNull(result);
            assertEquals(3, result.size());
            verify(memberRepository).findDirectionLeaders();
        }

        @Test
        @DisplayName("无负责人：应返回空列表")
        void getDirectionLeaders_noLeaders_shouldReturnEmptyList() {
            when(memberRepository.findDirectionLeaders()).thenReturn(new ArrayList<>());

            List<MemberResult> result = memberAppService.getDirectionLeaders();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(memberRepository).findDirectionLeaders();
        }
    }

    @Nested
    @DisplayName("getMemberExperiences 方法测试")
    class GetMemberExperiencesTests {

        @Test
        @DisplayName("正常情况：应返回经历列表")
        void getMemberExperiences_existingMember_shouldReturnExperiences() {
            Member member = createTestMember();
            List<ExperienceVO> experiences = List.of(
                    ExperienceVO.builder()
                            .id(1L)
                            .type(ExperienceType.PROJECT)
                            .title("测试项目")
                            .startTime("2023-01")
                            .endTime("2023-06")
                            .content(
                                    "{\"role\":\"开发\",\"description\":\"测试\",\"techStack\":[\"Java\"],\"demoUrl\":\"\"}")
                            .build());

            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.of(member));
            when(userExperienceDomainService.getExperiences(TEST_ID)).thenReturn(experiences);

            var result = memberAppService.getMemberExperiences(TEST_ID, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(memberRepository).findById(TEST_ID);
            verify(userExperienceDomainService).getExperiences(TEST_ID);
        }

        @Test
        @DisplayName("成员不存在：应抛出异常")
        void getMemberExperiences_nonExistingMember_shouldThrowException() {
            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            DataNotFound exception = assertThrows(
                    DataNotFound.class,
                    () -> memberAppService.getMemberExperiences(TEST_ID, null));

            assertEquals("成员不存在", exception.getMessage());
        }

        @Test
        @DisplayName("非团队成员：应返回空列表")
        void getMemberExperiences_nonTeamMember_shouldReturnEmptyList() {
            Member candidate = Member.reconstruct(
                    TEST_ID,
                    null,
                    TEST_USERNAME,
                    TEST_NICKNAME,
                    TEST_DIRECTION,
                    null,
                    null,
                    null,
                    null,
                    null,
                    RoleType.CANDIDATE,
                    "CANDIDATE",
                    null,
                    null,
                    null,
                    null,
                    null);

            when(memberRepository.findById(TEST_ID)).thenReturn(Optional.of(candidate));

            var result = memberAppService.getMemberExperiences(TEST_ID, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userExperienceDomainService, never()).getExperiences(any());
        }
    }
}
