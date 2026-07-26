package com.bluenet.web.api.dto.assessment_judgement;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端题目提交评分行。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "题目提交评分行")
public class AssessmentQuestionSubmissionDTO {
    @Schema(description = "答案ID")
    private Long answerId;
    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;
    @Schema(description = "题号")
    private Integer questionNo;
    @Schema(description = "题目标题")
    private String questionTitle;
    @Schema(description = "题型")
    private QuestionType questionType;
    @Schema(description = "题目满分")
    private BigDecimal maxScore;
    @Schema(description = "考生用户ID")
    private Long candidateUserId;
    @Schema(description = "考生学号")
    private String studentId;
    @Schema(description = "考生姓名")
    private String username;
    @Schema(description = "考生昵称")
    private String nickname;
    @Schema(description = "上传文件ID")
    private Long fileId;
    @Schema(description = "答案内容")
    private String content;
    @Schema(description = "算法题提交语言")
    private ProgrammingLanguage language;
    @Schema(description = "提交时间")
    private LocalDateTime submitTime;
    @Schema(description = "最新评判")
    private AssessmentJudgementDTO latestJudgement;
    @Schema(description = "历史评判记录；算法题用于展开查看所有提交评判，并标记最佳记录")
    private List<AssessmentQuestionSubmissionHistoryDTO> histories;
    @Schema(description = "所属队伍ID")
    private Long teamId;
    @Schema(description = "所属队伍名称")
    private String teamName;
    @Schema(description = "是否为队长")
    private Boolean isLeader;
    @Schema(description = "内推码（报名时填写）")
    private String internalReferralCode;
    @Schema(description = "推荐人姓名（内推码有效时返回）")
    private String referralUserName;
}
