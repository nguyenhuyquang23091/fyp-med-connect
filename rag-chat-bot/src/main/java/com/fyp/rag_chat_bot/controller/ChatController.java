package com.fyp.rag_chat_bot.controller;



import com.fyp.rag_chat_bot.dto.request.ChatRequest;
import com.fyp.rag_chat_bot.dto.response.ApiResponse;
import com.fyp.rag_chat_bot.dto.response.ChatBotResponse;
import com.fyp.rag_chat_bot.dto.response.PageResponse;
import com.fyp.rag_chat_bot.services.RagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class ChatController {
    RagService service;

    @PostMapping("/ai/generate")
    public ChatBotResponse chat(@Valid @RequestBody ChatRequest chatRequest) {
        return service.chat(chatRequest);
    }

    @GetMapping("/ai/{conversationId}")
    public ApiResponse<PageResponse<ChatBotResponse>> chat(@PathVariable @Valid String conversationId,
                                                          @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                          @RequestParam(value = "size", required = false, defaultValue = "20") int size)

            {

        return ApiResponse.<PageResponse<ChatBotResponse>>builder().
                result(service.getMyCurrentConversationData(conversationId, page, size)).build();
    }


    @DeleteMapping("ai/{conversationId}")
    public void deleteConversation(
            @PathVariable @NotBlank(message = "Conversation ID cannot be blank") String conversationId){
         service.deleteConversation(conversationId);
    }


}

