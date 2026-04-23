package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.enumerate.Direction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 成员仓库接口
 * <p>
 * 负责成员数据的查询操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface MemberRepository {
    /**
     * 查询全部成员记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的成员实体。
     */
    Page<Member> findAll(Direction direction, Pageable pageable);

    /**
     * 按主键查询成员记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的成员实体；不存在时为空。
     */
    Optional<Member> findById(Long id);

    /**
     * 查询各技术方向负责人成员列表。
     *
     * @return 满足条件的成员实体集合。
     */
    List<Member> findDirectionLeaders();
}
