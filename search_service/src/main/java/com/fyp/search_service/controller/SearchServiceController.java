package com.fyp.search_service.controller;


import com.fyp.search_service.dto.request.ApiResponse;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.search.ElasticSearchProxy;
import com.fyp.search_service.service.DoctorProfileSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/searchService")
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchServiceController {
 ElasticSearchProxy elasticSearchProxy;
 DoctorProfileSearchService doctorProfileSearchService;

 @PostMapping("/doctor")
 public ApiResponse<PageResponse<DoctorProfileResponse>> search( @RequestBody SearchFilter searchFilter){
   log.info("Received doctor search request - term: {}, page: {}, size: {}",
           searchFilter.getTerm(), searchFilter.getPage(), searchFilter.getSize());

   PageResponse<DoctorProfileResponse> pageResponse = elasticSearchProxy.searchDoctorByTerm(searchFilter);

   return ApiResponse.<PageResponse<DoctorProfileResponse>>builder()
           .result(pageResponse)
           .build();
 }

 @GetMapping("/allDoctors")
    public ApiResponse<PageResponse<DoctorProfileResponse>> searchAllDoctor(
         @RequestParam(value = "page", required = false, defaultValue = "0") int page,
         @RequestParam(value = "size", required = false, defaultValue = "10") int size
 ) {
     return ApiResponse.
             <PageResponse<DoctorProfileResponse>>builder()
             .result(doctorProfileSearchService.findAllDoctorProfile(page, size)).build();
 }

}
