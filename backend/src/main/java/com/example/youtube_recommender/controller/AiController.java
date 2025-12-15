package com.example.youtube_recommender.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {
    @Autowired
    private ChatModel chatModel;

    @GetMapping("/ai/chat")
    public String chat(@RequestParam(defaultValue = "Hello, how are you?") String message) {
        return chatModel.call(message);
    }
}
