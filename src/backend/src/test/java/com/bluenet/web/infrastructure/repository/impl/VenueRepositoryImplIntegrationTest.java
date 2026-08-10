package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.repository.VenueRepository;
import com.bluenet.web.infrastructure.repository.dataobject.VenueDO;
import com.bluenet.web.infrastructure.repository.mapper.VenueMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VenueRepositoryImpl 集成测试。
 */
@DisplayName("VenueRepositoryImpl 集成测试")
class VenueRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private VenueMapper venueMapper;

    private Venue createVenue(String name, Integer sortOrder) {
        Venue venue = Venue.create(name, name + "副标题", name + "描述", null, sortOrder);
        venueRepository.save(venue);
        return venue;
    }

    @Test
    @DisplayName("save: 新场地应插入并回写ID")
    void save_newVenue_shouldInsertAndReturnId() {
        Venue venue = createVenue("活动室A", 10);

        assertThat(venue.getId()).isNotNull();
        VenueDO dataObject = venueMapper.selectById(venue.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getName()).isEqualTo("活动室A");
        assertThat(dataObject.getSortOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("save: 已有场地应更新字段")
    void save_existingVenue_shouldUpdateFields() {
        Venue venue = createVenue("待更新场地", 5);
        venue.update("更新后场地", "新副标题", "新描述", null, 20);

        venueRepository.save(venue);

        VenueDO updated = venueMapper.selectById(venue.getId());
        assertThat(updated.getName()).isEqualTo("更新后场地");
        assertThat(updated.getSubtitle()).isEqualTo("新副标题");
        assertThat(updated.getSortOrder()).isEqualTo(20);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Venue venue = createVenue("查询场地", 1);

        Optional<Venue> found = venueRepository.findById(venue.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("查询场地");

        assertThat(venueRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findAllOrderBySortOrderDesc: 应按排序号倒序返回")
    void findAllOrderBySortOrderDesc_shouldSortDesc() {
        Venue venue1 = createVenue("排序10", 10);
        Venue venue2 = createVenue("排序30", 30);
        Venue venue3 = createVenue("排序20", 20);

        List<Venue> venues = venueRepository.findAllOrderBySortOrderDesc();

        assertThat(venues)
                .extracting(Venue::getId)
                .containsExactly(venue2.getId(), venue3.getId(), venue1.getId());
    }

    @Test
    @DisplayName("existsById: 应正确判断场地是否存在")
    void existsById_shouldWork() {
        Venue venue = createVenue("存在场地", 1);

        assertThat(venueRepository.existsById(venue.getId())).isTrue();
        assertThat(venueRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("deleteById: 应删除场地")
    void deleteById_shouldRemoveVenue() {
        Venue venue = createVenue("待删除场地", 1);
        Long venueId = venue.getId();

        venueRepository.deleteById(venueId);

        assertThat(venueMapper.selectById(venueId)).isNull();
    }
}
