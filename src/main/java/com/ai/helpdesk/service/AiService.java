package com.ai.helpdesk.service;


import com.ai.helpdesk.tools.TicketDatabaseTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiService {

    private ChatClient chatClient;

    private TicketDatabaseTool ticketDatabaseTool;

    @Value("classpath:/helpdesk-system.st")
    private Resource systemPrompt;

    public AiService(ChatClient chatClient, TicketDatabaseTool ticketDatabaseTool) {
        this.chatClient = chatClient;
        this.ticketDatabaseTool = ticketDatabaseTool;
    }

    public String getResponseFromAssistant(String query, String conversationId){
        return this.chatClient.prompt().advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,conversationId))
                .tools(ticketDatabaseTool)
                .system(systemPrompt)
                .user(query)
                .call().content();
    }


    public String getResponseFromAssistantV2(String query){
        return this.chatClient.prompt().tools(ticketDatabaseTool)
                .system(systemPrompt)
                .user(query)
                .call().content();
    }

}
