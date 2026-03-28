package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.api.dto.member.MemberListQueryDTO;

import java.util.List;

public interface MemberService {
    PageDTO<MemberBriefDTO> getMemberList(MemberListQueryDTO query);
    MemberDetailDTO getMemberById(Long id);
    List<DirectionLeaderDTO> getDirectionLeaders();
    List<ExperienceDTO> getMemberExperiences(Long memberId, String type);
}
