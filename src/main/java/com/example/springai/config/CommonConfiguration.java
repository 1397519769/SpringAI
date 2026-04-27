package com.example.springai.config;

import com.example.springai.repository.RedisChatMemoryRepository;
import com.example.springai.tools.CourseTools;
import com.example.springai.tools.SupervisionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.*;
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

/**
 * Spring AI 配置类
 * 配置聊天客户端和会话记忆功能
 */
@Configuration
public class CommonConfiguration {

    /**
     * 创建基于 Redis 的聊天消息存储仓库。
     * 使用 StringRedisTemplate 以 Hash 结构持久化每个会话的聊天记录。
     */
    @Bean
    public ChatMemoryRepository redisChatMemoryRepository(StringRedisTemplate stringRedisTemplate) {
        return new RedisChatMemoryRepository(stringRedisTemplate);
    }

    /**
     * 创建聊天会话内存窗口。
     * 基于 Redis 持久化仓库，设置每个会话最多保留 50 条消息。
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(50)
                .build();
    }

    /**
     * 创建向量存储，用于 RAG 检索增强生成。
     * 使用 SimpleVectorStore 实现本地向量存储和相似度检索。
     */
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 创建通用对话客户端。
     * 系统提示词设定为"小智"角色，启用消息日志和 Redis 持久化会话记忆。
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem("你是一个专业的智能助手，你的名字叫小智，请以小智的身份和语气回复消息")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * 创建智能客服客户端。
     * 使用督查督办系统提示词，绑定督查督办工具调用能力，支持 Redis 会话记忆。
     */
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

    /**
     * 创建 PDF 文档问答客户端。
     * 基于向量存储实现 RAG 检索增强，设置相似度阈值 0.6、取 Top 2 结果作为上下文。
     */
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
}
