-- 为所有数据库表、列和索引添加中文注释
-- 创建时间: 2026-02-16
-- 遵循开发手册《官网功能与开发手册.md》的表设计定义
-- 说明：本项目中所有表均无软删除(deleted)字段；用户表 tb_user.disable 为账号封禁标识，非软删除；
-- 所有表均无数据库层外键约束，关联关系由应用层维护。注释仅对实际存在的列/索引添加。

-- ============================================
-- 1. 角色权限相关表注释
-- ============================================

-- 1.1 tb_role 角色表
COMMENT ON TABLE tb_role IS '角色表，定义系统中的用户角色，如超级管理员、方向管理员、团队成员、考生等';
COMMENT ON COLUMN tb_role.id IS '角色ID，主键，自增';
COMMENT ON COLUMN tb_role.name IS '角色唯一标识，如 SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, CANDIDATE';

-- 1.2 tb_permission 权限表
COMMENT ON TABLE tb_permission IS '权限表，定义系统中的操作权限，与角色进行多对多关联';
COMMENT ON COLUMN tb_permission.id IS '权限ID，主键，自增';
COMMENT ON COLUMN tb_permission.name IS '权限名称，用于外部展示，如"用户管理"';
COMMENT ON COLUMN tb_permission.value IS '权限值，权限的唯一标识，如 user:create, user:read';
COMMENT ON COLUMN tb_permission.url IS '操作的URL路径，用于权限匹配';
COMMENT ON COLUMN tb_permission.method IS '操作URL的请求方法，如 GET, POST, PUT, DELETE';

-- 1.3 tb_role_permission 角色权限关联表
COMMENT ON TABLE tb_role_permission IS '角色权限关联表，实现角色和权限的多对多关系';
COMMENT ON COLUMN tb_role_permission.id IS '关联ID，主键，自增';
COMMENT ON COLUMN tb_role_permission.role_id IS '角色ID，关联 tb_role.id';
COMMENT ON COLUMN tb_role_permission.permission_id IS '权限ID，关联 tb_permission.id';

-- 1.4 角色权限相关表索引注释
COMMENT ON INDEX idx_user_role_id IS '优化按角色查询用户的性能';

-- ============================================
-- 2. 用户管理相关表注释
-- ============================================

-- 2.1 tb_college 学院表
COMMENT ON TABLE tb_college IS '学院表，存储学校各学院信息';
COMMENT ON COLUMN tb_college.id IS '学院ID，主键，自增';
COMMENT ON COLUMN tb_college.name IS '学院名称，如"计算机学院"';

-- 2.2 tb_user 用户表
COMMENT ON TABLE tb_user IS '用户表，存储系统用户信息，包括团队成员和考生';
COMMENT ON COLUMN tb_user.id IS '用户ID，主键，自增';
COMMENT ON COLUMN tb_user.student_id IS '学号，12-13位数字，用户唯一凭证';
COMMENT ON COLUMN tb_user.email IS '用户邮箱，用于通知和找回密码';
COMMENT ON COLUMN tb_user.role_id IS '角色ID，关联 tb_role.id';
COMMENT ON COLUMN tb_user.password IS '密码哈希值，使用 bcrypt 等算法加密';
COMMENT ON COLUMN tb_user.username IS '用户真实姓名';
COMMENT ON COLUMN tb_user.nickname IS '用户昵称，用于展示';
COMMENT ON COLUMN tb_user.college_id IS '学院ID，关联 tb_college.id';
COMMENT ON COLUMN tb_user.major IS '专业，如"软件工程"';
COMMENT ON COLUMN tb_user.direction IS '方向，枚举值：计算机视觉、结构设计、嵌入式开发';
COMMENT ON COLUMN tb_user.gender IS '性别，枚举值：male(男), female(女), unknown(未知)';
COMMENT ON COLUMN tb_user.job IS '细化职责，如"后端开发"、"前端开发"';
COMMENT ON COLUMN tb_user.avatar_id IS '头像文件ID，关联 tb_file.id';
COMMENT ON COLUMN tb_user.disable IS '账号封禁标识，true表示已封禁';
COMMENT ON COLUMN tb_user.wechat_qrcode IS '微信二维码图片URL';
COMMENT ON COLUMN tb_user.github_id IS 'GitHub用户唯一标识，用于OAuth2登录绑定';
COMMENT ON COLUMN tb_user.github_username IS 'GitHub用户名';

