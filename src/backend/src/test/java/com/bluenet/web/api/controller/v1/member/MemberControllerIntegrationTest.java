package com.bluenet.web.api.controller.v1.member;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private College saveDefaultCollege() {
        return CollegeFixture.saveDefaultCollege(collegeRepository);
    }

    @Test
    void getMemberList_noFilter_returnsPage() throws Exception {
        College college = saveDefaultCollege();
        User member = UserFixture.member("2024001001")
                .withCollege(college)
                .save(userRepository, passwordEncoder);

        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.empty").value(false))
                .andExpect(jsonPath("$.data.content[0].id").value(member.getId()))
                .andExpect(jsonPath("$.data.content[0].username").value(member.getUsername()));
    }

    @Test
    void getMemberList_withDirectionFilter_returnsFiltered() throws Exception {
        College college = saveDefaultCollege();
        User cvMember = UserFixture.member("2024001001")
                .withCollege(college)
                .withDirection(Direction.COMPUTER_VISION)
                .save(userRepository, passwordEncoder);
        User sdMember = UserFixture.member("2024001002")
                .withCollege(college)
                .withDirection(Direction.STRUCTURAL_DESIGN)
                .save(userRepository, passwordEncoder);

        mockMvc.perform(
                get("/api/v1/members")
                        .param("direction", "computer_vision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(cvMember.getId()))
                .andExpect(jsonPath("$.data.content[*].id").value((int) (long) cvMember.getId()));
    }

    @Test
    void getMemberById_existing_returnsDetail() throws Exception {
        College college = saveDefaultCollege();
        User member = UserFixture.member("2024001001")
                .withCollege(college)
                .save(userRepository, passwordEncoder);

        mockMvc.perform(get("/api/v1/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.username").value(member.getUsername()))
                .andExpect(jsonPath("$.data.studentId").value(member.getStudentId()));
    }

    @Test
    void getMemberById_nonExistent_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/members/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getDirectionLeaders_returnsLeaders() throws Exception {
        College college = saveDefaultCollege();
        User leader = UserFixture.directionAdmin("2024001001", Direction.COMPUTER_VISION)
                .withCollege(college)
                .save(userRepository, passwordEncoder);

        mockMvc.perform(get("/api/v1/members/direction-leaders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].direction").value("COMPUTER_VISION"))
                .andExpect(jsonPath("$.data[0].leader.id").value(leader.getId()))
                .andExpect(jsonPath("$.data[0].leader.username").value(leader.getUsername()));
    }

    @Test
    void getMemberExperiences_existingTeamMember_returnsExperiences() throws Exception {
        College college = saveDefaultCollege();
        User member = UserFixture.member("2024001001")
                .withCollege(college)
                .save(userRepository, passwordEncoder);
        UserExperience project = UserExperience.create(
                member.getId(),
                ExperienceType.PROJECT,
                "团队项目",
                "{}",
                null,
                null);
        userExperienceRepository.save(project);

        mockMvc.perform(get("/api/v1/members/{memberId}/experiences", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("团队项目"));
    }

    @Test
    void getMemberExperiences_nonExistentMember_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/members/{memberId}/experiences", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
