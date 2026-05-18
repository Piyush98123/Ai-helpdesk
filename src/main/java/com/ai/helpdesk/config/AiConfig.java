package com.ai.helpdesk.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient getChatClient(ChatClient.Builder builder, JdbcChatMemoryRepository jdbcChatMemoryRepository){
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder().chatMemoryRepository(jdbcChatMemoryRepository).maxMessages(20)
                .build();
        return builder.defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build()).build();
    }
}
