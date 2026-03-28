package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.service.MemberDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberDomainServiceImpl implements MemberDomainService {
    private final MemberRepository memberRepository;

    @Override
    public Page<MemberVO> getMemberList(Direction direction, Pageable pageable) {
        log.debug(
                "Getting member list with direction: {}, page: {}, size: {}",
                direction,
                pageable.getPageNumber(),
                pageable.getPageSize());
        return memberRepository.findAll(direction, pageable);
    }

    @Override
    public Optional<MemberVO> getMemberById(Long id) {
        log.debug("Getting member by id: {}", id);
        return memberRepository.findById(id);
    }

    @Override
    public List<MemberVO> getDirectionLeaders() {
        log.debug("Getting direction leaders");
        return memberRepository.findDirectionLeaders();
    }
}
