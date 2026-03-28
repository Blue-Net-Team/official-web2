package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.Enroll;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnrollMapper extends BaseMapper<Enroll> {
    Enroll selectByStudentId(String studentId);
}
