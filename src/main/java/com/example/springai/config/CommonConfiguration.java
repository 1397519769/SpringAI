package com.example.springai.config;

import com.example.springai.tools.CourseTools;
import com.example.springai.tools.SupervisionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import static com.example.springai.constants.SystemConstants.SERVICE_SYSTEM_PROMPT;
import static com.example.springai.constants.SystemConstants.SUPERVISION_SYSTEM_PROMPT;

import java.util.*;

/**
 * Spring AI 配置类
 * 配置聊天客户端和会话记忆功能
 */
@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemoryRepository redisChatMemoryRepository(StringRedisTemplate stringRedisTemplate) {
        return new RedisChatMemoryRepository(stringRedisTemplate);
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(50)
                .build();
    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 配置 ChatClient，启用聊天记忆功能和日志记录
     * MessageChatMemoryAdvisor 自动管理对话上下文
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem("你是一个热心可爱的智能助手，你的名字叫小可爱，请以小可爱的身份和语气回复消息")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel chatModel, ChatMemory chatMemory, CourseTools courseTools, SupervisionTools supervisionTools) {
        return ChatClient
                .builder(chatModel)
                //课程预约
//                .defaultSystem(SERVICE_SYSTEM_PROMPT)
                //督查督办
                .defaultSystem(SUPERVISION_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                //课程预约
//                .defaultTools(courseTools)
                //督查督办
                .defaultTools(supervisionTools)
                .build();
    }

    @Bean
    public ChatClient pdfChatClient(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient
                .builder(model)
                .defaultSystem("请根据上下文回答问题，遇到上下文没有的问题，不要随意编造。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.6)
                                        .topK(2)
                                        .build())
                                .build()
                )
                .build();
    }

    /**
     * Redis-backed ChatMemoryRepository using Hash storage.
     * Each conversation is stored as a Redis Hash:
     *   Key: spring_ai_chat_memory:{conversationId}
     *   Field: {index}:type -> message type, {index}:text -> message text
     * Also maintains a Set of all conversation IDs for findConversationIds().
     */
    public static class RedisChatMemoryRepository implements ChatMemoryRepository {

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
            // Collect entries grouped by index
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
}
