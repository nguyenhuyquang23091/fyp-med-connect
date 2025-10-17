package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fyp.profile_service.dto.request.PracticeExperienceRequest;
import com.fyp.profile_service.dto.response.PracticeExperienceResponse;
import com.fyp.profile_service.entity.PracticeExperience;

@Mapper(componentModel = "spring")
public interface PracticeExperienceMapper {

    @Mapping(target = "id", ignore = true)
    PracticeExperience toPracticeExperience(PracticeExperienceRequest request);

    PracticeExperienceResponse toPracticeExperienceResponse(PracticeExperience experience);

    @Mapping(target = "id", ignore = true)
    void updatePracticeExperience(@MappingTarget PracticeExperience experience, PracticeExperienceRequest request);
}
