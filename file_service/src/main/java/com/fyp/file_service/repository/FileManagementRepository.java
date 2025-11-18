package com.fyp.file_service.repository;

import com.fyp.file_service.entity.FileManagement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileManagementRepository extends MongoRepository<FileManagement, String> {
    Optional<FileManagement> findBySecureUrl(String secureUrl);

    Optional<FileManagement> findBySecureUrlAndOwnerId(String secureUrl, String ownerId);
}

