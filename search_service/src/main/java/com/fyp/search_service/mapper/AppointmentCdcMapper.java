package com.fyp.search_service.mapper;

import com.fyp.search_service.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;


@Mapper(componentModel = "spring")
public interface AppointmentCdcMapper {

    @Mapping(target = "id", expression = "java(toString(cdcData.get(\"id\")))")
    @Mapping(target = "patientName", expression = "java(toString(cdcData.get(\"patient_name\")))")
    @Mapping(target = "doctorName", expression = "java(toString(cdcData.get(\"doctor_name\")))")
    @Mapping(target = "doctorSpecialty", expression = "java(toString(cdcData.get(\"doctor_specialty\")))")
    @Mapping(target = "appointmentDate", expression = "java(parseDateTime(cdcData.get(\"appointment_date\")))")
    @Mapping(target = "status", expression = "java(toString(cdcData.get(\"status\")))")
    @Mapping(target = "reason", expression = "java(toString(cdcData.get(\"reason\")))")
    AppointmentEntity toAppointmentEntity(Map<String, Object> cdcData);

    default String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    @Named("parseDateTime")
    default LocalDateTime parseDateTime(Object dateTimeObj) {
        if (dateTimeObj == null) {
            return null;
        }

        // Handle epoch milliseconds (from Debezium timestamp)
        if (dateTimeObj instanceof Long) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli((Long) dateTimeObj),
                    ZoneId.systemDefault()
            );
        }

        // Handle epoch milliseconds as String
        if (dateTimeObj instanceof String) {
            String dateTimeStr = (String) dateTimeObj;
            try {
                // Try parsing as epoch milliseconds
                long epochMilli = Long.parseLong(dateTimeStr);
                return LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(epochMilli),
                        ZoneId.systemDefault()
                );
            } catch (NumberFormatException e) {
                // If not a number, try parsing as ISO-8601 string
                return LocalDateTime.parse(dateTimeStr);
            }
        }

        return null;
    }
}