-- 2.3 tb_user_experience 用户经历表
COMMENT ON TABLE tb_user_experience IS '用户经历表，存储用户的项目、竞赛、实习等经历';
COMMENT ON COLUMN tb_user_experience.id IS '经历ID，主键，自增';
COMMENT ON COLUMN tb_user_experience.user_id IS '用户ID，关联 tb_user.id';
COMMENT ON COLUMN tb_user_experience.type IS '经历类型，枚举值：竞赛、项目、实习';
COMMENT ON COLUMN tb_user_experience.title IS '经历或项目标题';
COMMENT ON COLUMN tb_user_experience.content IS '经历或项目详细内容';
COMMENT ON COLUMN tb_user_experience.start_time IS '经历开始时间';
COMMENT ON COLUMN tb_user_experience.end_time IS '经历结束时间';

-- 2.4 用户管理相关表索引注释
COMMENT ON INDEX idx_user_student_id IS '优化按学号查询用户的性能，学号是登录凭证';
COMMENT ON INDEX idx_user_email IS '优化按邮箱查询用户的性能';
COMMENT ON INDEX idx_user_college_id IS '优化按学院查询用户的性能';
COMMENT ON INDEX idx_user_direction IS '优化按方向查询用户的性能';
COMMENT ON INDEX idx_user_experience_user_id IS '优化查询指定用户所有经历的性能';

-- ============================================
-- 3. 成就相关表注释
-- ============================================

-- 3.1 tb_achievement 成就表
COMMENT ON TABLE tb_achievement IS '成就表，存储团队获得的竞赛奖项、论文、专利等成就';
COMMENT ON COLUMN tb_achievement.id IS '成就ID，主键，自增';
COMMENT ON COLUMN tb_achievement.title IS '成就标题，如"2024年全国大学生电子设计竞赛三等奖"';
COMMENT ON COLUMN tb_achievement.type IS '成就类型，枚举值：论文、专利、竞赛';
COMMENT ON COLUMN tb_achievement.relate_to IS '相关的竞赛或期刊名称，如果是专利则填写"专利"';
COMMENT ON COLUMN tb_achievement.achieve_at IS '成就获得年份，如 2024';

-- 3.2 tb_user_achievement 用户成就关联表
COMMENT ON TABLE tb_user_achievement IS '用户成就关联表，实现用户和成就的多对多关系';
COMMENT ON COLUMN tb_user_achievement.id IS '关联ID，主键，自增';
COMMENT ON COLUMN tb_user_achievement.user_id IS '用户ID，关联 tb_user.id';
COMMENT ON COLUMN tb_user_achievement.achievement_id IS '成就ID，关联 tb_achievement.id';

-- 3.3 成就相关表索引注释（无额外索引）

-- ============================================
-- 4. 文件和报名相关表注释
-- ============================================

-- 4.1 tb_file 文件表
COMMENT ON TABLE tb_file IS '文件表，存储文件元信息，实际文件存储在OSS等对象存储中';
COMMENT ON COLUMN tb_file.id IS '文件ID，主键，自增';
COMMENT ON COLUMN tb_file.name IS '文件名，包含扩展名';
COMMENT ON COLUMN tb_file.type IS '文件类型，枚举值：avatar(头像), normal_img(普通图片), evaluation_attachment(考题附件), work(考生作品), qrcode(二维码)';
COMMENT ON COLUMN tb_file.url IS '文件访问URL，指向对象存储地址';

-- 4.2 tb_introduce_image 介绍图片表
COMMENT ON TABLE tb_introduce_image IS '介绍图片表，存储实验室介绍、设备介绍、团队合照等展示图片';
COMMENT ON COLUMN tb_introduce_image.id IS '图片ID，主键，自增';
COMMENT ON COLUMN tb_introduce_image.type IS '图片类型，枚举值：实验室介绍、设备介绍、团队合照、方向介绍、竞赛介绍、专利介绍、论文介绍';
COMMENT ON COLUMN tb_introduce_image.description IS '图片详情说明，如"3D打印机"、"计算机视觉实验室"';

-- 4.3 tb_enroll 报名表
COMMENT ON TABLE tb_enroll IS '报名表，存储外部用户的报名信息，账号发放后转为正式用户';
COMMENT ON COLUMN tb_enroll.id IS '报名ID，主键，自增';
COMMENT ON COLUMN tb_enroll.username IS '真实姓名';
COMMENT ON COLUMN tb_enroll.student_id IS '学号，12-13位数字，报名唯一凭证';
COMMENT ON COLUMN tb_enroll.password IS '密码，账号发放时使用';
COMMENT ON COLUMN tb_enroll.internal_referral_code IS '内推码，用于内部推荐';
COMMENT ON COLUMN tb_enroll.college_id IS '学院ID，关联 tb_college.id';
COMMENT ON COLUMN tb_enroll.major IS '专业';
COMMENT ON COLUMN tb_enroll.grade IS '年级，如 2023';
COMMENT ON COLUMN tb_enroll.direction IS '报名方向，枚举值：计算机视觉、结构设计、嵌入式开发';
COMMENT ON COLUMN tb_enroll.avatar_id IS '头像文件ID，关联 tb_file.id';
COMMENT ON COLUMN tb_enroll.status IS '报名状态，枚举值：pending(待审核)、approved(已通过)、rejected(已拒绝)';

