package com.profile.profile_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.profile.profile_service.constant.AccessStatus;
import com.profile.profile_service.entity.PrescriptionAccess;

@Repository
public interface PrescriptionAccessRepository extends MongoRepository<PrescriptionAccess, String> {

    Optional<PrescriptionAccess> findByIdAndPatientUserId(String id, String patientUserId);

    boolean existsPrescriptionAccessByDoctorUserIdAndPatientUserIdAndAccessStatus(
            String doctorUserId, String patientUserId, AccessStatus accessStatus);

    boolean existsPrescriptionAccessByDoctorUserIdAndPrescriptionIdAndAccessStatus(
            String doctorUserId, String prescriptionId, AccessStatus accessStatus);

    Optional<PrescriptionAccess> findByDoctorUserIdAndPrescriptionId(String doctorUserId, String prescriptionId);

    List<PrescriptionAccess> findByDoctorUserId(String doctorUserId);

    List<PrescriptionAccess> findAllByAccessStatusAndRequestedAtBefore(AccessStatus accessStatus, Instant time);
}
