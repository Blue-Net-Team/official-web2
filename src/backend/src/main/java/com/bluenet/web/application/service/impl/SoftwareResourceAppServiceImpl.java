package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.SoftwareResourceResult;
import com.bluenet.web.application.command.softwareresource.SoftwareResourceCommands;
import com.bluenet.web.application.service.SoftwareResourceAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 软件资源应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class SoftwareResourceAppServiceImpl implements SoftwareResourceAppService {

    private final SoftwareResourceRepository softwareResourceRepository;

    @Override
    public Page<SoftwareResourceResult> listActiveResources(SoftwareResourceDirection direction, String keyword,
            Pageable pageable) {
        return softwareResourceRepository.findActiveByDirection(direction, keyword, pageable)
                .map(this::toResult);
    }

    @Override
    public Page<SoftwareResourceResult> listAllForAdmin(Pageable pageable) {
        return softwareResourceRepository.findAllForAdmin(pageable)
                .map(this::toResult);
    }

    @Override
    @Transactional
    public SoftwareResourceResult createSoftwareResource(
            SoftwareResourceCommands.CreateSoftwareResourceCommand command) {
        SoftwareResource softwareResource = SoftwareResource.create(
                command.name(),
                command.direction(),
                command.category(),
                command.description(),
                command.externalUrl(),
                command.sortOrder());
        softwareResourceRepository.save(softwareResource);
        return toResult(softwareResource);
    }

    @Override
    @Transactional
    public SoftwareResourceResult updateSoftwareResource(
            SoftwareResourceCommands.UpdateSoftwareResourceCommand command) {
        SoftwareResource softwareResource = softwareResourceRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("软件资源不存在"));
        softwareResource.update(
                command.name(),
                command.direction(),
                command.category(),
                command.description(),
                command.externalUrl(),
                command.sortOrder(),
                command.status());
        softwareResourceRepository.update(softwareResource);
        return toResult(softwareResource);
    }

    @Override
    @Transactional
    public void deleteSoftwareResource(Long id) {
        if (!softwareResourceRepository.findById(id).isPresent()) {
            throw new DataNotFound("软件资源不存在");
        }
        softwareResourceRepository.deleteById(id);
    }

    private SoftwareResourceResult toResult(SoftwareResource softwareResource) {
        return new SoftwareResourceResult(
                softwareResource.getId(),
                softwareResource.getName(),
                softwareResource.getDirection(),
                softwareResource.getCategory(),
                softwareResource.getDescription(),
                softwareResource.getExternalUrl(),
                softwareResource.getSortOrder(),
                softwareResource.getStatus());
    }
}
