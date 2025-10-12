package com.fyp.file_service.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyp.file_service.dto.FileInfo;
import com.fyp.file_service.dto.response.FileResponse;
import com.fyp.file_service.entity.FileManagement;
import com.fyp.file_service.exceptions.AppException;
import com.fyp.file_service.exceptions.ErrorCode;
import com.fyp.file_service.mapper.FileManagementMapper;
import com.fyp.file_service.repository.FileManagementRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class FileService {
    Cloudinary cloudinary;
    @Qualifier("fileManagementMapperImpl")
    FileManagementMapper managementMapper;
    FileManagementRepository repository;
    ObjectMapper objectMapper;


    public FileResponse uploadFile(MultipartFile file ) throws  IOException{

        String folderName = determineFolder();
        Map<?, ? > cloudinaryResponse = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folderName));
        log.info("Raw Cloudinary response: {}", cloudinaryResponse);

        FileInfo fileInfo = objectMapper.convertValue(cloudinaryResponse, FileInfo.class);
        log.info("Successfully converted to FileInfo: {}", fileInfo);

        FileManagement fileManagement = managementMapper.toFileManagement(fileInfo);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        fileManagement.setOwnerId(userId);
        repository.save(fileManagement);

        return FileResponse.builder().
                url(fileInfo.getSecureUrl())
                .originalFilename(file.getOriginalFilename()).
                build();
    }
    private String determineFolder(){
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            String callingService = request.getHeader("X-Calling-Service");
            log.info("Current calling service {}" , callingService);
            return mapServiceFolder(callingService);

        } catch (Exception e) {
            log.warn("Could not determine calling service, using default folder", e);
            return  "general";
        }

    }

    private String mapServiceFolder(String service){
        if(service == null ){
            return "general";
        }

        return switch (service.toLowerCase()){
            case "profile-service" -> "profile";
            case "document-service" -> "document";
            default -> "general";
        };
    }

    public void deleteFile(String fileUrl) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Find file metadata by URL and verify ownership
        FileManagement fileManagement = repository.findBySecureUrl(fileUrl)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Verify ownership
        if (!fileManagement.getOwnerId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Delete from Cloudinary using publicId
        Map<?, ?> cloudinaryResponse = cloudinary.uploader().destroy(
                fileManagement.getPublicId(),
                ObjectUtils.emptyMap()
        );

        log.info("Cloudinary deletion response: {}", cloudinaryResponse);

        // Delete metadata from MongoDB
        repository.delete(fileManagement);

        log.info("Successfully deleted file with publicId: {}", fileManagement.getPublicId());
    }


    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteFileByAuthorizedUser(String fileUrl, String requestPatientId) throws IOException {
        String doctorId = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("Doctor {} attempting to delete file owned by patient {}", doctorId, requestPatientId);

        FileManagement fileManagement = repository.findBySecureUrlAndOwnerId(fileUrl, requestPatientId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_FILE_FOUND));

        // Delete from Cloudinary
        Map<?, ?> cloudinaryResponse = cloudinary.uploader().destroy(
                fileManagement.getPublicId(),
                ObjectUtils.emptyMap()
        );

        log.info("Cloudinary deletion response: {}", cloudinaryResponse);

        // Delete metadata from MongoDB
        repository.delete(fileManagement);

        log.info("Doctor {} successfully deleted file with publicId: {} owned by patient {}",
                 doctorId, fileManagement.getPublicId(), requestPatientId);
    }

}
