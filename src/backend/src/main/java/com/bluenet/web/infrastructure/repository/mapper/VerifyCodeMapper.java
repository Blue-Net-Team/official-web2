package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.VerifyCodeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VerifyCodeMapper extends BaseMapper<VerifyCodeDO> {
    /**
     * 按接收目标和验证码查询最新验证码数据行。
     *
     * @param target
     *            验证码发送目标，例如邮箱。
     * @param code
     *            验证码或推荐码。
     * @return 匹配条件的验证码 数据行；不存在时为 null。
     */
    VerifyCodeDO selectLatestByTargetAndCode(@Param("target") String target, @Param("code") String code);

    /**
     * 按接收目标、验证码和场景查询最新验证码数据行。
     *
     * @param target
     *            验证码发送目标，例如邮箱。
     * @param code
     *            验证码或推荐码。
     * @param scene
     *            验证码使用场景。
     * @return 匹配条件的验证码 数据行；不存在时为 null。
     */
    VerifyCodeDO selectLatestByTargetAndCodeAndScene(@Param("target") String target, @Param("code") String code,
            @Param("scene") String scene);

    /**
     * 将匹配的验证码记录标记为已使用。
     *
     * @param target
     *            验证码发送目标，例如邮箱。
     * @param code
     *            验证码或推荐码。
     * @param usedAt
     *            验证码标记为已使用的时间。
     * @return 数据库受影响行数。
     */
    int markAsUsed(@Param("target") String target, @Param("code") String code,
            @Param("usedAt") java.time.LocalDateTime usedAt);

    /**
     * 将匹配的验证码记录标记为已使用。
     *
     * @param target
     *            验证码发送目标，例如邮箱。
     * @param code
     *            验证码或推荐码。
     * @param scene
     *            验证码使用场景。
     * @param usedAt
     *            验证码标记为已使用的时间。
     * @return 数据库受影响行数。
     */
    int markAsUsedWithScene(@Param("target") String target, @Param("code") String code, @Param("scene") String scene,
            @Param("usedAt") java.time.LocalDateTime usedAt);
}
