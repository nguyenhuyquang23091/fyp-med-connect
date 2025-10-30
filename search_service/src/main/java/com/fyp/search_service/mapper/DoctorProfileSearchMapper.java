package com.fyp.search_service.mapper;

import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.entity.DoctorProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface DoctorProfileSearchMapper {
    //nhin vo la biet ngay thang mapper roi

    DoctorProfileResponse toDoctorProfileResponse(DoctorProfile doctorProfile);
}
