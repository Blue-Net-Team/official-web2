package com.bluenet.web.api.converter.college;

import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.application.command.college.CollegeCommands;
import org.springframework.stereotype.Component;

/**
 * 学院请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class CollegeRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public CollegeCommands.CreateCollegeCommand toCommand(CreateCollegeRequestDTO dto) {
        return new CollegeCommands.CreateCollegeCommand(dto.getName());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public CollegeCommands.UpdateCollegeCommand toCommand(Long id, UpdateCollegeRequestDTO dto) {
        return new CollegeCommands.UpdateCollegeCommand(id, dto.getName());
    }
}
