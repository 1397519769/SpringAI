package com.example.springai.entity.vo;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;

/**
 * 消息视图对象，将 Spring AI 的 Message 转换为简单的角色+内容格式返回给前端。
 */
@Data
public class MessageVo {
    private String role;
    private String content;

    public MessageVo(Message message) {
        switch (message.getMessageType()) {
            case USER:
                role = "user";
                break;
            case ASSISTANT:
                role = "assistant";
                break;
            default:
                role = "";
        }
        this.content = message.getText();
    }
}
