package com.fyp.profile_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fyp.event.dto.DoctorProfileEntityType;
import com.fyp.profile_service.dto.request.PracticeExperienceRequest;
import com.fyp.profile_service.dto.response.PracticeExperienceResponse;
import com.fyp.profile_service.entity.DoctorExperienceRelationship;
import com.fyp.profile_service.entity.DoctorProfile;
import com.fyp.profile_service.entity.PracticeExperience;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.DoctorProfileCdcMapper;
import com.fyp.profile_service.mapper.PracticeExperienceMapper;
import com.fyp.profile_service.repository.DoctorProfileRepository;
import com.fyp.profile_service.repository.PracticeExperienceRepository;
import com.fyp.profile_service.utils.ProfileServiceUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PracticeExperienceService {

    PracticeExperienceRepository practiceExperienceRepository;
    DoctorProfileRepository doctorProfileRepository;
    PracticeExperienceMapper practiceExperienceMapper;
    DoctorProfileCdcProducer cdcProducer;
    DoctorProfileCdcMapper cdcMapper;

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public PracticeExperienceResponse addPracticeExperience(PracticeExperienceRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        log.info("Current user ");
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PracticeExperience experience = practiceExperienceMapper.toPracticeExperience(request);
        experience = practiceExperienceRepository.save(experience);

        DoctorExperienceRelationship relationship = DoctorExperienceRelationship.builder()
                .experience(experience)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isHighlighted(false)
                .build();

        doctorProfile.getPracticeExperience().add(relationship);
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // Publish CDC event for experience creation
        Map<String, Object> afterState = cdcMapper.toExperienceMap(relationship);
        cdcProducer.publishCreate(DoctorProfileEntityType.EXPERIENCE, afterState, doctorProfile.getId(), userId);

        log.info("Doctor {} added practice experience at {}", userId, experience.getHospitalName());
        return practiceExperienceMapper.toPracticeExperienceResponse(experience);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public PracticeExperienceResponse updatePracticeExperience(String experienceId, PracticeExperienceRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PracticeExperience experience = practiceExperienceRepository
                .findById(experienceId)
                .orElseThrow(() -> new AppException(ErrorCode.PRACTICE_EXPERIENCE_NOT_FOUND));

        DoctorExperienceRelationship relationship = doctorProfile.getPracticeExperience().stream()
                .filter(rel -> rel.getExperience().getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRACTICE_EXPERIENCE_NOT_OWNED));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toExperienceMap(relationship);

        practiceExperienceMapper.updatePracticeExperience(experience, request);
        experience = practiceExperienceRepository.save(experience);

        // Capture after state for CDC
        Map<String, Object> afterState = cdcMapper.toExperienceMap(relationship);

        // Publish CDC event for experience update
        cdcProducer.publishUpdate(
                DoctorProfileEntityType.EXPERIENCE, beforeState, afterState, doctorProfile.getId(), userId);

        log.info("Doctor {} updated practice experience {}", userId, experienceId);
        return practiceExperienceMapper.toPracticeExperienceResponse(experience);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public void deletePracticeExperience(String experienceId) {
        String userId = ProfileServiceUtil.getCurrentUserId();

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorExperienceRelationship relationshipToRemove = doctorProfile.getPracticeExperience().stream()
                .filter(rel -> rel.getExperience().getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRACTICE_EXPERIENCE_NOT_OWNED));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toExperienceMap(relationshipToRemove);

        doctorProfile.getPracticeExperience().remove(relationshipToRemove);
        doctorProfileRepository.save(doctorProfile);

        practiceExperienceRepository.deleteById(experienceId);

        // Publish CDC event for experience deletion
        cdcProducer.publishDelete(DoctorProfileEntityType.EXPERIENCE, beforeState, doctorProfile.getId(), userId);

        log.info("Doctor {} deleted practice experience {}", userId, experienceId);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public List<PracticeExperienceResponse> getMyPracticeExperiences() {
        String userId = ProfileServiceUtil.getCurrentUserId();

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfile.getPracticeExperience().stream()
                .map(rel -> practiceExperienceMapper.toPracticeExperienceResponse(rel.getExperience()))
                .toList();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public PracticeExperienceResponse highlightPracticeExperience(String experienceId, boolean isHighlighted) {
        String userId = ProfileServiceUtil.getCurrentUserId();

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorExperienceRelationship relationship = doctorProfile.getPracticeExperience().stream()
                .filter(rel -> rel.getExperience().getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRACTICE_EXPERIENCE_NOT_OWNED));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toExperienceMap(relationship);

        relationship.setIsHighlighted(isHighlighted);
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // Capture after state for CDC
        Map<String, Object> afterState = cdcMapper.toExperienceMap(relationship);

        // Publish CDC event for experience metadata update
        cdcProducer.publishUpdate(
                DoctorProfileEntityType.EXPERIENCE, beforeState, afterState, doctorProfile.getId(), userId);

        log.info(
                "Doctor {} {} practice experience {}",
                userId,
                isHighlighted ? "highlighted" : "unhighlighted",
                experienceId);

        return practiceExperienceMapper.toPracticeExperienceResponse(relationship.getExperience());
    }
}
