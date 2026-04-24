package com.bluenet.web.application.service;

import com.bluenet.web.application.message.MessageTemplateInfo;

import java.util.List;
import java.util.Map;

/**
 * 消息模板应用服务接口。
 */
public interface MessageTemplateAppService {

    /**
     * 查询所有模板列表。
     *
     * @return 模板元数据列表。
     */
    List<MessageTemplateInfo> listTemplates();

    /**
     * 查询指定模板详情。
     *
     * @param code
     *            模板编码。
     * @return 模板详情。
     */
    MessageTemplateInfo getTemplate(String code);

    /**
     * 更新模板内容。
     *
     * @param code
     *            模板编码。
     * @param subject
     *            新主题（可选，null 表示不修改）。
     * @param content
     *            新内容。
     */
    void updateTemplate(String code, String subject, String content);

    /**
     * 切换模板启禁用状态。
     *
     * @param code
     *            模板编码。
     * @param enabled
     *            是否启用。
     */
    void toggleTemplate(String code, boolean enabled);

    /**
     * 预览模板渲染效果。
     *
     * @param code
     *            模板编码。
     * @param variables
     *            测试变量值。
     * @return 渲染后的 HTML。
     */
    String previewTemplate(String code, Map<String, String> variables);
}
