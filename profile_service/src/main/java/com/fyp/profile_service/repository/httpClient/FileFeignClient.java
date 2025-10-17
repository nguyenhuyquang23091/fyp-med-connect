package com.fyp.profile_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fyp.profile_service.config.AuthenticationInterceptor;
import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.response.FileResponse;

@FeignClient(
        name = "file-service",
        url = "http://localhost:8086",
        configuration = {AuthenticationInterceptor.class})
public interface FileFeignClient {
    @PostMapping(value = "/file/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileResponse> uploadMedia(@RequestPart("file") MultipartFile file);

    @DeleteMapping(value = "/file/media/delete")
    ApiResponse<Void> deleteMedia(@RequestParam("fileUrl") String fileUrl);

    @DeleteMapping(value = "/file/media/delete/{patientId}")
    ApiResponse<Void> authorizedUserDeleteMedia(
            @PathVariable String patientId, @RequestParam("fileUrl") String fileUrl);
}
