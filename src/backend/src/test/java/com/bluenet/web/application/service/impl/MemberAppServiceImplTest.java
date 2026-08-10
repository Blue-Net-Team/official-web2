package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.application.query.member.GetMemberListQuery;
import com.bluenet.web.application.result.member.MemberResult;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MemberAppServiceImpl 单元测试。
 *
 * <p>
 * Application 层仅编排查询逻辑，无事务与多 Repository 写操作，因此 mock 下层 Repository，
 * 验证应用服务层的编排、参数传递、异常抛出与结果转换。
 * </p>
 */
@DisplayName("MemberAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class MemberAppServiceImplTest {

    @InjectMocks
    private MemberAppServiceImpl memberAppService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserExperienceRepository userExperienceRepository;

    private Member createMember(Long id, String studentId, RoleType role) {
        return Member.reconstruct(
                id,
                studentId,
                "用户" + studentId,
                "昵称" + studentId,
                Direction.COMPUTER_VISION,
                "开发",
                null,
                "计算机学院",
                "计算机",
                Gender.MALE,
                role,
                role.getName(),
                "bio",
                null,
                null,
                2024,
                2024);
    }

    private UserExperience createExperience(Long id, Long userId, ExperienceType type, String title) {
        return UserExperience.reconstruct(
                id,
                userId,
                type,
                title,
                "{}",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                null);
    }

    @Test
    @DisplayName("getMemberExperiences: 应返回成员经历列表")
    void getMemberExperiences_shouldReturnExperiences() {
        Long memberId = 1L;
        Member member = createMember(memberId, "2026002001", RoleType.MEMBER);
        UserExperience project = createExperience(10L, memberId, ExperienceType.PROJECT, "项目A");
        UserExperience internship = createExperience(11L, memberId, ExperienceType.INTERNSHIP, "公司B");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(userExperienceRepository.findByUserId(memberId)).thenReturn(List.of(project, internship));

        List<UserExperienceResult> results = memberAppService.getMemberExperiences(memberId, null);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("getMemberExperiences: 应按类型过滤")
    void getMemberExperiences_shouldFilterByType() {
        Long memberId = 2L;
        Member member = createMember(memberId, "2026002002", RoleType.MEMBER);
        UserExperience project = createExperience(20L, memberId, ExperienceType.PROJECT, "项目A");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(userExperienceRepository.findByUserIdAndType(memberId, ExperienceType.PROJECT))
                .thenReturn(List.of(project));

        List<UserExperienceResult> results = memberAppService.getMemberExperiences(memberId, "PROJECT");

        assertEquals(1, results.size());
        assertEquals("项目A", results.get(0).title());
        verify(userExperienceRepository, never()).findByUserId(memberId);
    }

    @Test
    @DisplayName("getMemberExperiences: 无效类型应抛异常")
    void getMemberExperiences_invalidType_shouldThrow() {
        Long memberId = 3L;
        Member member = createMember(memberId, "2026002003", RoleType.MEMBER);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(
                BadRequest.class,
                () -> memberAppService.getMemberExperiences(memberId, "INVALID"));
    }

    @Test
    @DisplayName("getMemberExperiences: 非团队成员应返回空列表")
    void getMemberExperiences_nonTeamMember_shouldReturnEmpty() {
        Long candidateId = 4L;
        Member candidate = createMember(candidateId, "2026002004", RoleType.CANDIDATE);

        when(memberRepository.findById(candidateId)).thenReturn(Optional.of(candidate));

        List<UserExperienceResult> results = memberAppService.getMemberExperiences(candidateId, null);

        assertTrue(results.isEmpty());
        verify(userExperienceRepository, never()).findByUserId(any());
        verify(userExperienceRepository, never()).findByUserIdAndType(any(), any());
    }

    @Test
    @DisplayName("getMemberExperiences: 成员不存在应抛异常")
    void getMemberExperiences_memberNotFound_shouldThrow() {
        when(memberRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> memberAppService.getMemberExperiences(-1L, null));
    }

    @Test
    @DisplayName("getMemberById: 应返回成员详情")
    void getMemberById_shouldReturnMember() {
        Long memberId = 5L;
        Member member = createMember(memberId, "2026002005", RoleType.MEMBER);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        MemberResult result = memberAppService.getMemberById(memberId);

        assertEquals(memberId, result.id());
        assertEquals("用户2026002005", result.username());
    }

    @Test
    @DisplayName("getMemberList: 应分页返回成员列表")
    void getMemberList_shouldReturnPage() {
        Long memberId = 6L;
        Member member = createMember(memberId, "2026002006", RoleType.MEMBER);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Member> memberPage = new PageImpl<>(List.of(member), pageable, 1);

        when(memberRepository.findAll(eq((Direction) null), any(Pageable.class))).thenReturn(memberPage);

        PageDTO<MemberResult> page = PageDTO.from(
                memberAppService.getMemberList(new GetMemberListQuery(null, 0, 20)));

        assertEquals(1, page.getTotalElements());
    }
}
