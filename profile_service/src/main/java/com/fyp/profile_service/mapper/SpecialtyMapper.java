package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fyp.profile_service.dto.request.SpecialtyRequest;
import com.fyp.profile_service.dto.response.SpecialtyResponse;
import com.fyp.profile_service.entity.Specialty;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    @Mapping(target = "id", ignore = true)
    Specialty toSpecialty(SpecialtyRequest request);

    SpecialtyResponse toSpecialtyResponse(Specialty specialty);

    @Mapping(target = "id", ignore = true)
    void updateSpecialty(@MappingTarget Specialty specialty, SpecialtyRequest request);
}
