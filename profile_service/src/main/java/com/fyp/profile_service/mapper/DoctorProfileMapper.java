package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fyp.profile_service.dto.request.DoctorProfileUpdateRequest;
import com.fyp.profile_service.dto.response.DoctorProfileResponse;
import com.fyp.profile_service.entity.DoctorProfile;
import com.fyp.profile_service.entity.UserProfile;

@Mapper(
        componentModel = "spring",
        uses = {
            DoctorSpecialtyRelationshipMapper.class,
            DoctorServiceRelationshipMapper.class,
            DoctorExperienceRelationshipMapper.class
        })
@Qualifier("doctorProfileMapper")
public interface DoctorProfileMapper {

    @Mapping(source = "doctorProfile.id", target = "id")
    @Mapping(source = "doctorProfile.userId", target = "userId")
    DoctorProfileResponse toResponse(DoctorProfile doctorProfile, UserProfile userProfile);

    void updateDoctorBaseProfile(@MappingTarget DoctorProfile doctorProfile, DoctorProfileUpdateRequest request);
}
