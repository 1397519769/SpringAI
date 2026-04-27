package com.example.springai.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 基于 Redis Set 的聊天会话 ID 存储实现。
 * 按类型（chat/service/pdf）分组管理会话 ID。
 * Key 格式: spring_ai_chat_history:{type}
 */
@Component
public class RedisChatHistoryRepository implements ChatHistoryRepository {

    private static final String KEY_PREFIX = "spring_ai_chat_history:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisChatHistoryRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void save(String type, String chatId) {
        String key = KEY_PREFIX + type;
        stringRedisTemplate.opsForSet().add(key, chatId);
    }

    @Override
    public List<String> getChatIds(String type) {
        String key = KEY_PREFIX + type;
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        return members != null ? new ArrayList<>(members) : List.of();
    }
}
