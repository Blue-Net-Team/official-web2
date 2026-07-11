package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.vo.question_content.FileUploadContent;
import com.bluenet.web.domain.model.vo.question_content.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.question_content.QuestionContent;
import com.bluenet.web.domain.model.vo.question_content.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考核模块测试夹具，解决构造一次合法考核需要多张表的痛点。
 */
public final class AssessmentFixture {

    private static final String DEFAULT_QUESTION_TITLE = "测试题目";
    private static final BigDecimal DEFAULT_SCORE = new BigDecimal("100");
    private static final String DEFAULT_TEAM_NAME = "测试队伍";

    private AssessmentFixture() {
    }

    public static TimeBuilder timeBuilder() {
        return new TimeBuilder();
    }

    public static QuestionBuilder questionBuilder() {
        return new QuestionBuilder();
    }

    public static SessionBuilder sessionBuilder() {
        return new SessionBuilder();
    }

    public static TeamBuilder teamBuilder() {
        return new TeamBuilder();
    }

    public static AnswerBuilder answerBuilder() {
        return new AnswerBuilder();
    }

    public static JudgementBuilder judgementBuilder() {
        return new JudgementBuilder();
    }

    public static DecisionBuilder decisionBuilder() {
        return new DecisionBuilder();
    }

    public static final class TimeBuilder {

        private Direction direction = Direction.COMPUTER_VISION;
        private Integer epoch = 1;
        private Integer grade = 2024;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean timeLimit = false;
        private Integer timeLimitMinutes;
        private Boolean allowTeam = false;

        private TimeBuilder() {
            LocalDateTime[] window = TimeFixture.withinNow();
            this.startTime = window[0];
            this.endTime = window[1];
        }

        public TimeBuilder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public TimeBuilder epoch(Integer epoch) {
            this.epoch = epoch;
            return this;
        }

        public TimeBuilder grade(Integer grade) {
            this.grade = grade;
            return this;
        }

        public TimeBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public TimeBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public TimeBuilder withinNow() {
            LocalDateTime[] window = TimeFixture.withinNow();
            this.startTime = window[0];
            this.endTime = window[1];
            return this;
        }

        public TimeBuilder ended() {
            this.startTime = TimeFixture.minusMinutes(60);
            this.endTime = TimeFixture.minusMinutes(5);
            return this;
        }

        public TimeBuilder notStarted() {
            this.startTime = TimeFixture.plusMinutes(10);
            this.endTime = TimeFixture.plusMinutes(70);
            return this;
        }

        public TimeBuilder timeLimit(int minutes) {
            this.timeLimit = true;
            this.timeLimitMinutes = minutes;
            return this;
        }

        public TimeBuilder allowTeam() {
            this.allowTeam = true;
            return this;
        }

        public AssessmentTime build() {
            return AssessmentTime
                    .create(direction, epoch, grade, startTime, endTime, timeLimit, timeLimitMinutes, allowTeam);
        }

        public AssessmentTime save(AssessmentTimeRepository repository) {
            AssessmentTime time = build();
            repository.save(time);
            return time;
        }
    }

    public static final class QuestionBuilder {

        private Long assessmentTimeId;
        private Integer questionNo = 1;
        private QuestionType questionType = QuestionType.FILE_UPLOAD;
        private String title = DEFAULT_QUESTION_TITLE;
        private QuestionContent content = new FileUploadContent();
        private Long attachmentId;
        private BigDecimal score = DEFAULT_SCORE;

        public QuestionBuilder assessmentTimeId(Long assessmentTimeId) {
            this.assessmentTimeId = assessmentTimeId;
            return this;
        }

        public QuestionBuilder questionNo(Integer questionNo) {
            this.questionNo = questionNo;
            return this;
        }

        public QuestionBuilder type(QuestionType questionType) {
            this.questionType = questionType;
            if (questionType == QuestionType.FILE_UPLOAD && !(content instanceof FileUploadContent)) {
                this.content = new FileUploadContent();
            }
            return this;
        }

        public QuestionBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QuestionBuilder content(QuestionContent content) {
            this.content = content;
            return this;
        }

