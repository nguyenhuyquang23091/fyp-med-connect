package com.profile.profile_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.profile.profile_service.config.AuthenticationInterceptor;
import com.profile.profile_service.dto.request.ApiResponse;
import com.profile.profile_service.dto.response.FileResponse;

@FeignClient(
        name = "file-service",
        url = "http://localhost:8086",
        configuration = {AuthenticationInterceptor.class})
public interface FileFeignClient {
    @PostMapping(value = "/file/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<FileResponse> uploadMedia(
            @RequestPart("file") MultipartFile file);
}
