package com.fyp.profile_service.mapper;

import org.mapstruct.*;

import com.fyp.profile_service.constant.PrescriptionStatus;
import com.fyp.profile_service.dto.request.DoctorPrescriptionUpdateRequest;
import com.fyp.profile_service.dto.request.PatientPrescriptionUpdateRequest;
import com.fyp.profile_service.dto.response.PrescriptionGeneralResponse;
import com.fyp.profile_service.dto.response.PrescriptionResponse;
import com.fyp.profile_service.entity.UserPrescription;
import com.fyp.profile_service.utils.ProfileServiceUtil;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserPrescriptionMapper {

    PrescriptionResponse toUserPrescriptionResponse(UserPrescription prescription);

    PrescriptionGeneralResponse toUserPrescriptionGeneralResponse(UserPrescription userPrescription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userProfileId", ignore = true)
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    void updatePrescriptionFromPatient(
            @MappingTarget UserPrescription userPrescription, PatientPrescriptionUpdateRequest updateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userProfileId", ignore = true)
    @Mapping(target = "prescriptionName", ignore = true)
    @Mapping(target = "imageURLS", ignore = true)
    @Mapping(target = "doctorId", expression = "java(getCurrentDoctorId())")
    @Mapping(target = "status", expression = "java(getDoctorReviewedStatus())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    void updatePrescriptionFromDoctor(
            @MappingTarget UserPrescription userPrescription, DoctorPrescriptionUpdateRequest updateRequest);

    default String getCurrentDoctorId() {
        return ProfileServiceUtil.getCurrentUserId();
    }

    default PrescriptionStatus getDoctorReviewedStatus() {
        return PrescriptionStatus.DOCTOR_REVIEWED;
    }
}