        public QuestionBuilder singleChoice(String correctAnswer, String... options) {
            this.questionType = QuestionType.SINGLE_CHOICE;
            SingleChoiceContent singleChoiceContent = new SingleChoiceContent();
            singleChoiceContent.setOptions(List.of(options));
            singleChoiceContent.setCorrectAnswer(correctAnswer);
            this.content = singleChoiceContent;
            return this;
        }

        public QuestionBuilder multipleChoice(List<String> correctAnswers, String... options) {
            this.questionType = QuestionType.MULTIPLE_CHOICE;
            MultipleChoiceContent multipleChoiceContent = new MultipleChoiceContent();
            multipleChoiceContent.setOptions(List.of(options));
            multipleChoiceContent.setCorrectAnswers(correctAnswers);
            this.content = multipleChoiceContent;
            return this;
        }

        public QuestionBuilder fileUpload() {
            this.questionType = QuestionType.FILE_UPLOAD;
            this.content = new FileUploadContent();
            return this;
        }

        public QuestionBuilder algorithm() {
            this.questionType = QuestionType.ALGORITHM;
            this.content = new FileUploadContent();
            return this;
        }

        public QuestionBuilder score(BigDecimal score) {
            this.score = score;
            return this;
        }

        public QuestionBuilder attachmentId(Long attachmentId) {
            this.attachmentId = attachmentId;
            return this;
        }

        public AssessmentQuestion build() {
            return AssessmentQuestion
                    .create(assessmentTimeId, questionNo, questionType, title, content, attachmentId, score);
        }

        public AssessmentQuestion save(AssessmentQuestionRepository repository) {
            AssessmentQuestion question = build();
            repository.save(question);
            return question;
        }
    }

    public static final class SessionBuilder {

        private Long userId;
        private Long assessmentTimeId;
        private LocalDateTime startTime = TimeFixture.now();
        private LocalDateTime deadline = TimeFixture.plusMinutes(60);

        public SessionBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public SessionBuilder user(User user) {
            this.userId = user.getId();
            return this;
        }

        public SessionBuilder assessmentTimeId(Long assessmentTimeId) {
            this.assessmentTimeId = assessmentTimeId;
            return this;
        }

        public SessionBuilder assessmentTime(AssessmentTime assessmentTime) {
            this.assessmentTimeId = assessmentTime.getId();
            return this;
        }

        public SessionBuilder deadline(LocalDateTime deadline) {
            this.deadline = deadline;
            return this;
        }

        public AssessmentSession build() {
            return AssessmentSession.create(userId, assessmentTimeId, startTime, deadline);
        }

        public AssessmentSession save(AssessmentSessionRepository repository) {
            AssessmentSession session = build();
            repository.save(session);
            return session;
        }
    }

    public static final class TeamBuilder {

        private Long assessmentTimeId;
        private Long leaderId;
        private String name = DEFAULT_TEAM_NAME;
        private String inviteCode = "INVITE";

        public TeamBuilder assessmentTimeId(Long assessmentTimeId) {
            this.assessmentTimeId = assessmentTimeId;
            return this;
        }

        public TeamBuilder assessmentTime(AssessmentTime assessmentTime) {
            this.assessmentTimeId = assessmentTime.getId();
            return this;
        }

        public TeamBuilder leader(User user) {
            this.leaderId = user.getId();
            return this;
        }

        public TeamBuilder leaderId(Long leaderId) {
            this.leaderId = leaderId;
            return this;
        }

        public TeamBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamBuilder inviteCode(String inviteCode) {
            this.inviteCode = inviteCode;
            return this;
        }

        public AssessmentTeam build() {
            return AssessmentTeam.create(assessmentTimeId, leaderId, name, inviteCode);
        }

        public AssessmentTeam save(AssessmentTeamRepository repository) {
            AssessmentTeam team = build();
            repository.save(team);
            repository.addMember(team.getId(), team.getLeaderId());
            return team;
        }
    }

    public static final class AnswerBuilder {

        private Long userId;
        private Long questionId;
        private String content = "测试答案";
        private ProgrammingLanguage language = ProgrammingLanguage.CPP;
        private Long fileId;
        private Long teamId;

        public AnswerBuilder user(User user) {
            this.userId = user.getId();
            return this;
        }

        public AnswerBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AnswerBuilder question(AssessmentQuestion question) {
            this.questionId = question.getId();
            return this;
        }

