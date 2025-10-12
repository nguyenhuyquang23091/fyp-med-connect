package com.fyp.notification_service.repository;

import com.fyp.notification_service.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {


    Page<Notification> findByRecipientUserId(String recipientUserId, Pageable pageable);


    @Query("{ 'recipientUserId': ?0, 'metadata.requestId': ?1 }")
    Optional<Notification> findByRecipientUserIdAndMetadataRequestId(String recipientUserId, String requestId);

}
