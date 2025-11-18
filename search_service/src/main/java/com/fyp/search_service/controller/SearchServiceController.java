package com.fyp.search_service.controller;


import com.fyp.search_service.dto.request.ApiResponse;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.dto.response.SearchSuggestion;
import com.fyp.search_service.search.ElasticSearchProxy;
import com.fyp.search_service.service.DoctorProfileSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class SearchServiceController {
 ElasticSearchProxy elasticSearchProxy;
DoctorProfileSearchService doctorProfileSearchService;

 @PostMapping("/doctor")
 public ApiResponse<PageResponse<DoctorProfileResponse>> search( @RequestBody SearchFilter searchFilter){
   log.info("Received doctor search request - term: {}, page: {}, size: {}, sortBy: {}, sortOrder: {}",
           searchFilter.getTerm(), searchFilter.getPage(), searchFilter.getSize(),
           searchFilter.getSortBy(), searchFilter.getSortOrder());

   PageResponse<DoctorProfileResponse> pageResponse = elasticSearchProxy.searchDoctorByTerm(searchFilter);

   return ApiResponse.<PageResponse<DoctorProfileResponse>>builder()
           .result(pageResponse)
           .build();
 }

 @GetMapping("/allDoctors")
    public ApiResponse<PageResponse<DoctorProfileResponse>> searchAllDoctor(
         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
         @RequestParam(value = "size", required = false, defaultValue = "10") int size
 ) {
     return ApiResponse.
             <PageResponse<DoctorProfileResponse>>builder()
             .result(doctorProfileSearchService.findAllDoctorProfile(page, size)).build();
 }

 @GetMapping("/suggestions")
 public ApiResponse<List<SearchSuggestion>> getSuggestions(
         @RequestParam("term") @Size(min = 1, max = 100, message = "Search term must be between 1 and 100 characters") String term,
         @RequestParam(value = "limit", required = false, defaultValue = "10") @Min(1) @Max(20) int limit
 ) {

     return ApiResponse.<List<SearchSuggestion>>builder()
             .result(elasticSearchProxy.getSuggestions(term, limit))
             .build();
 }


}
