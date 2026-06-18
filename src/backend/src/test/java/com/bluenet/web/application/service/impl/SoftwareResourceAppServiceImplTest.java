package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.SoftwareResourceResult;
import com.bluenet.web.application.command.softwareresource.SoftwareResourceCommands;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SoftwareResourceAppServiceImplTest - 软件资源应用服务测试")
@ExtendWith(MockitoExtension.class)
class SoftwareResourceAppServiceImplTest {

    @Mock
    private SoftwareResourceRepository softwareResourceRepository;

    @InjectMocks
    private SoftwareResourceAppServiceImpl softwareResourceAppService;

    private SoftwareResource createTestResource() {
        return SoftwareResource.reconstruct(
                1L,
                "VS Code",
                SoftwareResourceDirection.GENERAL,
                "IDE",
                "编辑器",
                "https://code.visualstudio.com/",
                10,
                SoftwareResourceStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-001: 查询已启用资源列表（按方向）")
    void listActiveResources_byDirection_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SoftwareResource resource = createTestResource();
        when(softwareResourceRepository.findActiveByDirection(SoftwareResourceDirection.COMPUTER_VISION, pageable))
                .thenReturn(new PageImpl<>(List.of(resource), pageable, 1));

        var result = softwareResourceAppService
                .listActiveResources(SoftwareResourceDirection.COMPUTER_VISION, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("VS Code", result.getContent().get(0).name());
    }

    @Test
    @DisplayName("TC-002: 创建软件资源（正常场景）")
    void createSoftwareResource_success() {
        SoftwareResourceCommands.CreateSoftwareResourceCommand command = new SoftwareResourceCommands.CreateSoftwareResourceCommand(
                "VS Code",
                SoftwareResourceDirection.GENERAL,
                "IDE",
                "编辑器",
                "https://code.visualstudio.com/",
                10);

        SoftwareResourceResult result = softwareResourceAppService.createSoftwareResource(command);

        assertNotNull(result);
        assertEquals("VS Code", result.name());
        assertEquals(SoftwareResourceDirection.GENERAL, result.direction());
        assertEquals(SoftwareResourceStatus.ACTIVE, result.status());
        verify(softwareResourceRepository).save(any(SoftwareResource.class));
    }

    @Test
    @DisplayName("TC-003: 更新软件资源（正常场景）")
    void updateSoftwareResource_success() {
        Long id = 1L;
        SoftwareResource existing = createTestResource();
        SoftwareResourceCommands.UpdateSoftwareResourceCommand command = new SoftwareResourceCommands.UpdateSoftwareResourceCommand(
                id,
                "Updated VS Code",
                SoftwareResourceDirection.EMBEDDED,
                "Tool",
                "updated desc",
                "https://updated.example.com/",
                20,
                SoftwareResourceStatus.DISABLED);

        when(softwareResourceRepository.findById(id)).thenReturn(Optional.of(existing));

        SoftwareResourceResult result = softwareResourceAppService.updateSoftwareResource(command);

        assertEquals("Updated VS Code", result.name());
        assertEquals(SoftwareResourceDirection.EMBEDDED, result.direction());
        assertEquals(SoftwareResourceStatus.DISABLED, result.status());
        assertEquals(20, result.sortOrder());
        verify(softwareResourceRepository).update(any(SoftwareResource.class));
    }

    @Test
    @DisplayName("TC-004: 更新不存在资源应抛出异常")
    void updateSoftwareResource_notFound_shouldThrow() {
        Long id = 999L;
        SoftwareResourceCommands.UpdateSoftwareResourceCommand command = new SoftwareResourceCommands.UpdateSoftwareResourceCommand(
                id,
                "Tool",
                SoftwareResourceDirection.GENERAL,
                "tool",
                "desc",
                "https://example.com/",
                1,
                SoftwareResourceStatus.ACTIVE);

        when(softwareResourceRepository.findById(id)).thenReturn(Optional.empty());

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> softwareResourceAppService.updateSoftwareResource(command));
        assertEquals("软件资源不存在", exception.getMessage());
    }

    @Test
    @DisplayName("TC-005: 删除软件资源（正常场景）")
    void deleteSoftwareResource_success() {
        Long id = 1L;
        SoftwareResource existing = createTestResource();
        when(softwareResourceRepository.findById(id)).thenReturn(Optional.of(existing));

        softwareResourceAppService.deleteSoftwareResource(id);

        verify(softwareResourceRepository).deleteById(id);
    }

    @Test
    @DisplayName("TC-006: 删除不存在资源应抛出异常")
    void deleteSoftwareResource_notFound_shouldThrow() {
        Long id = 999L;
        when(softwareResourceRepository.findById(id)).thenReturn(Optional.empty());

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> softwareResourceAppService.deleteSoftwareResource(id));
        assertEquals("软件资源不存在", exception.getMessage());
    }
}
