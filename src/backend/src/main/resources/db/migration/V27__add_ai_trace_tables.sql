-- AI 对话轨迹表
-- 由 ai-service（Python）写入原始执行轨迹，由主 API 服务只读查询
-- 用途：开发团队回看用户提问、检索过程、模型思考与作答依据

-- ============================================
-- 会话表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_ai_conversation (
    id VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_conversation_created_at ON tb_ai_conversation(created_at);
CREATE INDEX IF NOT EXISTS idx_ai_conversation_last_active_at ON tb_ai_conversation(last_active_at);

-- ============================================
-- 提问轨迹表（一行 = 一个用户提问）
-- 注意：消息数 = 该会话下本表行数（只计用户侧提问，不计助手回复）
-- ============================================
CREATE TABLE IF NOT EXISTS tb_ai_turn (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL,
    seq INT NOT NULL,
    user_input TEXT DEFAULT '',
    answer TEXT DEFAULT '',
    prompt JSONB,
    events JSONB NOT NULL DEFAULT '[]'::jsonb,
    degraded BOOLEAN NOT NULL DEFAULT FALSE,
    duration_ms INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_turn_conversation_seq ON tb_ai_turn(conversation_id, seq);
CREATE INDEX IF NOT EXISTS idx_ai_turn_created_at ON tb_ai_turn(created_at);

-- ============================================
-- 表注释
-- ============================================
COMMENT ON TABLE tb_ai_conversation IS 'AI 对话会话表（会话边界由 ai-service 生成并回传前端）';
COMMENT ON COLUMN tb_ai_conversation.id IS '会话 ID，主键，由 ai-service 生成';
COMMENT ON COLUMN tb_ai_conversation.created_at IS '会话创建时间';
COMMENT ON COLUMN tb_ai_conversation.last_active_at IS '会话最后活跃时间，每次新增提问时更新';

COMMENT ON TABLE tb_ai_turn IS 'AI 对话提问轨迹表，一行对应一个用户提问的完整执行轨迹';
COMMENT ON COLUMN tb_ai_turn.id IS '轨迹 ID，主键';
COMMENT ON COLUMN tb_ai_turn.conversation_id IS '所属会话 ID（应用层维护关系，无物理外键）';
COMMENT ON COLUMN tb_ai_turn.seq IS '会话内序号，从 1 开始递增';
COMMENT ON COLUMN tb_ai_turn.user_input IS '用户原始提问文本，不含预披露阶段拼接的检索结果';
COMMENT ON COLUMN tb_ai_turn.answer IS '最终答案，被拦截或断流时可能为空';
COMMENT ON COLUMN tb_ai_turn.prompt IS '图执行结束时组装完成的完整消息列表快照（模型实际收到的上下文）；拒答与直接回复路径为 NULL';
COMMENT ON COLUMN tb_ai_turn.events IS '原始执行事件流，轨迹的唯一真相来源；事件类型包括 intent / pre_disclose / reasoning / tool_call / tool_result / content';
COMMENT ON COLUMN tb_ai_turn.degraded IS '是否为不完整记录（客户端中途断流或采集异常）';
COMMENT ON COLUMN tb_ai_turn.duration_ms IS '本轮提问从开始到结束的耗时（毫秒）';
COMMENT ON COLUMN tb_ai_turn.created_at IS '记录创建时间';
