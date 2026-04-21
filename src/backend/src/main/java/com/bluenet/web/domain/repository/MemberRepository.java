package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    /**
     * 查询全部成员 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的成员 结果。
     */
    Page<MemberVO> findAll(Direction direction, Pageable pageable);
    /**
     * 按主键查询成员 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的成员 结果；不存在时为空。
     */
    Optional<MemberVO> findById(Long id);
    /**
     * 查询各技术方向负责人成员列表。
     *
     * @return 满足条件的成员 结果集合。
     */
    List<MemberVO> findDirectionLeaders();
}
