package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fyp.profile_service.dto.request.PrescriptionNotification;
import com.fyp.profile_service.dto.response.PrescriptionAccessResponse;
import com.fyp.profile_service.entity.PrescriptionAccess;

@Mapper(componentModel = "spring")
public interface PrescriptionAccessMapper {
    PrescriptionAccess toPrescriptionAccess(PrescriptionNotification request);

    // have to do custom mapper to map from Enum Type in AccessStatus
    @Mapping(
            target = "accessStatus",
            expression =
                    "java(prescriptionAccess.getAccessStatus() != null ? prescriptionAccess.getAccessStatus().name() : null)")
    PrescriptionAccessResponse toPrescriptionResponse(PrescriptionAccess prescriptionAccess);
}
