package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fyp.profile_service.dto.request.DoctorServiceRequest;
import com.fyp.profile_service.dto.response.DoctorServiceResponse;
import com.fyp.profile_service.entity.DoctorServiceRelationship;

@Mapper(
        componentModel = "spring",
        uses = {MedicalServiceMapper.class})
@Qualifier("doctorServiceRelationshipMapper")
public interface DoctorServiceRelationshipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    DoctorServiceRelationship toRelationship(DoctorServiceRequest request);

    @Mapping(source = "id", target = "relationshipId")
    @Mapping(source = "service", target = "service")
    DoctorServiceResponse toResponse(DoctorServiceRelationship relationship);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    void updateRelationship(@MappingTarget DoctorServiceRelationship relationship, DoctorServiceRequest request);
}
