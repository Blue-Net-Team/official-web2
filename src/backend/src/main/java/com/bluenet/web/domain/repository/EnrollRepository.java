package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 报名仓库接口
 * <p>
 * 负责报名数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface EnrollRepository {
    /**
     * 按主键查询报名申请记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的报名实体；不存在时为空。
     */
    Optional<Enroll> findById(Long id);

    /**
     * 按学号查询报名申请。
     *
     * @param studentId
     *            学生学号。
     * @return 查询到的报名实体；不存在时为空。
     */
    Optional<Enroll> findByStudentId(String studentId);

    /**
     * 判断是否存在满足条件的报名申请记录。
     *
     * @param studentId
     *            学生学号。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByStudentId(String studentId);

    /**
     * 保存新的报名申请记录。
     *
     * @param enroll
     *            报名实体。
     */
    void save(Enroll enroll);

    /**
     * 更新已有报名申请记录。
     *
     * @param enroll
     *            报名实体（id 必须非空）。
     */
    void update(Enroll enroll);

    /**
     * 查询全部报名申请记录。
     *
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名实体。
     */
    Page<Enroll> findAll(Pageable pageable);

    /**
     * 按业务状态查询报名申请记录。
     *
     * @param status
     *            业务状态过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名实体。
     */
    Page<Enroll> findByStatus(EnrollStatus status, Pageable pageable);

    /**
     * 按技术方向查询报名申请记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名实体。
     */
    Page<Enroll> findByDirection(Direction direction, Pageable pageable);

    /**
     * 按报名状态和技术方向查询报名申请。
     *
     * @param status
     *            业务状态过滤条件。
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名实体。
     */
    Page<Enroll> findByStatusAndDirection(EnrollStatus status, Direction direction, Pageable pageable);

    /**
     * 按关键字搜索报名申请记录。
     *
     * @param keyword
     *            搜索关键字。
     * @param status
     *            业务状态过滤条件。
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的报名实体。
     */
    Page<Enroll> search(String keyword, EnrollStatus status, Direction direction, Pageable pageable);

    /**
     * 汇总报名申请相关的状态和方向统计数据。
     *
     * @return 统计数据。
     */
    EnrollStatisticsVO getStatistics();
}
