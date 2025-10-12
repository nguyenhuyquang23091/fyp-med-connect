package com.fyp.file_service.controller;

import com.fyp.file_service.dto.response.ApiResponse;
import com.fyp.file_service.dto.response.FileResponse;
import com.fyp.file_service.service.FileService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class FileController {
    FileService fileService;

    @PostMapping("/media/upload")
    public ApiResponse<FileResponse> uploadImage(
            @RequestParam("file") @NotNull(message = "File is required") MultipartFile file
    ) throws IOException {
        return ApiResponse.<FileResponse>builder()
                .result(fileService.uploadFile(file ))
                .build();
    }

    @DeleteMapping("/media/delete")
    public ApiResponse<Void> deleteFile(
            @RequestParam("fileUrl") @NotBlank(message = "File URL is required") String fileUrl
    ) throws IOException {
        fileService.deleteFile(fileUrl);
        return ApiResponse.<Void>builder()
                .message("File deleted successfully")
                .build();
    }
    /**
     * Endpoint for authorized users (e.g., doctors) to delete files owned by patients.
     * Requires DOCTOR role and proper access verification in the calling service.
     *
     * @param patientId the ID of the patient who owns the file
     * @param fileUrl the URL of the file to delete
     * @return ApiResponse with success message
     * @throws IOException if Cloudinary deletion fails
     */
    @DeleteMapping("/media/delete/{patientId}")
    public ApiResponse<Void> authorizedDoctorDeleteFile(
            @PathVariable @NotBlank(message = "Patient ID is required") String patientId,
            @RequestParam("fileUrl") @NotBlank(message = "File URL is required") String fileUrl
    ) throws IOException {
        fileService.deleteFileByAuthorizedUser(fileUrl, patientId);
        return ApiResponse.<Void>builder()
                .message("File deleted successfully by authorized user")
                .build();
    }

}
