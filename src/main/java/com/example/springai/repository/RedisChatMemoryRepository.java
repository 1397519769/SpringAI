package com.example.springai.repository;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

/**
 * 基于 Redis Hash 的聊天消息存储实现。
 * 每个会话存储为一个 Redis Hash：
 *   Key: spring_ai_chat_memory:{conversationId}
 *   Field: {index}:type / {index}:text
 * 会话 ID 通过 Set 维护，用于 findConversationIds() 查询。
 */
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private static final String KEY_PREFIX = "spring_ai_chat_memory:";
    private static final String IDS_SET = "spring_ai_chat_memory:__ids__";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisChatMemoryRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<String> findConversationIds() {
        Set<String> members = stringRedisTemplate.opsForSet().members(IDS_SET);
        return members != null ? new ArrayList<>(members) : List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        Long size = stringRedisTemplate.opsForHash().size(key);
        if (size == null || size == 0) {
            return List.of();
        }

        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        // 按索引分组: {index}:type / {index}:text
        Map<Integer, Map<String, String>> grouped = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey().toString();
            String[] parts = field.split(":", 2);
            int index = Integer.parseInt(parts[0]);
            String fieldKey = parts[1];
            grouped.computeIfAbsent(index, k -> new HashMap<>()).put(fieldKey, entry.getValue().toString());
        }

        List<Message> messages = new ArrayList<>();
        for (Map<String, String> msgData : grouped.values()) {
            messages.add(deserializeMessage(msgData));
        }
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String key = KEY_PREFIX + conversationId;
        stringRedisTemplate.delete(key);

        Map<String, String> hash = new LinkedHashMap<>();
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            hash.put(i + ":type", msg.getMessageType().toString());
            hash.put(i + ":text", msg.getText());
        }
        if (!hash.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(key, hash);
        }

        stringRedisTemplate.opsForSet().add(IDS_SET, conversationId);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        stringRedisTemplate.delete(KEY_PREFIX + conversationId);
        stringRedisTemplate.opsForSet().remove(IDS_SET, conversationId);
    }

    private Message deserializeMessage(Map<String, String> data) {
        String type = data.get("type");
        String text = data.get("text");

        return switch (type) {
            case "SYSTEM" -> SystemMessage.builder().text(text).build();
            case "ASSISTANT" -> AssistantMessage.builder().content(text).build();
            case "USER" -> UserMessage.builder().text(text).build();
            case "TOOL" -> ToolResponseMessage.builder().responses(List.of()).build();
            default -> UserMessage.builder().text(text).build();
        };
    }
}