        public AnswerBuilder questionId(Long questionId) {
            this.questionId = questionId;
            return this;
        }

        public AnswerBuilder content(String content) {
            this.content = content;
            return this;
        }

        public AnswerBuilder language(ProgrammingLanguage language) {
            this.language = language;
            return this;
        }

        public AnswerBuilder file(File file) {
            this.fileId = file.getId();
            return this;
        }

        public AnswerBuilder fileId(Long fileId) {
            this.fileId = fileId;
            return this;
        }

        public AnswerBuilder team(AssessmentTeam team) {
            this.teamId = team.getId();
            return this;
        }

        public AnswerBuilder teamId(Long teamId) {
            this.teamId = teamId;
            return this;
        }

        public AssessmentAnswer build() {
            return AssessmentAnswer.create(userId, questionId, content, language, fileId, teamId);
        }

        public AssessmentAnswer save(AssessmentAnswerRepository repository) {
            AssessmentAnswer answer = build();
            repository.save(answer);
            return answer;
        }
    }

    public static final class JudgementBuilder {

        private Long answerId;
        private Long questionId;
        private Long assessmentTimeId;
        private Long userId;
        private BigDecimal score = DEFAULT_SCORE;
        private BigDecimal maxScore = DEFAULT_SCORE;
        private JudgementStatus status = JudgementStatus.JUDGED;
        private ObjectiveResultCode resultCode = ObjectiveResultCode.AC;
        private JudgementSource source = JudgementSource.AUTO;
        private Long reviewerId;
        private ReviewerType reviewerType = ReviewerType.SYSTEM;
        private LocalDateTime judgedAt = TimeFixture.now();

        public JudgementBuilder answer(AssessmentAnswer answer) {
            this.answerId = answer.getId();
            this.userId = answer.getUserId();
            return this;
        }

        public JudgementBuilder question(AssessmentQuestion question) {
            this.questionId = question.getId();
            this.assessmentTimeId = question.getAssessmentTimeId();
            return this;
        }

        public JudgementBuilder score(BigDecimal score, BigDecimal maxScore) {
            this.score = score;
            this.maxScore = maxScore;
            return this;
        }

        public JudgementBuilder source(JudgementSource source) {
            this.source = source;
            return this;
        }

        public JudgementBuilder reviewer(User user) {
            this.reviewerId = user.getId();
            return this;
        }

        public JudgementBuilder status(JudgementStatus status) {
            this.status = status;
            return this;
        }

        public AssessmentJudgement build() {
            return AssessmentJudgement.create(
                    answerId,
                    questionId,
                    assessmentTimeId,
                    userId,
                    score,
                    maxScore,
                    status,
                    resultCode,
                    source,
                    reviewerId,
                    reviewerType,
                    judgedAt);
        }

        public AssessmentJudgement save(AssessmentJudgementRepository repository) {
            AssessmentJudgement judgement = build();
            repository.save(judgement);
            return judgement;
        }
    }

    public static final class DecisionBuilder {

        private Long userId;
        private Long assessmentTimeId;
        private Boolean passed = true;
        private Long decidedBy;
        private String decisionComment = "测试决策";

        public DecisionBuilder user(User user) {
            this.userId = user.getId();
            return this;
        }

        public DecisionBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public DecisionBuilder assessmentTime(AssessmentTime assessmentTime) {
            this.assessmentTimeId = assessmentTime.getId();
            return this;
        }

        public DecisionBuilder assessmentTimeId(Long assessmentTimeId) {
            this.assessmentTimeId = assessmentTimeId;
            return this;
        }

        public DecisionBuilder passed(Boolean passed) {
            this.passed = passed;
            return this;
        }

        public DecisionBuilder decidedBy(User admin) {
            this.decidedBy = admin.getId();
            return this;
        }

        public DecisionBuilder decisionComment(String comment) {
            this.decisionComment = comment;
            return this;
        }

        public AssessmentDecision build() {
            return AssessmentDecision.create(userId, assessmentTimeId, passed, decidedBy, decisionComment);
        }

        public AssessmentDecision save(AssessmentDecisionRepository repository) {
            AssessmentDecision decision = build();
            repository.save(decision);
            return decision;
        }
    }
}
