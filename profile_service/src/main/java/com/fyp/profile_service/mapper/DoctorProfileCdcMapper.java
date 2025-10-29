package com.fyp.profile_service.mapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fyp.profile_service.entity.*;


@Component
public class DoctorProfileCdcMapper {

    public Map<String, Object> toProfileMap(DoctorProfile profile) {
        Map<String, Object> map = new HashMap<>();

        if (profile == null) {
            return map;
        }

        map.put("profileId", profile.getId());
        map.put("userId", profile.getUserId());
        map.put("isAvailable", profile.getIsAvailable());
        map.put("yearsOfExperience", profile.getYearsOfExperience());
        map.put("bio", profile.getBio());
        map.put("residency", profile.getResidency());
        map.put("createdAt", profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null);
        map.put("updatedAt", profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : null);

        return map;
    }

    public Map<String, Object> toSpecialtyMap(DoctorSpecialtyRelationship relationship) {
        Map<String, Object> map = new HashMap<>();

        if (relationship == null) {
            return map;
        }

        map.put("relationshipId", relationship.getId());
        map.put("isPrimary", relationship.getIsPrimary());
        map.put("certificationDate", relationship.getCertificationDate() != null
                ? relationship.getCertificationDate().toString()
                : null);
        map.put("certificationBody", relationship.getCertificationBody());
        map.put("yearsOfExperienceInSpecialty", relationship.getYearsOfExperienceInSpecialty());

        if (relationship.getSpecialty() != null) {
            Specialty specialty = relationship.getSpecialty();
            map.put("specialtyId", specialty.getId());
            map.put("specialtyName", specialty.getName());
            map.put("specialtyCode", specialty.getCode());
            map.put("specialtyDescription", specialty.getDescription());
        }

        return map;
    }


    public Map<String, Object> toServiceMap(DoctorServiceRelationship relationship) {
        Map<String, Object> map = new HashMap<>();

        if (relationship == null) {
            return map;
        }

        map.put("relationshipId", relationship.getId());
        map.put("price", relationship.getPrice() != null ? relationship.getPrice().toString() : null);
        map.put("currency", relationship.getCurrency());
        map.put("displayOrder", relationship.getDisplayOrder());

        if (relationship.getService() != null) {
            MedicalService service = relationship.getService();
            map.put("serviceId", service.getId());
            map.put("serviceName", service.getName());
        }

        return map;
    }

    /**
     * Converts a DoctorExperienceRelationship to Map format for CDC events.
     *
     * @param relationship the experience relationship to convert
     * @return Map representation of the experience relationship
     */
    public Map<String, Object> toExperienceMap(DoctorExperienceRelationship relationship) {
        Map<String, Object> map = new HashMap<>();

        if (relationship == null) {
            return map;
        }

        map.put("relationshipId", relationship.getId());
        map.put("displayOrder", relationship.getDisplayOrder());
        map.put("isHighlighted", relationship.getIsHighlighted());

        if (relationship.getExperience() != null) {
            PracticeExperience experience = relationship.getExperience();
            map.put("experienceId", experience.getId());
            map.put("hospitalName", experience.getHospitalName());
            map.put("hospitalLogo", experience.getHospitalLogo());
            map.put("department", experience.getDepartment());
            map.put("location", experience.getLocation());
            map.put("country", experience.getCountry());
            map.put("position", experience.getPosition());
            map.put("startDate", experience.getStartDate() != null ? experience.getStartDate().toString() : null);
            map.put("endDate", experience.getEndDate() != null ? experience.getEndDate().toString() : null);
            map.put("isCurrent", experience.getIsCurrent());
            map.put("description", experience.getDescription());
        }

        return map;
    }

    /**
     * Creates a shallow Map for deletions (contains only relationship ID).
     *
     * @param relationshipId the ID of the deleted relationship
     * @return Map containing the relationship ID
     */
    public Map<String, Object> toDeletedMap(Long relationshipId) {
        Map<String, Object> map = new HashMap<>();
        map.put("relationshipId", relationshipId);
        return map;
    }

    /**
     * Creates a shallow Map for experience deletions (uses String ID).
     *
     * @param experienceId the ID of the deleted experience
     * @return Map containing the experience ID
     */
    public Map<String, Object> toDeletedExperienceMap(String experienceId) {
        Map<String, Object> map = new HashMap<>();
        map.put("experienceId", experienceId);
        return map;
    }
}
