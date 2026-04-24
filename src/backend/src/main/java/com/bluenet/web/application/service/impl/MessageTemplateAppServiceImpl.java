package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.message.MessageTemplateInfo;
import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.application.service.MessageTemplateAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消息模板应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class MessageTemplateAppServiceImpl implements MessageTemplateAppService {

    private final MessageTemplateRegistry templateRegistry;

    @Override
    public List<MessageTemplateInfo> listTemplates() {
        return templateRegistry.listAll();
    }

    @Override
    public MessageTemplateInfo getTemplate(String code) {
        return templateRegistry.findByCode(code)
                .orElseThrow(() -> new DataNotFound("模板不存在: " + code));
    }

    @Override
    public void updateTemplate(String code, String subject, String content) {
        if (content != null && !content.isBlank()) {
            templateRegistry.updateContent(code, content);
        }
        // subject 暂存，当前内存实现中主题与模板元数据绑定，后续可扩展
    }

    @Override
    public void toggleTemplate(String code, boolean enabled) {
        templateRegistry.setEnabled(code, enabled);
    }

    @Override
    public String previewTemplate(String code, Map<String, String> variables) {
        return templateRegistry.preview(code, variables);
    }
}
