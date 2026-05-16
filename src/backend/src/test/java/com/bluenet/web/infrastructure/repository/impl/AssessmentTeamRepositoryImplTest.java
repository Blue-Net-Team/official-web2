package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.infrastructure.repository.converter.AssessmentTeamRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamMemberDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTeamMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTeamMemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AssessmentTeamRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTeamRepositoryImplTest {

    @Mock
    private AssessmentTeamMapper assessmentTeamMapper;

    @Mock
    private AssessmentTeamMemberMapper assessmentTeamMemberMapper;

    @Spy
    private AssessmentTeamRepositoryConverter converter = new AssessmentTeamRepositoryConverter();

    @InjectMocks
    private AssessmentTeamRepositoryImpl assessmentTeamRepository;

    private static final Long TEST_TEAM_ID = 10L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TIME_ID = 20L;
    private static final String TEST_INVITE_CODE = "ABC123";

    private AssessmentTeamDO createTestTeamDO() {
        return AssessmentTeamDO.builder()
                .id(TEST_TEAM_ID)
                .assessmentTimeId(TEST_TIME_ID)
                .leaderId(TEST_USER_ID)
                .name("测试队伍")
                .inviteCode(TEST_INVITE_CODE)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AssessmentTeamMemberDO createTestMemberDO(Long id, Long userId) {
        return AssessmentTeamMemberDO.builder()
                .id(id)
                .teamId(TEST_TEAM_ID)
                .userId(userId)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("findById 方法测试")
    class FindByIdTests {

        @Test
        @DisplayName("队伍存在：应返回实体")
        void findById_existing_shouldReturnEntity() {
            when(assessmentTeamMapper.selectById(TEST_TEAM_ID)).thenReturn(createTestTeamDO());

            Optional<AssessmentTeam> result = assessmentTeamRepository.findById(TEST_TEAM_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_TEAM_ID, result.get().getId());
            assertEquals(TEST_USER_ID, result.get().getLeaderId());
        }

        @Test
        @DisplayName("队伍不存在：应返回空")
        void findById_notExisting_shouldReturnEmpty() {
            when(assessmentTeamMapper.selectById(TEST_TEAM_ID)).thenReturn(null);

            Optional<AssessmentTeam> result = assessmentTeamRepository.findById(TEST_TEAM_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByInviteCode 方法测试")
    class FindByInviteCodeTests {

        @Test
        @DisplayName("邀请码存在：应返回实体")
        void findByInviteCode_existing_shouldReturnEntity() {
            when(assessmentTeamMapper.selectByInviteCode(TEST_INVITE_CODE)).thenReturn(createTestTeamDO());

            Optional<AssessmentTeam> result = assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE);

            assertTrue(result.isPresent());
            assertEquals(TEST_INVITE_CODE, result.get().getInviteCode());
        }

        @Test
        @DisplayName("邀请码不存在：应返回空")
        void findByInviteCode_notExisting_shouldReturnEmpty() {
            when(assessmentTeamMapper.selectByInviteCode("INVALID")).thenReturn(null);

            Optional<AssessmentTeam> result = assessmentTeamRepository.findByInviteCode("INVALID");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByAssessmentTimeIdAndUserId 方法测试")
    class FindByAssessmentTimeIdAndUserIdTests {

        @Test
        @DisplayName("用户已加入队伍：应返回实体")
        void findByAssessmentTimeIdAndUserId_existing_shouldReturnEntity() {
            when(assessmentTeamMapper.selectByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(createTestTeamDO());

            Optional<AssessmentTeam> result = assessmentTeamRepository
                    .findByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_TIME_ID, result.get().getAssessmentTimeId());
        }

        @Test
        @DisplayName("用户未加入队伍：应返回空")
        void findByAssessmentTimeIdAndUserId_notExisting_shouldReturnEmpty() {
            when(assessmentTeamMapper.selectByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(null);

            Optional<AssessmentTeam> result = assessmentTeamRepository
                    .findByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("existsByAssessmentTimeIdAndUserId 方法测试")
    class ExistsByAssessmentTimeIdAndUserIdTests {

        @Test
        @DisplayName("用户已加入队伍：应返回true")
        void existsByAssessmentTimeIdAndUserId_existing_shouldReturnTrue() {
            when(assessmentTeamMapper.selectByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(createTestTeamDO());

            boolean result = assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("用户未加入队伍：应返回false")
        void existsByAssessmentTimeIdAndUserId_notExisting_shouldReturnFalse() {
            when(assessmentTeamMapper.selectByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(null);

            boolean result = assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("save 方法测试")
    class SaveTests {

        @Test
        @DisplayName("保存队伍：应插入队伍和队长成员")
        void save_valid_shouldInsertTeamAndLeaderMember() {
            AssessmentTeam team = AssessmentTeam.create(TEST_TIME_ID, TEST_USER_ID, "测试队伍", "XYZ789");
            when(assessmentTeamMapper.insert(any(AssessmentTeamDO.class))).thenAnswer(invocation -> {
                AssessmentTeamDO dataObject = invocation.getArgument(0);
                dataObject.setId(TEST_TEAM_ID);
                return 1;
            });

            assessmentTeamRepository.save(team);

            assertEquals(TEST_TEAM_ID, team.getId());
            verify(assessmentTeamMapper).insert(any(AssessmentTeamDO.class));
            verify(assessmentTeamMemberMapper).insert(any(AssessmentTeamMemberDO.class));
        }
    }

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("更新队伍：应调用updateById")
        void update_valid_shouldCallUpdateById() {
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    TEST_TEAM_ID,
                    TEST_TIME_ID,
                    TEST_USER_ID,
                    "新名称",
                    TEST_INVITE_CODE,
                    AssessmentTeam.TeamStatus.DISBANDED,
                    LocalDateTime.now());

            assessmentTeamRepository.update(team);

            verify(assessmentTeamMapper).updateById(any(AssessmentTeamDO.class));
        }
    }

    @Nested
    @DisplayName("deleteById 方法测试")
    class DeleteByIdTests {

        @Test
        @DisplayName("删除队伍：应先删除成员再删除队伍")
        void deleteById_valid_shouldDeleteMembersThenTeam() {
            assessmentTeamRepository.deleteById(TEST_TEAM_ID);

            verify(assessmentTeamMemberMapper).delete(any());
            verify(assessmentTeamMapper).deleteById(TEST_TEAM_ID);
        }
    }

    @Nested
    @DisplayName("updateLeader 方法测试")
    class UpdateLeaderTests {

        @Test
        @DisplayName("转让队长：应调用updateLeader")
        void updateLeader_valid_shouldCallMapper() {
            Long newLeaderId = 2L;
            when(assessmentTeamMapper.updateLeader(TEST_TEAM_ID, newLeaderId)).thenReturn(1);

            assessmentTeamRepository.updateLeader(TEST_TEAM_ID, newLeaderId);

            verify(assessmentTeamMapper).updateLeader(TEST_TEAM_ID, newLeaderId);
        }
    }

    @Nested
    @DisplayName("addMember 方法测试")
    class AddMemberTests {

        @Test
        @DisplayName("添加成员：应插入成员记录")
        void addMember_valid_shouldInsertMember() {
            Long newUserId = 2L;

            assessmentTeamRepository.addMember(TEST_TEAM_ID, newUserId);

            verify(assessmentTeamMemberMapper).insert(
                    argThat(
                            (AssessmentTeamMemberDO dataObject) -> dataObject.getTeamId().equals(TEST_TEAM_ID)
                                    && dataObject.getUserId().equals(newUserId)));
        }
    }

    @Nested
    @DisplayName("removeMember 方法测试")
    class RemoveMemberTests {

        @Test
        @DisplayName("移除成员：应调用deleteByTeamIdAndUserId")
        void removeMember_valid_shouldCallDelete() {
            Long memberId = 2L;

            assessmentTeamRepository.removeMember(TEST_TEAM_ID, memberId);

            verify(assessmentTeamMemberMapper).deleteByTeamIdAndUserId(TEST_TEAM_ID, memberId);
        }
    }

    @Nested
    @DisplayName("findMembersByTeamId 方法测试")
    class FindMembersByTeamIdTests {

        @Test
        @DisplayName("有成员：应返回成员列表")
        void findMembersByTeamId_withMembers_shouldReturnList() {
            when(assessmentTeamMemberMapper.selectByTeamId(TEST_TEAM_ID))
                    .thenReturn(
                            List.of(
                                    createTestMemberDO(1L, TEST_USER_ID),
                                    createTestMemberDO(2L, 2L)));

            List<AssessmentTeamMember> result = assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID);

            assertEquals(2, result.size());
            assertEquals(TEST_USER_ID, result.get(0).getUserId());
            assertEquals(2L, result.get(1).getUserId());
        }

        @Test
        @DisplayName("无成员：应返回空列表")
        void findMembersByTeamId_noMembers_shouldReturnEmptyList() {
            when(assessmentTeamMemberMapper.selectByTeamId(TEST_TEAM_ID))
                    .thenReturn(Collections.emptyList());

            List<AssessmentTeamMember> result = assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isMember 方法测试")
    class IsMemberTests {

        @Test
        @DisplayName("是成员：应返回true")
        void isMember_yes_shouldReturnTrue() {
            when(assessmentTeamMemberMapper.existsByTeamIdAndUserId(TEST_TEAM_ID, TEST_USER_ID))
                    .thenReturn(true);

            boolean result = assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_USER_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("不是成员：应返回false")
        void isMember_no_shouldReturnFalse() {
            when(assessmentTeamMemberMapper.existsByTeamIdAndUserId(TEST_TEAM_ID, TEST_USER_ID))
                    .thenReturn(false);

            boolean result = assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_USER_ID);

            assertFalse(result);
        }
    }
}