-- 4.4 文件和报名相关表索引注释
COMMENT ON INDEX idx_enroll_student_id IS '优化按学号查询报名信息的性能';
COMMENT ON INDEX idx_enroll_college_id IS '优化按学院查询报名信息的性能';
COMMENT ON INDEX idx_enroll_direction IS '优化按方向查询报名信息的性能';
COMMENT ON INDEX idx_enroll_status IS '优化按状态查询报名信息的性能';

-- ============================================
-- 5. 验证码和消息模板表注释
-- ============================================

-- 5.1 tb_verify_code 验证码表
COMMENT ON TABLE tb_verify_code IS '验证码表，存储邮箱验证码信息，用于登录验证和密码重置';
COMMENT ON COLUMN tb_verify_code.id IS '验证码ID，主键，自增';
COMMENT ON COLUMN tb_verify_code.target IS '目标标识，即邮箱地址';
COMMENT ON COLUMN tb_verify_code.code IS '验证码，通常为6位数字';
COMMENT ON COLUMN tb_verify_code.expire_at IS '验证码过期时间，通常5分钟后过期';
COMMENT ON COLUMN tb_verify_code.used_at IS '验证码使用时间，使用后记录';
COMMENT ON COLUMN tb_verify_code.ip_address IS '请求IP地址，用于限流和安全审计';

-- 5.2 tb_message_template 消息模板表
COMMENT ON TABLE tb_message_template IS '消息模板表，存储邮件、短信等消息模板，支持变量替换';
COMMENT ON COLUMN tb_message_template.id IS '模板ID，主键，自增';
COMMENT ON COLUMN tb_message_template.code IS '模板编码，唯一标识，如 EMAIL_VERIFY_CODE, EVALUATION_RESULT';
COMMENT ON COLUMN tb_message_template.name IS '模板名称，用于展示';
COMMENT ON COLUMN tb_message_template.subject IS '邮件主题或消息标题';
COMMENT ON COLUMN tb_message_template.content IS '模板内容，支持变量替换，如 {{username}}, {{code}}, {{result}}';
COMMENT ON COLUMN tb_message_template.description IS '模板用途说明';
COMMENT ON COLUMN tb_message_template.enabled IS '是否启用，true表示启用';

-- 5.3 验证码和消息模板表索引注释
COMMENT ON INDEX idx_verify_code_target IS '优化按邮箱查询验证码的性能';
COMMENT ON INDEX idx_verify_code_expire_at IS '优化清理过期验证码的性能';

-- ============================================
-- 6. 考核系统相关表注释
-- ============================================

-- 6.1 tb_evaluation_time 考核时间表
COMMENT ON TABLE tb_evaluation_time IS '考核时间表，定义各方向各轮次的考核时间范围和限时规则';
COMMENT ON COLUMN tb_evaluation_time.id IS '考核时间ID，主键，自增';
COMMENT ON COLUMN tb_evaluation_time.direction IS '考核方向，枚举值：计算机视觉、结构设计、嵌入式开发';
COMMENT ON COLUMN tb_evaluation_time.epoch IS '考核轮次，0表示最终考核，1、2、3...表示第1、2、3...轮考核';
COMMENT ON COLUMN tb_evaluation_time.start_time IS '考核开始时间';
COMMENT ON COLUMN tb_evaluation_time.end_time IS '考核结束时间';
COMMENT ON COLUMN tb_evaluation_time.time_limit IS '是否限时，true表示限时考核';
COMMENT ON COLUMN tb_evaluation_time.time_limit_minutes IS '限时分钟数，time_limit为true时有效';

