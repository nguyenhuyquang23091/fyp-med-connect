package com.fyp.search_service.controller;


import com.fyp.search_service.dto.request.ApiResponse;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.search.ElasticSearchProxy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchServiceController {
 ElasticSearchProxy elasticSearchProxy;

 @PostMapping("/doctor")
 public ApiResponse<List<DoctorProfileResponse>> search(@RequestBody SearchFilter searchFilter){
   return ApiResponse.<List<DoctorProfileResponse>>builder().
           result(elasticSearchProxy.searchDoctor(searchFilter)).
           build();
 }



 }
