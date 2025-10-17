package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fyp.profile_service.dto.request.DoctorSpecialtyRequest;
import com.fyp.profile_service.dto.response.DoctorSpecialtyResponse;
import com.fyp.profile_service.entity.DoctorSpecialtyRelationship;

@Mapper(
        componentModel = "spring",
        uses = {SpecialtyMapper.class})
@Qualifier("doctorSpecialtyRelationshipMapper")
public interface DoctorSpecialtyRelationshipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    DoctorSpecialtyRelationship toRelationship(DoctorSpecialtyRequest request);

    @Mapping(source = "id", target = "relationshipId")
    @Mapping(source = "specialty", target = "specialty")
    DoctorSpecialtyResponse toResponse(DoctorSpecialtyRelationship relationship);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    void updateRelationship(@MappingTarget DoctorSpecialtyRelationship relationship, DoctorSpecialtyRequest request);
}