-- 6.2 tb_evaluation_question 考核题目表
COMMENT ON TABLE tb_evaluation_question IS '考核题目表，存储各轮次考核的具体题目';
COMMENT ON COLUMN tb_evaluation_question.id IS '题目ID，主键，自增';
COMMENT ON COLUMN tb_evaluation_question.evaluation_time_id IS '所属考核时间ID，关联 tb_evaluation_time.id';
COMMENT ON COLUMN tb_evaluation_question.question_no IS '题目序号，在同一考核时间下唯一';
COMMENT ON COLUMN tb_evaluation_question.question_type IS '题目类型，枚举值：SINGLE_CHOICE(单选), MULTIPLE_CHOICE(多选), FILE_UPLOAD(文件上传), ALGORITHM(算法题)';
COMMENT ON COLUMN tb_evaluation_question.title IS '考题标题';
COMMENT ON COLUMN tb_evaluation_question.content IS '题目内容，JSON格式，包含选项、正确答案、算法用例等';
COMMENT ON COLUMN tb_evaluation_question.attachment_id IS '附件文件ID，关联 tb_file.id，用于上传题附件';
COMMENT ON COLUMN tb_evaluation_question.score IS '题目满分分值';

-- 6.3 tb_evaluation_answer 考核答案表
COMMENT ON TABLE tb_evaluation_answer IS '考核答案表，存储考生的答题内容和评分';
COMMENT ON COLUMN tb_evaluation_answer.id IS '答案ID，主键，自增';
COMMENT ON COLUMN tb_evaluation_answer.user_id IS '考生用户ID，关联 tb_user.id';
COMMENT ON COLUMN tb_evaluation_answer.question_id IS '题目ID，关联 tb_evaluation_question.id';
COMMENT ON COLUMN tb_evaluation_answer.content IS '答案内容，文本答案或算法代码';
COMMENT ON COLUMN tb_evaluation_answer.language IS '编程语言，算法题时使用，如 python, c, cpp, java, js';
COMMENT ON COLUMN tb_evaluation_answer.file_id IS '文件答案ID，应用层关联 tb_file，用于文件上传题';
COMMENT ON COLUMN tb_evaluation_answer.submit_time IS '提交时间，默认为当前时间';

-- 6.4 tb_comment 评论表
COMMENT ON TABLE tb_comment IS '评论表，存储团队成员对考生答案的评论和评分';
COMMENT ON COLUMN tb_comment.id IS '评论ID，主键，自增';
COMMENT ON COLUMN tb_comment.answer_id IS '关联的答案ID，关联 tb_evaluation_answer.id';
COMMENT ON COLUMN tb_comment.user_id IS '评论者ID，关联 tb_user.id，必须是团队成员';
COMMENT ON COLUMN tb_comment.content IS '评论内容';
COMMENT ON COLUMN tb_comment.score IS '评分，与评论合并在一起';
COMMENT ON COLUMN tb_comment.comment_time IS '评论时间，默认为当前时间';

-- 6.5 考核系统相关表索引注释
COMMENT ON INDEX idx_eval_time_direction IS '优化按方向查询考核时间的性能';
COMMENT ON INDEX idx_eval_time_epoch IS '优化按轮次查询考核时间的性能';
COMMENT ON INDEX idx_eval_question_time_id IS '优化查询指定考核时间下所有题目的性能';
COMMENT ON INDEX idx_eval_question_type IS '优化按题目类型查询的性能';
COMMENT ON INDEX idx_eval_answer_user_id IS '优化查询指定用户所有答案的性能';
COMMENT ON INDEX idx_eval_answer_question_id IS '优化查询指定题目所有答案的性能';
COMMENT ON INDEX idx_comment_answer_id IS '优化查询指定答案所有评论的性能';
COMMENT ON INDEX idx_comment_user_id IS '优化查询指定用户所有评论的性能';

-- ============================================
-- 7. 审计日志表注释
-- ============================================

-- 7.1 tb_audit 审计日志表
COMMENT ON TABLE tb_audit IS '审计日志表，记录所有操作便于安全审计和问题追踪';
COMMENT ON COLUMN tb_audit.id IS '日志ID，主键，自增';
COMMENT ON COLUMN tb_audit.action IS '应用层函数名，标识执行的操作';
COMMENT ON COLUMN tb_audit.action_arg IS '应用层传参，JSON格式，限制4000字符';
COMMENT ON COLUMN tb_audit.action_user_id IS '操作人ID，未登录用户为null，关联 tb_user.id';
COMMENT ON COLUMN tb_audit.action_time IS '操作时间，默认为当前时间';
COMMENT ON COLUMN tb_audit.ip_address IS '客户端IP地址';
COMMENT ON COLUMN tb_audit.user_agent IS '客户端User-Agent';
COMMENT ON COLUMN tb_audit.remarks IS '备注信息';
COMMENT ON COLUMN tb_audit.success_state IS '操作状态，true表示成功，false表示失败';

-- 7.2 审计日志表索引注释
COMMENT ON INDEX idx_audit_action_time IS '优化按时间查询审计日志的性能，用于按时间段审计';
COMMENT ON INDEX idx_audit_action_user_id IS '优化按操作人查询审计日志的性能';
