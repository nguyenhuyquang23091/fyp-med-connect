package com.fyp.search_service.mapper;

import com.fyp.search_service.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;


@Mapper(componentModel = "spring")
public interface AppointmentCdcMapper {

    @Mapping(target = "id", expression = "java(toString(cdcData.get(\"id\")))")
    @Mapping(target = "userId", expression = "java(toString(cdcData.get(\"user_id\")))")
    @Mapping(target = "patientFullName", expression = "java(toString(cdcData.get(\"patient_full_name\")))")
    @Mapping(target = "doctorFullName", expression = "java(toString(cdcData.get(\"doctor_full_name\")))")
    @Mapping(target = "doctorId", expression = "java(toString(cdcData.get(\"doctor_id\")))")
    @Mapping(target = "reasons", expression = "java(toString(cdcData.get(\"reasons\")))")
    @Mapping(target = "phoneNumber", expression = "java(toString(cdcData.get(\"phone_number\")))")
    @Mapping(target = "appointmentDateTime", expression = "java(toDateTimeString(cdcData.get(\"appointment_date_time\")))")
    @Mapping(target = "createdDate", expression = "java(toDateTimeString(cdcData.get(\"created_date\")))")
    @Mapping(target = "specialty", expression = "java(toString(cdcData.get(\"specialty\")))")
    @Mapping(target = "services", expression = "java(toString(cdcData.get(\"services\")))")
    @Mapping(target = "appointmentStatus", expression = "java(toString(cdcData.get(\"appointment_status\")))")
    @Mapping(target = "consultationType", expression = "java(toString(cdcData.get(\"consultation_type\")))")
    @Mapping(target = "paymentMethod", expression = "java(toString(cdcData.get(\"payment_method\")))")
    @Mapping(target = "modifiedDate", expression = "java(toDateTimeString(cdcData.get(\"modified_date\")))")
    @Mapping(target = "prices", expression = "java(toBigDecimal(cdcData.get(\"prices\")))")
    AppointmentEntity toAppointmentEntity(Map<String, Object> cdcData);

    default String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    default BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return BigDecimal.valueOf(((Number) obj).doubleValue());
        if (obj instanceof String) {
            try {
                return new BigDecimal((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    default String toDateTimeString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        if (obj instanceof Number) {
            long epochMicros = ((Number) obj).longValue();
            return Instant.ofEpochMilli(epochMicros / 1000)
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                    .toString();
        }
        return obj.toString();
    }
}