package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Page<MemberVO> findAll(Direction direction, Pageable pageable);
    Optional<MemberVO> findById(Long id);
    List<MemberVO> findDirectionLeaders();
}
