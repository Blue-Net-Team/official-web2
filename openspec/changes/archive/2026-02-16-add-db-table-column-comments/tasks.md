## 1. 角色权限相关表注释

- [x] 1.1 为 tb_role 表添加表注释和列注释（id, name）
- [x] 1.2 为 tb_permission 表添加表注释和列注释（id, name, value, url, method）
- [x] 1.3 为 tb_role_permission 表添加表注释和列注释（id, role_id, permission_id）
- [x] 1.4 为以上表的索引添加注释

## 2. 用户管理相关表注释

- [x] 2.1 为 tb_college 表添加表注释和列注释（id, name）
- [x] 2.2 为 tb_user 表添加表注释和列注释（id, student_id, email, role_id, password, username, nickname, college_id, major, direction, gender, job, avatar_id, disable, wechat_qrcode, github_id, github_username, deleted）
- [x] 2.3 为 tb_user_experience 表添加表注释和列注释（id, user_id, type, title, content, start_time, end_time, deleted）
- [x] 2.4 为以上表的索引添加注释

## 3. 成就相关表注释

- [x] 3.1 为 tb_achievement 表添加表注释和列注释（id, title, type, relate_to, achieve_at, deleted）
- [x] 3.2 为 tb_user_achievement 表添加表注释和列注释（id, user_id, achievement_id, deleted）
- [x] 3.3 为以上表的索引添加注释

## 4. 文件和报名相关表注释

- [x] 4.1 为 tb_file 表添加表注释和列注释（id, name, type, url, deleted）
- [x] 4.2 为 tb_introduce_image 表添加表注释和列注释（id, type, description, file_id, deleted）
- [x] 4.3 为 tb_enroll 表添加表注释和列注释（id, username, student_id, password, internal_referral_code, college_id, major, grade, direction, avatar_id, status, deleted）
- [x] 4.4 为以上表的索引添加注释

## 5. 验证码和消息模板表注释

- [x] 5.1 为 tb_verify_code 表添加表注释和列注释（id, target, code, expire_at, used_at, ip_address, deleted）
- [x] 5.2 为 tb_message_template 表添加表注释和列注释（id, code, name, type, subject, content, description, enabled, deleted）
- [x] 5.3 为以上表的索引添加注释

## 6. 考核系统相关表注释

- [x] 6.1 为 tb_evaluation_time 表添加表注释和列注释（id, direction, epoch, start_time, end_time, time_limit, time_limit_minutes, deleted）
- [x] 6.2 为 tb_evaluation_question 表添加表注释和列注释（id, evaluation_time_id, question_no, question_type, title, content, attachment_id, score, deleted）
- [x] 6.3 为 tb_evaluation_answer 表添加表注释和列注释（id, user_id, question_id, content, language, file_id, submit_time, final_score, deleted）
- [x] 6.4 为 tb_comment 表添加表注释和列注释（id, answer_id, user_id, content, score, comment_time, deleted）
- [x] 6.5 为以上表的索引添加注释

## 7. 审计日志表注释

- [x] 7.1 为 tb_audit 表添加表注释和列注释（id, action, action_arg, action_user_id, action_time, ip_address, user_agent, remarks, success_state, deleted）
- [x] 7.2 为以上表的索引添加注释

## 8. 测试和验证

- [x] 8.1 在本地环境执行 Flyway 迁移脚本（通过 mvn flyway:migrate 或启动应用）
- [x] 8.2 使用 psql 命令验证所有表注释已正确添加（查询 information_schema 系统表）
- [x] 8.3 使用 psql 命令验证所有列注释已正确添加（查询 information_schema.columns）
- [x] 8.4 使用 psql 命令验证所有索引注释已正确添加（查询 pg_indexes）
- [x] 8.5 重复执行迁移脚本验证幂等性（PostgreSQL COMMENT 语句天然幂等）
