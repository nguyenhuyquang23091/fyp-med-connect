package com.fyp.search_service.controller;


import com.fyp.search_service.dto.request.ApiResponse;
import com.fyp.search_service.dto.request.AppointmentSearchFilter;
import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.request.UserSearchFilter;
import com.fyp.search_service.dto.response.AppointmentResponse;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.dto.response.SearchSuggestion;
import com.fyp.search_service.dto.response.UserResponse;
import com.fyp.search_service.service.AppointmentSearchService;
import com.fyp.search_service.service.DoctorProfileSearchService;
import com.fyp.search_service.service.UserSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class SearchServiceController {
DoctorProfileSearchService doctorProfileSearchService;
AppointmentSearchService appointmentSearchService;
UserSearchService userSearchService;

 @PostMapping("/doctor")
 public ApiResponse<PageResponse<DoctorProfileResponse>> search( @RequestBody SearchFilter searchFilter){
   log.info("Received doctor search request - term: {}, page: {}, size: {}, sortBy: {}, sortOrder: {}",
           searchFilter.getTerm(), searchFilter.getPage(), searchFilter.getSize(),
           searchFilter.getSortBy(), searchFilter.getSortOrder());

   PageResponse<DoctorProfileResponse> pageResponse = doctorProfileSearchService.searchDoctorProfiles(searchFilter);

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

 @GetMapping("/doctor/suggestions")
 public ApiResponse<List<SearchSuggestion>> getSuggestions(
         @RequestParam("term") @Size(min = 1, max = 100, message = "Search term must be between 1 and 100 characters") String term,
         @RequestParam(value = "limit", required = false, defaultValue = "10") @Min(1) @Max(20) int limit
 ) {

     return ApiResponse.<List<SearchSuggestion>>builder()
             .result(doctorProfileSearchService.getDoctorSuggestions(term, limit))
             .build();
 }

 @PostMapping("/appointment")
 public ApiResponse<PageResponse<AppointmentResponse>> searchAppointments(@RequestBody AppointmentSearchFilter filter) {
     log.info("Received appointment search request - term: {}, page: {}, size: {}",
             filter.getTerm(), filter.getPage(), filter.getSize());

     PageResponse<AppointmentResponse> pageResponse = appointmentSearchService.searchAppointments(filter);

     return ApiResponse.<PageResponse<AppointmentResponse>>builder()
             .result(pageResponse)
             .build();
 }

 @GetMapping("/appointment/suggestions")
 public ApiResponse<List<SearchSuggestion>> getAppointmentSuggestions(
         @RequestParam("term") @Size(min = 1, max = 100, message = "Search term must be between 1 and 100 characters") String term,
         @RequestParam(value = "limit", required = false, defaultValue = "10") @Min(1) @Max(20) int limit
 ) {
     return ApiResponse.<List<SearchSuggestion>>builder()
             .result(appointmentSearchService.getAppointmentSuggestions(term, limit))
             .build();
 }

 @PostMapping("/user")
 public ApiResponse<PageResponse<UserResponse>> searchUsers(@RequestBody UserSearchFilter filter) {
     log.info("Received user search request - term: {}, page: {}, size: {}",
             filter.getTerm(), filter.getPage(), filter.getSize());

     PageResponse<UserResponse> pageResponse = userSearchService.searchUsers(filter);

     return ApiResponse.<PageResponse<UserResponse>>builder()
             .result(pageResponse)
             .build();
 }

 @GetMapping("/user/suggestions")
 public ApiResponse<List<SearchSuggestion>> getUserSuggestions(
         @RequestParam("term") @Size(min = 1, max = 100, message = "Search term must be between 1 and 100 characters") String term,
         @RequestParam(value = "limit", required = false, defaultValue = "10") @Min(1) @Max(20) int limit
 ) {
     return ApiResponse.<List<SearchSuggestion>>builder()
             .result(userSearchService.getUserSuggestions(term, limit))
             .build();
 }


}
