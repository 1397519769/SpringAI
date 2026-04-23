package com.example.springai.config;

import com.example.springai.tools.CourseTools;
import com.example.springai.tools.SupervisionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static com.example.springai.constants.SystemConstants.SERVICE_SYSTEM_PROMPT;
import static com.example.springai.constants.SystemConstants.SUPERVISION_SYSTEM_PROMPT;

/**
 * Spring AI 配置类
 * 配置聊天客户端和会话记忆功能
 */
@Configuration
public class CommonConfiguration {

    /**
     * 配置会话内存 - 使用 MessageWindowChatMemory 保留最近的消息历史
     * 默认保留最近的消息数量可根据需要调整
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .build();
    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 配置 ChatClient，启用聊天记忆功能和日志记录
     * MessageChatMemoryAdvisor 自动管理对话上下文
     * 使用 'userId' 作为会话 ID 参数名
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

}
