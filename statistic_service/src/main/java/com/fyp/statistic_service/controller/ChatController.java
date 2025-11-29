package com.fyp.statistic_service.controller;


import com.fyp.statistic_service.dto.request.ApiResponse;
import com.fyp.statistic_service.dto.request.ChatRequest;
import com.fyp.statistic_service.dto.response.ChatBotResponse;
import com.fyp.statistic_service.service.BAChatBotService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatController {

    BAChatBotService BAChatBotService;


    @PostMapping("/chat")
    public ApiResponse<ChatBotResponse> chatOnce(@RequestBody ChatRequest chatRequest){

        return ApiResponse.<ChatBotResponse>builder().
                result(        BAChatBotService.chat(chatRequest)
    ).
        build();
    }





}
