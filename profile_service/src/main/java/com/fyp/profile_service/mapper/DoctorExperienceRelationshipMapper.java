package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fyp.profile_service.dto.request.DoctorExperienceUpdateRequest;
import com.fyp.profile_service.dto.response.DoctorExperienceResponse;
import com.fyp.profile_service.entity.DoctorExperienceRelationship;

@Mapper(
        componentModel = "spring",
        uses = {PracticeExperienceMapper.class})
@Qualifier("doctorExperienceRelationshipMapper")
public interface DoctorExperienceRelationshipMapper {

    @Mapping(source = "id", target = "relationshipId")
    @Mapping(source = "experience", target = "experience")
    DoctorExperienceResponse toResponse(DoctorExperienceRelationship relationship);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", ignore = true)
    void updateRelationshipMetadata(
            @MappingTarget DoctorExperienceRelationship relationship, DoctorExperienceUpdateRequest request);
}
