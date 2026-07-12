package com.bluenet.web.api.controller.v1.member;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.api.converter.member.MemberResponseConverter;
import com.bluenet.web.api.converter.userexperience.UserExperienceResponseConverter;
import com.bluenet.web.application.result.member.MemberResult;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.service.MemberAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("MemberController 集成测试")
class MemberControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberAppService memberAppService;

    @MockitoBean
    private MemberResponseConverter memberResponseConverter;

    @MockitoBean
    private UserExperienceResponseConverter userExperienceResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private MemberResult createMemberResult(Long id, String username, Direction direction) {
        return new MemberResult(
                id,
                "2024001001",
                username,
                "昵称",
                direction,
                "后端开发",
                100L,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                RoleType.MEMBER,
                "MEMBER",
                "简介",
                "github",
                200L,
                2024,
                2024);
    }

    @Test
    void getMemberList_noFilter_returnsPage() throws Exception {
        MemberResult result = createMemberResult(1L, "测试用户", Direction.COMPUTER_VISION);
        MemberBriefDTO briefDTO = MemberBriefDTO.builder()
                .id(1L)
                .username("测试用户")
                .nickname("昵称")
                .build();
        when(memberAppService.getMemberList(any())).thenReturn(new PageImpl<>(List.of(result)));
        when(memberResponseConverter.toBriefDTO(any(MemberResult.class))).thenReturn(briefDTO);

        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.empty").value(false))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("测试用户"));
    }

    @Test
    void getMemberList_withDirectionFilter_returnsFiltered() throws Exception {
        MemberResult cvResult = createMemberResult(1L, "CV成员", Direction.COMPUTER_VISION);
        MemberBriefDTO cvDTO = MemberBriefDTO.builder()
                .id(1L)
                .username("CV成员")
                .direction(Direction.COMPUTER_VISION)
                .build();
        when(memberAppService.getMemberList(any())).thenReturn(new PageImpl<>(List.of(cvResult)));
        when(memberResponseConverter.toBriefDTO(any(MemberResult.class))).thenReturn(cvDTO);

        mockMvc.perform(
                get("/api/v1/members")
                        .param("direction", "computer_vision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[*].id").value(1));
    }

    @Test
    void getMemberById_existing_returnsDetail() throws Exception {
        MemberResult result = createMemberResult(1L, "测试用户", Direction.COMPUTER_VISION);
        MemberDetailDTO detailDTO = MemberDetailDTO.builder()
                .id(1L)
                .username("测试用户")
                .studentId("2024001001")
                .build();
        when(memberAppService.getMemberById(1L)).thenReturn(result);
        when(memberResponseConverter.toDetailDTO(any(MemberResult.class))).thenReturn(detailDTO);

        mockMvc.perform(get("/api/v1/members/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("测试用户"))
                .andExpect(jsonPath("$.data.studentId").value("2024001001"));
    }

    @Test
    void getMemberById_nonExistent_returnsNotFound() throws Exception {
        when(memberAppService.getMemberById(9999L)).thenThrow(new DataNotFound("成员不存在"));

        mockMvc.perform(get("/api/v1/members/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getDirectionLeaders_returnsLeaders() throws Exception {
        MemberResult leader = createMemberResult(1L, "方向负责人", Direction.COMPUTER_VISION);
        DirectionLeaderDTO.LeaderInfo leaderInfo = DirectionLeaderDTO.LeaderInfo.builder()
                .id(1L)
                .username("方向负责人")
                .nickname("昵称")
                .build();
        DirectionLeaderDTO leaderDTO = DirectionLeaderDTO.builder()
                .direction(Direction.COMPUTER_VISION)
                .directionName(Direction.COMPUTER_VISION.getDescription())
                .leader(leaderInfo)
                .build();
        when(memberAppService.getDirectionLeaders()).thenReturn(List.of(leader));
        when(memberResponseConverter.toDirectionLeaderDTOs(any())).thenReturn(List.of(leaderDTO));

        mockMvc.perform(get("/api/v1/members/direction-leaders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].direction").value("COMPUTER_VISION"))
                .andExpect(jsonPath("$.data[0].leader.id").value(1))
                .andExpect(jsonPath("$.data[0].leader.username").value("方向负责人"));
    }

    @Test
    void getMemberExperiences_existingTeamMember_returnsExperiences() throws Exception {
        UserExperienceResult result = new UserExperienceResult(
                1L,
                ExperienceType.PROJECT,
                "团队项目",
                "2024.09",
                "2025.06",
                null);
        ExperienceDTO dto = ExperienceDTO.builder()
                .id("1")
                .type("PROJECT")
                .name("团队项目")
                .build();
        when(memberAppService.getMemberExperiences(1L, null)).thenReturn(List.of(result));
        when(userExperienceResponseConverter.toDTOList(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/members/{memberId}/experiences", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("团队项目"));
    }

    @Test
    void getMemberExperiences_nonExistentMember_returnsNotFound() throws Exception {
        when(memberAppService.getMemberExperiences(9999L, null))
                .thenThrow(new DataNotFound("成员不存在"));

        mockMvc.perform(get("/api/v1/members/{memberId}/experiences", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
