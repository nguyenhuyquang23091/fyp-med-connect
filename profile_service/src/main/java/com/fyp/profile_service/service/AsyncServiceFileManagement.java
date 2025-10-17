package com.fyp.profile_service.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fyp.profile_service.config.AuthenticationInterceptor;
import com.fyp.profile_service.dto.response.FileResponse;
import com.fyp.profile_service.repository.httpClient.FileFeignClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AsyncServiceFileManagement {
    FileFeignClient fileUpload;

    @Async("fileManagementExecutor")
    public CompletableFuture<String> uploadImageAsync(MultipartFile multipartFile, String authToken) {
        try {

            // Set auth token in ThreadLocal for this async thread
            // Reason: Feign client needs Authorization header, but async threads don't have request context

            AuthenticationInterceptor.setAuthToken(authToken);
            log.info(
                    "Starting async upload for file: {} in thread: {}",
                    multipartFile.getOriginalFilename(),
                    Thread.currentThread().getName());

            // Upload the file using Feign client
            FileResponse response = fileUpload.uploadMedia(multipartFile).getResult();
            String url = response.getUrl();

            log.info(
                    "Successfully uploaded file: {} -> {} in thread: {}",
                    multipartFile.getOriginalFilename(),
                    url,
                    Thread.currentThread().getName());

            return CompletableFuture.completedFuture(url);

        } catch (Exception e) {
            log.error(
                    "Failed to upload file: {} in thread: {}",
                    multipartFile.getOriginalFilename(),
                    Thread.currentThread().getName(),
                    e);
            return CompletableFuture.failedFuture(e);

        } finally {
            // Always clear ThreadLocal to prevent memory leaks
            // Reason: Thread pool reuses threads, so we must clean up ThreadLocal data
            AuthenticationInterceptor.clearAuthToken();
        }
    }

    @Async("fileManagementExecutor")
    public CompletableFuture<Void> deleteImageAsync(String fileUrl, String authToken) {
        try {
            // Set auth token in ThreadLocal for this async thread
            AuthenticationInterceptor.setAuthToken(authToken);

            log.info(
                    "Starting async delete for file: {} in thread: {}",
                    fileUrl,
                    Thread.currentThread().getName());

            // Delete the file using Feign client
            fileUpload.deleteMedia(fileUrl);

            log.info(
                    "Successfully deleted file: {} in thread: {}",
                    fileUrl,
                    Thread.currentThread().getName());

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error(
                    "Failed to delete file: {} in thread: {}",
                    fileUrl,
                    Thread.currentThread().getName(),
                    e);
            return CompletableFuture.failedFuture(e);

        } finally {
            // Always clear ThreadLocal to prevent memory leaks
            AuthenticationInterceptor.clearAuthToken();
        }
    }

    @Async("fileManagementExecutor")
    public CompletableFuture<Void> authorizedUserDeleteImageAsync(String fileUrl, String patientId, String authToken) {
        try {
            // Set auth token in ThreadLocal for this async thread
            // Reason: Feign client needs Authorization header, but async threads don't have request context
            AuthenticationInterceptor.setAuthToken(authToken);

            log.info(
                    "Starting async authorized delete for file: {} (owner: {}) in thread: {}",
                    fileUrl,
                    patientId,
                    Thread.currentThread().getName());

            // Delete the file using Feign client with authorized user endpoint
            fileUpload.authorizedUserDeleteMedia(patientId, fileUrl);

            log.info(
                    "Successfully deleted file: {} (owner: {}) in thread: {}",
                    fileUrl,
                    patientId,
                    Thread.currentThread().getName());

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error(
                    "Failed to delete file: {} (owner: {}) in thread: {}",
                    fileUrl,
                    patientId,
                    Thread.currentThread().getName(),
                    e);
            return CompletableFuture.failedFuture(e);

        } finally {
            // Always clear ThreadLocal to prevent memory leaks
            // Reason: Thread pool reuses threads, so we must clean up ThreadLocal data
            AuthenticationInterceptor.clearAuthToken();
        }
    }
}
