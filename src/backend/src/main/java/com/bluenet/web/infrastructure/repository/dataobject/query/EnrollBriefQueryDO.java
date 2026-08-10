package com.bluenet.web.infrastructure.repository.dataobject.query;

import com.bluenet.web.infrastructure.repository.dataobject.EnrollDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报名列表查询数据对象，仅用于承接 XML 查询结果。
 * <p>
 * 在 EnrollDO 表字段之上附加推荐人姓名投影，供管理端报名列表展示内推信息。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnrollBriefQueryDO extends EnrollDO {
    /**
     * 推荐人用户名，由推荐码 JOIN tb_user 反查得到；无推荐码或推荐码无效时为 null。
     */
    private String referralUsername;
}
