package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.venue.VenueCommands;
import com.bluenet.web.application.result.venue.VenueResult;
import com.bluenet.web.application.service.VenueAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.VenueRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * VenueAppServiceImpl 集成测试。
 *
 * <p>
 * 验证场地应用服务的查询、创建、更新、删除以及图片更新逻辑。
 * </p>
 */
@DisplayName("VenueAppServiceImpl 集成测试")
class VenueAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VenueAppService venueAppService;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createVenue: 应创建场地并持久化")
    void createVenue_shouldCreateAndPersist() {
        VenueCommands.CreateVenueCommand command = new VenueCommands.CreateVenueCommand(
                "开放实验室", "团队协作空间", "供团队进行项目开发与讨论", null, 10);

        VenueResult result = venueAppService.createVenue(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("开放实验室");
        assertThat(result.subtitle()).isEqualTo("团队协作空间");
        assertThat(result.description()).isEqualTo("供团队进行项目开发与讨论");
        assertThat(result.imageUrl()).isNull();
        assertThat(result.imageFileId()).isNull();
        assertThat(result.sortOrder()).isEqualTo(10);
        assertThat(venueRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(venue -> {
                    assertThat(venue.getName()).isEqualTo("开放实验室");
                    assertThat(venue.getSubtitle()).isEqualTo("团队协作空间");
                    assertThat(venue.getDescription()).isEqualTo("供团队进行项目开发与讨论");
                    assertThat(venue.getSortOrder()).isEqualTo(10);
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getAllVenues: 应按 sortOrder 降序返回所有场地")
    void getAllVenues_shouldReturnAllVenuesOrderedBySortOrderDesc() {
        Venue venueA = Venue.create("会议室 A", "副标题A", "描述A", null, 5);
        Venue venueB = Venue.create("开放实验室", "副标题B", "描述B", null, 20);
        Venue venueC = Venue.create("调试区", "副标题C", "描述C", null, 10);
        venueRepository.save(venueA);
        venueRepository.save(venueB);
        venueRepository.save(venueC);

        List<VenueResult> result = venueAppService.getAllVenues();

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(VenueResult::name)
                .containsExactly("开放实验室", "调试区", "会议室 A");
        assertThat(result)
                .extracting(VenueResult::sortOrder)
                .containsExactly(20, 10, 5);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getVenueDetail: 当 imageFileId 存在时应解析图片 URL")
    void getVenueDetail_withImage_shouldResolveImageUrl() {
        File savedFile = FileFixture.save(fileRepository, "venue.png", FileType.NORMAL_IMG);
        Venue venue = Venue.create("开放实验室", "副标题", "描述", savedFile.getId(), 1);
        venueRepository.save(venue);

        VenueResult result = venueAppService.getVenueDetail(venue.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(venue.getId());
        assertThat(result.name()).isEqualTo("开放实验室");
        assertThat(result.imageFileId()).isEqualTo(savedFile.getId());
        assertThat(result.imageUrl()).isEqualTo(savedFile.getUrl());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getVenueDetail: 当无图片时 imageUrl 应为 null")
    void getVenueDetail_withoutImage_shouldReturnNullImageUrl() {
        Venue venue = Venue.create("会议室", "副标题", "描述", null, 2);
        venueRepository.save(venue);

        VenueResult result = venueAppService.getVenueDetail(venue.getId());

        assertThat(result).isNotNull();
        assertThat(result.imageFileId()).isNull();
        assertThat(result.imageUrl()).isNull();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getVenueDetail: 不存在的 id 应抛 DataNotFound")
    void getVenueDetail_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> venueAppService.getVenueDetail(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("场地不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateVenue: 应更新场地并持久化")
    void updateVenue_shouldUpdateAndPersist() {
        Venue venue = Venue.create("旧场地", "旧副标题", "旧描述", null, 1);
        venueRepository.save(venue);
        VenueCommands.UpdateVenueCommand command = new VenueCommands.UpdateVenueCommand(
                venue.getId(), "新场地", "新副标题", "新描述", null, 5);

        VenueResult result = venueAppService.updateVenue(command);

        assertThat(result.name()).isEqualTo("新场地");
        assertThat(result.subtitle()).isEqualTo("新副标题");
        assertThat(result.description()).isEqualTo("新描述");
        assertThat(result.sortOrder()).isEqualTo(5);
        assertThat(venueRepository.findById(venue.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getName()).isEqualTo("新场地");
                    assertThat(updated.getSubtitle()).isEqualTo("新副标题");
                    assertThat(updated.getDescription()).isEqualTo("新描述");
                    assertThat(updated.getSortOrder()).isEqualTo(5);
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateVenue: 不存在的 id 应抛 DataNotFound")
    void updateVenue_notFound_shouldThrowDataNotFound() {
        VenueCommands.UpdateVenueCommand command = new VenueCommands.UpdateVenueCommand(
                99999L, "任意场地", "副标题", "描述", null, 1);

        assertThatThrownBy(() -> venueAppService.updateVenue(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("场地不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteVenue: 应删除场地")
    void deleteVenue_shouldDelete() {
        Venue venue = Venue.create("待删除场地", "副标题", "描述", null, 1);
        venueRepository.save(venue);

        venueAppService.deleteVenue(venue.getId());

        assertThat(venueRepository.findById(venue.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteVenue: 不存在的 id 应抛 DataNotFound")
    void deleteVenue_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> venueAppService.deleteVenue(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("场地不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateVenueImage: 应更新场地图片文件 id")
    void updateVenueImage_shouldUpdateImageFileId() {
        Venue venue = Venue.create("场地", "副标题", "描述", null, 1);
        venueRepository.save(venue);
        File imageFile = FileFixture.save(fileRepository, "new-image.png", FileType.NORMAL_IMG);

        venueAppService.updateVenueImage(venue.getId(), imageFile.getId());

        assertThat(venueRepository.findById(venue.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getImageFileId()).isEqualTo(imageFile.getId()));
    }
}
