package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户测试夹具，封装 {@link User#create} 的 18 个参数。
 */
public final class UserFixture {

    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_MAJOR = "计算机科学与技术";
    private static final Integer DEFAULT_GRADE_YEAR = 2024;
    private static final Direction DEFAULT_DIRECTION = Direction.COMPUTER_VISION;
    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final String DEFAULT_JOB = "开发";
    private static final String DEFAULT_BIO = "个人简介";

    private UserFixture() {
    }

    /**
     * 创建成员用户 Builder。
     */
    public static Builder member(String studentId) {
        return builder()
                .withStudentId(studentId)
                .withRoleType(RoleType.MEMBER);
    }

    /**
     * 创建考生用户 Builder。
     */
    public static Builder candidate(String studentId) {
        return builder()
                .withStudentId(studentId)
                .withRoleType(RoleType.CANDIDATE);
    }

    /**
     * 创建方向管理员 Builder。
     */
    public static Builder directionAdmin(String studentId, Direction direction) {
        return builder()
                .withStudentId(studentId)
                .withRoleType(RoleType.DIRECTION_ADMIN)
                .withDirection(direction);
    }

    /**
     * 创建超级管理员 Builder。
     */
    public static Builder superAdmin(String studentId) {
        return builder()
                .withStudentId(studentId)
                .withRoleType(RoleType.SUPER_ADMIN);
    }

    /**
     * 通用 Builder 入口。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 保存用户，若密码未编码则自动编码。
     */
    public static User save(UserRepository userRepository, PasswordEncoder passwordEncoder, User user) {
        String password = user.getPassword();
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
        return user;
    }

    public static final class Builder {

        private String studentId = "2024001001";
        private String email;
        private Long roleId;
        private String password = DEFAULT_PASSWORD;
        private String username;
        private String nickname;
        private Long collegeId;
        private String major = DEFAULT_MAJOR;
        private Integer assessmentGradeYear = DEFAULT_GRADE_YEAR;
        private Direction direction = DEFAULT_DIRECTION;
        private Gender gender = DEFAULT_GENDER;
        private String job = DEFAULT_JOB;
        private Long avatarId;
        private Long qrcodeId;
        private String githubId;
        private String githubUsername;
        private String internalReferralCode;
        private String bio = DEFAULT_BIO;
        private Boolean disable = false;

        private Builder() {
        }

        public Builder withStudentId(String studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withRoleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder withRoleType(RoleType roleType) {
            this.roleId = RoleFixture.defaultRoleId(roleType);
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder withCollege(College college) {
            this.collegeId = college.getId();
            return this;
        }

        public Builder withCollegeId(Long collegeId) {
            this.collegeId = collegeId;
            return this;
        }

        public Builder withMajor(String major) {
            this.major = major;
            return this;
        }

        public Builder withAssessmentGradeYear(Integer year) {
            this.assessmentGradeYear = year;
            return this;
        }

        public Builder withDirection(Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder withGender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder withJob(String job) {
            this.job = job;
            return this;
        }

        public Builder withAvatarId(Long avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public Builder withQrcodeId(Long qrcodeId) {
            this.qrcodeId = qrcodeId;
            return this;
        }

        public Builder disabled() {
            this.disable = true;
            return this;
        }

        public Builder withGithubId(String githubId) {
            this.githubId = githubId;
            return this;
        }

        public Builder withInternalReferralCode(String code) {
            this.internalReferralCode = code;
            return this;
        }

        public Builder withBio(String bio) {
            this.bio = bio;
            return this;
        }

        public User build() {
            if (email == null) {
                email = studentId + "@example.com";
            }
            if (username == null) {
                username = "用户" + studentId;
            }
            if (nickname == null) {
                nickname = "昵称" + studentId;
            }
            if (internalReferralCode == null) {
                String suffix = studentId.length() > 5 ? studentId.substring(studentId.length() - 5) : studentId;
                internalReferralCode = "REF" + suffix;
            }
            User user = User.create(
                    studentId,
                    email,
                    roleId,
                    password,
                    username,
                    nickname,
                    collegeId,
                    major,
                    assessmentGradeYear,
                    direction,
                    gender,
                    job,
                    avatarId,
                    qrcodeId,
                    githubId,
                    githubUsername,
                    internalReferralCode,
                    bio);
            user.setDisable(disable);
            return user;
        }

        public User save(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            return UserFixture.save(userRepository, passwordEncoder, build());
        }
    }
}
