package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberDomainService {
    Page<MemberVO> getMemberList(Direction direction, Pageable pageable);
    Optional<MemberVO> getMemberById(Long id);
    List<MemberVO> getDirectionLeaders();
}
