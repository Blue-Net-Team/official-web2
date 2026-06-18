package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.infrastructure.repository.converter.SoftwareResourceRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import com.bluenet.web.infrastructure.repository.mapper.SoftwareResourceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SoftwareResourceRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class SoftwareResourceRepositoryImplTest {

    @Mock
    private SoftwareResourceMapper softwareResourceMapper;

    @Spy
    private SoftwareResourceRepositoryConverter converter = new SoftwareResourceRepositoryConverter();

    @InjectMocks
    private SoftwareResourceRepositoryImpl softwareResourceRepository;

    private SoftwareResourceDO createTestDO(Long id, String name, SoftwareResourceDirection direction, int sortOrder,
            SoftwareResourceStatus status) {
        return SoftwareResourceDO.builder()
                .id(id)
                .name(name)
                .direction(direction)
                .category("tool")
                .description("desc")
                .externalUrl("https://example.com/" + name)
                .sortOrder(sortOrder)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("findById: 应返回转换后的实体")
    void findById_shouldReturnEntity() {
        SoftwareResourceDO dataObject = createTestDO(
                1L,
                "Tool",
                SoftwareResourceDirection.GENERAL,
                1,
                SoftwareResourceStatus.ACTIVE);
        when(softwareResourceMapper.selectById(1L)).thenReturn(dataObject);

        Optional<SoftwareResource> result = softwareResourceRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Tool", result.get().getName());
    }

    @Test
    @DisplayName("findActiveByDirection: 分页参数应正确转换")
    void findActiveByDirection_shouldConvertPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SoftwareResourceDO> mpPage = new Page<>(1, 10, 1);
        mpPage.setRecords(
                List.of(
                        createTestDO(
                                1L,
                                "Tool",
                                SoftwareResourceDirection.COMPUTER_VISION,
                                1,
                                SoftwareResourceStatus.ACTIVE)));
        List<SoftwareResourceDirection> expectedDirections = List.of(
                SoftwareResourceDirection.COMPUTER_VISION,
                SoftwareResourceDirection.GENERAL);
        when(
                softwareResourceMapper.selectActiveByDirection(
                        any(Page.class),
                        eq(expectedDirections),
                        eq(SoftwareResourceStatus.ACTIVE),
                        eq("git")))
                                .thenReturn(mpPage);

        var result = softwareResourceRepository
                .findActiveByDirection(SoftwareResourceDirection.COMPUTER_VISION, "git", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getNumber());
        verify(softwareResourceMapper).selectActiveByDirection(
                any(Page.class),
                eq(expectedDirections),
                eq(SoftwareResourceStatus.ACTIVE),
                eq("git"));
    }

    @Test
    @DisplayName("findActiveByDirection: 无关键字时应传递 null")
    void findActiveByDirection_nullKeyword_shouldPassNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SoftwareResourceDO> mpPage = new Page<>(1, 10, 0);
        mpPage.setRecords(List.of());
        when(
                softwareResourceMapper.selectActiveByDirection(
                        any(Page.class),
                        eq(List.of(SoftwareResourceDirection.COMPUTER_VISION, SoftwareResourceDirection.GENERAL)),
                        eq(SoftwareResourceStatus.ACTIVE),
                        eq(null)))
                                .thenReturn(mpPage);

        var result = softwareResourceRepository
                .findActiveByDirection(SoftwareResourceDirection.COMPUTER_VISION, null, pageable);

        assertEquals(0, result.getTotalElements());
        verify(softwareResourceMapper).selectActiveByDirection(
                any(Page.class),
                eq(List.of(SoftwareResourceDirection.COMPUTER_VISION, SoftwareResourceDirection.GENERAL)),
                eq(SoftwareResourceStatus.ACTIVE),
                eq(null));
    }

    @Test
    @DisplayName("save: 应设置实体 ID")
    void save_shouldSetEntityId() {
        SoftwareResource resource = SoftwareResource.create(
                "Tool",
                SoftwareResourceDirection.GENERAL,
                "tool",
                "desc",
                "https://example.com",
                1);
        when(softwareResourceMapper.insert(any(SoftwareResourceDO.class))).thenAnswer(invocation -> {
            SoftwareResourceDO dataObject = invocation.getArgument(0);
            dataObject.setId(100L);
            return 1;
        });

        Long id = softwareResourceRepository.save(resource);

        assertEquals(100L, id);
        assertEquals(100L, resource.getId());
    }
}
