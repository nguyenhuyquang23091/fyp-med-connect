package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fyp.profile_service.dto.request.MedicalServiceRequest;
import com.fyp.profile_service.dto.response.MedicalServiceResponse;
import com.fyp.profile_service.entity.MedicalService;

@Mapper(componentModel = "spring")
public interface MedicalServiceMapper {

    @Mapping(target = "id", ignore = true)
    MedicalService toMedicalService(MedicalServiceRequest request);

    MedicalServiceResponse toMedicalServiceResponse(MedicalService service);

    @Mapping(target = "id", ignore = true)
    void updateMedicalService(@MappingTarget MedicalService service, MedicalServiceRequest request);
}
