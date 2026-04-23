package com.bluenet.web.application.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.application.MemberResult;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;

@DisplayName("MemberAppConverter 单元测试")
class MemberAppConverterTest {

    private final MemberAppConverter converter = new MemberAppConverter();

    private MemberResult createTestMemberResult() {
        return new MemberResult(
                1L,
                "2021010001",
                "张三",
                "小张",
                Direction.COMPUTER_VISION,
                "后端开发",
                123L,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                RoleType.MEMBER,
                "MEMBER",
                "热爱编程",
                "zhangsan",
                "/api/v1/files/456",
                2021,
                null);
    }

    @Nested
    @DisplayName("toBriefDTO 方法测试")
    class ToBriefDTOTests {

        @Test
        @DisplayName("正常情况：应正确转换Result为BriefDTO")
        void toBriefDTO_validResult_shouldConvertCorrectly() {
            MemberResult result = createTestMemberResult();

            MemberBriefDTO dto = converter.toBriefDTO(result);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("张三", dto.getUsername());
            assertEquals("小张", dto.getNickname());
            assertEquals(Direction.COMPUTER_VISION, dto.getDirection());
            assertEquals("后端开发", dto.getJob());
            assertEquals(123L, dto.getAvatarFileId());
            assertEquals("计算机学院", dto.getCollege());
            assertEquals("计算机科学与技术", dto.getMajor());
            assertEquals(2021, dto.getEnrollmentYear());
            assertEquals(Gender.MALE, dto.getGender());
            assertEquals("MEMBER", dto.getRoleName());
        }

        @Test
        @DisplayName("部分字段为空：应正确处理null值")
        void toBriefDTO_partialNullFields_shouldHandleNulls() {
            MemberResult result = new MemberResult(
                    1L, null, "张三", null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null);

            MemberBriefDTO dto = converter.toBriefDTO(result);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("张三", dto.getUsername());
            assertNull(dto.getNickname());
            assertNull(dto.getDirection());
            assertNull(dto.getJob());
            assertNull(dto.getAvatarFileId());
            assertNull(dto.getCollege());
            assertNull(dto.getMajor());
            assertNull(dto.getEnrollmentYear());
            assertNull(dto.getGender());
            assertNull(dto.getRoleName());
        }
    }

    @Nested
    @DisplayName("toDetailDTO 方法测试")
    class ToDetailDTOTests {

        @Test
        @DisplayName("正常情况：应正确转换Result为DetailDTO")
        void toDetailDTO_validResult_shouldConvertCorrectly() {
            MemberResult result = createTestMemberResult();

            MemberDetailDTO dto = converter.toDetailDTO(result);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("张三", dto.getUsername());
            assertEquals("小张", dto.getNickname());
            assertEquals(Direction.COMPUTER_VISION, dto.getDirection());
            assertEquals("后端开发", dto.getJob());
            assertEquals(123L, dto.getAvatarFileId());
            assertEquals("计算机学院", dto.getCollege());
            assertEquals("计算机科学与技术", dto.getMajor());
            assertEquals(Gender.MALE, dto.getGender());
            assertEquals("zhangsan", dto.getGithubUsername());
            assertEquals("/api/v1/files/456", dto.getWechatQrcode());
        }

        @Test
        @DisplayName("部分字段为空：应正确处理null值")
        void toDetailDTO_partialNullFields_shouldHandleNulls() {
            MemberResult result = new MemberResult(
                    1L, null, "张三", null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null);

            MemberDetailDTO dto = converter.toDetailDTO(result);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("张三", dto.getUsername());
            assertNull(dto.getNickname());
            assertNull(dto.getDirection());
            assertNull(dto.getGender());
            assertNull(dto.getGithubUsername());
            assertNull(dto.getWechatQrcode());
        }
    }

    @Nested
    @DisplayName("toDirectionLeaderDTOs 方法测试")
    class ToDirectionLeaderDTOsTests {

        @Test
        @DisplayName("正常情况：应返回所有方向的负责人信息")
        void toDirectionLeaderDTOs_withLeaders_shouldReturnAllDirections() {
            List<MemberResult> leaders = new ArrayList<>();
            leaders.add(
                    new MemberResult(
                            1L, null, "视觉组长", "CV组长", Direction.COMPUTER_VISION,
                            null, 1L, null, null, null, null, null, null, null, null, null, null));
            leaders.add(
                    new MemberResult(
                            2L, null, "结构组长", "结构组长", Direction.STRUCTURAL_DESIGN,
                            null, 2L, null, null, null, null, null, null, null, null, null, null));
            leaders.add(
                    new MemberResult(
                            3L, null, "嵌入式组长", "嵌入式组长", Direction.EMBEDDED,
                            null, 3L, null, null, null, null, null, null, null, null, null, null));

            List<DirectionLeaderDTO> result = converter.toDirectionLeaderDTOs(leaders);

            assertNotNull(result);
            assertEquals(3, result.size());

            for (DirectionLeaderDTO dto : result) {
                assertNotNull(dto.getDirection());
                assertNotNull(dto.getDirectionName());
                assertNotNull(dto.getLeader());
            }
        }

        @Test
        @DisplayName("部分方向无负责人：无负责人的方向leader应为null")
        void toDirectionLeaderDTOs_partialLeaders_shouldHaveNullForMissing() {
            List<MemberResult> leaders = new ArrayList<>();
            leaders.add(
                    new MemberResult(
                            1L, null, "视觉组长", null, Direction.COMPUTER_VISION,
                            null, null, null, null, null, null, null, null, null, null, null, null));

            List<DirectionLeaderDTO> result = converter.toDirectionLeaderDTOs(leaders);

            assertNotNull(result);
            assertEquals(3, result.size());

            DirectionLeaderDTO cvLeader = result.stream()
                    .filter(d -> d.getDirection() == Direction.COMPUTER_VISION)
                    .findFirst()
                    .orElse(null);
            assertNotNull(cvLeader);
            assertNotNull(cvLeader.getLeader());
            assertEquals("视觉组长", cvLeader.getLeader().getUsername());

            DirectionLeaderDTO embeddedLeader = result.stream()
                    .filter(d -> d.getDirection() == Direction.EMBEDDED)
                    .findFirst()
                    .orElse(null);
            assertNotNull(embeddedLeader);
            assertNull(embeddedLeader.getLeader());
        }

        @Test
        @DisplayName("无负责人：所有方向的leader应为null")
        void toDirectionLeaderDTOs_noLeaders_shouldAllBeNull() {
            List<MemberResult> leaders = new ArrayList<>();

            List<DirectionLeaderDTO> result = converter.toDirectionLeaderDTOs(leaders);

            assertNotNull(result);
            assertEquals(3, result.size());

            for (DirectionLeaderDTO dto : result) {
                assertNotNull(dto.getDirection());
                assertNotNull(dto.getDirectionName());
                assertNull(dto.getLeader());
            }
        }

        @Test
        @DisplayName("方向名称应正确映射")
        void toDirectionLeaderDTOs_shouldMapCorrectDirectionNames() {
            List<MemberResult> leaders = new ArrayList<>();

            List<DirectionLeaderDTO> result = converter.toDirectionLeaderDTOs(leaders);

            for (DirectionLeaderDTO dto : result) {
                switch (dto.getDirection()) {
                    case COMPUTER_VISION -> assertEquals("计算机视觉", dto.getDirectionName());
                    case STRUCTURAL_DESIGN -> assertEquals("结构设计", dto.getDirectionName());
                    case EMBEDDED -> assertEquals("嵌入式开发", dto.getDirectionName());
                }
            }
        }
    }
}
