package com.ai.helpdesk.controller;

import com.ai.helpdesk.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {


    private AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }


    @PostMapping
    public ResponseEntity<String> getResponse(@RequestBody String query, @RequestHeader("ConversationId") String conversationId){
        return ResponseEntity.status(HttpStatus.OK).body(aiService.getResponseFromAssistant(query, conversationId));
    }

    @GetMapping("/stream")
    public ResponseEntity<String> getResponseV2(@RequestParam(name = "query") String query){
        return ResponseEntity.status(HttpStatus.OK).body(aiService.getResponseFromAssistantV2(query));
    }



}
