package com.fyp.search_service.mapper;
import com.fyp.search_service.entity.DoctorProfile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


@Mapper(componentModel = "spring")
public interface DoctorProfileCdcMapper {


    //Convert CDC event data for indexing From a Map<Strinng, Object> data to a DoctorProfileENtity
    @Mapping(target = "doctorProfileId", expression = "java(toString(cdcData.get(\"profileId\")))")
    @Mapping(target = "userId", expression = "java(toString(cdcData.get(\"userId\")))")
    @Mapping(target = "residency", expression = "java(toString(cdcData.get(\"residency\")))")
    @Mapping(target = "yearsOfExperience", expression = "java(toInteger(cdcData.get(\"yearsOfExperience\")))")
    @Mapping(target = "bio", expression = "java(toString(cdcData.get(\"bio\")))")
    @Mapping(target = "fullName", expression = "java(toString(cdcData.get(\"fullName\")))")
    @Mapping(target = "avatar", expression = "java(toString(cdcData.get(\"avatar\")))")
    @Mapping(target = "email", expression = "java(toString(cdcData.get(\"email\")))")


    @Mapping(target = "isAvailable", expression = "java(toBoolean(cdcData.get(\"isAvailable\")))")
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "specialties", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    DoctorProfile toProfile(Map<String, Object> cdcData);

    @Mapping(target = "relationshipId", expression = "java(toLong(cdcData.get(\"relationshipId\")))")
    @Mapping(target = "serviceId", expression = "java(toLong(cdcData.get(\"serviceId\")))")
    @Mapping(target = "serviceName", expression = "java(toString(cdcData.get(\"serviceName\")))")
    @Mapping(target = "price", expression = "java(toBigDecimal(cdcData.get(\"price\")))")
    @Mapping(target = "currency", expression = "java(toString(cdcData.get(\"currency\")))")
    @Mapping(target = "displayOrder", expression = "java(toInteger(cdcData.get(\"displayOrder\")))")
    DoctorProfile.ServiceInfo toServiceInfo(Map<String, Object> cdcData);


    @Mapping(target = "relationshipId", expression = "java(toLong(cdcData.get(\"relationshipId\")))")
    @Mapping(target = "specialtyId", expression = "java(toLong(cdcData.get(\"specialtyId\")))")
    @Mapping(target = "specialtyName", expression = "java(toString(cdcData.get(\"specialtyName\")))")
    @Mapping(target = "specialtyCode", expression = "java(toString(cdcData.get(\"specialtyCode\")))")
    @Mapping(target = "specialtyDescription", expression = "java(toString(cdcData.get(\"specialtyDescription\")))")
    @Mapping(target = "isPrimary", expression = "java(toBoolean(cdcData.get(\"isPrimary\")))")
    @Mapping(target = "certificationDate", expression = "java(toDateString(cdcData.get(\"certificationDate\")))")
    @Mapping(target = "certificationBody", expression = "java(toString(cdcData.get(\"certificationBody\")))")
    @Mapping(target = "yearsOfExperienceInSpecialty", expression = "java(toInteger(cdcData.get(\"yearsOfExperienceInSpecialty\")))")
    DoctorProfile.SpecialtyInfo toSpecialtyInfo(Map<String, Object> cdcData);


    @Mapping(target = "relationshipId", expression = "java(toLong(cdcData.get(\"relationshipId\")))")
    @Mapping(target = "experienceId", expression = "java(toString(cdcData.get(\"experienceId\")))")
    @Mapping(target = "hospitalName", expression = "java(toString(cdcData.get(\"hospitalName\")))")
    @Mapping(target = "hospitalLogo", expression = "java(toString(cdcData.get(\"hospitalLogo\")))")
    @Mapping(target = "department", expression = "java(toString(cdcData.get(\"department\")))")
    @Mapping(target = "location", expression = "java(toString(cdcData.get(\"location\")))")
    @Mapping(target = "country", expression = "java(toString(cdcData.get(\"country\")))")
    @Mapping(target = "position", expression = "java(toString(cdcData.get(\"position\")))")
    @Mapping(target = "startDate", expression = "java(toDateString(cdcData.get(\"startDate\")))")
    @Mapping(target = "endDate", expression = "java(toDateString(cdcData.get(\"endDate\")))")
    @Mapping(target = "isCurrent", expression = "java(toBoolean(cdcData.get(\"isCurrent\")))")
    @Mapping(target = "description", expression = "java(toString(cdcData.get(\"description\")))")
    @Mapping(target = "displayOrder", expression = "java(toInteger(cdcData.get(\"displayOrder\")))")
    @Mapping(target = "isHighlighted", expression = "java(toBoolean(cdcData.get(\"isHighlighted\")))")
    DoctorProfile.ExperienceInfo toExperienceInfo(Map<String, Object> cdcData);

    // Helper methods for type conversion

    default String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    default Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    default Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Long) return ((Long) obj).intValue();
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    default Boolean toBoolean(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Boolean) return (Boolean) obj;
        if (obj instanceof String) return Boolean.parseBoolean((String) obj);
        return null;
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

    default String toDateString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate) return obj.toString();
        if (obj instanceof String) return (String) obj;
        if (obj instanceof Number) {
            // Handle epoch millis
            long epochMilli = ((Number) obj).longValue();
            return java.time.Instant.ofEpochMilli(epochMilli)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toString();
        }
        return obj.toString();
    }
}


