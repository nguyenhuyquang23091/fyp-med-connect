package com.fyp.search_service.controller;


import com.fyp.search_service.service.IndexService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IndexController {

    IndexService indexService;

    @PostMapping("/create")
    public void createIndex(){
        indexService.createIndices();
    }
}
