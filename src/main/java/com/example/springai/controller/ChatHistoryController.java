package com.example.springai.controller;

import com.example.springai.entity.vo.MessageVo;
import com.example.springai.repository.ChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 聊天历史查询接口
 * 提供按类型查询会话列表和按会话 ID 查询消息记录的功能。
 */
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ChatHistoryRepository chatHistoryRepository;
    @Autowired
    private ChatMemory chatMemory;

    /**
     * 根据类型查询所有会话 ID 列表
     */
    @GetMapping(value = "/{type}")
    public List<String> getChatIds(@PathVariable("type") String type) {
            return chatHistoryRepository.getChatIds(type);
    }

    /**
     * 根据类型和会话 ID 查询消息记录
     */
    @GetMapping(value = "/{type}/{chatId}")
    public List<MessageVo> getChatHistory(@PathVariable("type") String type, @PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        if(messages == null){
            return List.of();
        }
        return messages.stream().map(m -> new MessageVo(m)).toList();
    }
}



