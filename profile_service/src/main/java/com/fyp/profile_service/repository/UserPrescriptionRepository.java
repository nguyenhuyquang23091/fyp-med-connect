package com.fyp.profile_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fyp.profile_service.entity.UserPrescription;

@Repository
public interface UserPrescriptionRepository extends MongoRepository<UserPrescription, String> {

    Optional<List<UserPrescription>> findAllByUserId(String userId);

    Optional<UserPrescription> findByIdAndUserId(String prescriptionId, String userId);

    Page<UserPrescription> findByUserId(String userId, Pageable pageable);
}
