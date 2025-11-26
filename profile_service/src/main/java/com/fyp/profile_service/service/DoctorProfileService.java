package com.fyp.profile_service.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fyp.profile_service.dto.response.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fyp.event.dto.DoctorProfileEntityType;
import com.fyp.profile_service.dto.request.*;
import com.fyp.profile_service.entity.*;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.DoctorExperienceRelationshipMapper;
import com.fyp.profile_service.mapper.DoctorProfileCdcMapper;
import com.fyp.profile_service.mapper.DoctorProfileMapper;
import com.fyp.profile_service.mapper.DoctorServiceRelationshipMapper;
import com.fyp.profile_service.mapper.DoctorSpecialtyRelationshipMapper;
import com.fyp.profile_service.repository.DoctorProfileRepository;
import com.fyp.profile_service.repository.MedicalServiceRepository;
import com.fyp.profile_service.repository.SpecialtyRepository;
import com.fyp.profile_service.repository.UserProfileRepository;
import com.fyp.profile_service.utils.ProfileServiceUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DoctorProfileService {

    DoctorProfileRepository doctorProfileRepository;
    MedicalServiceRepository medicalServiceRepository;
    SpecialtyRepository specialtyRepository;

    @Qualifier("doctorServiceRelationshipMapper")
    DoctorServiceRelationshipMapper serviceRelationshipMapper;

    @Qualifier("doctorSpecialtyRelationshipMapper")
    DoctorSpecialtyRelationshipMapper specialtyRelationshipMapper;

    @Qualifier("doctorExperienceRelationshipMapper")
    DoctorExperienceRelationshipMapper experienceRelationshipMapper;

    DoctorProfileMapper doctorProfileMapper;
    UserProfileRepository userProfileRepository;

    DoctorProfileCdcProducer cdcProducer;
    DoctorProfileCdcMapper cdcMapper;

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorProfileResponse updateBaseDoctorProfile(DoctorProfileUpdateRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        return updateBaseDoctorProfile(userId, request);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorServiceResponse addServiceToProfile(DoctorServiceRequest request) {

        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        MedicalService service = medicalServiceRepository
                .findById(request.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND));

        boolean alreadyExists = doctorProfile.getServices().stream()
                .anyMatch(rel -> rel.getService().getId().equals(request.getServiceId()));

        if (alreadyExists) {
            throw new AppException(ErrorCode.MEDICAL_SERVICE_ALREADY_ADDED);
        }

        DoctorServiceRelationship relationship = serviceRelationshipMapper.toRelationship(request);
        relationship.setService(service);

        doctorProfile.getServices().add(relationship);
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // Publish CDC event for service creation
        Map<String, Object> afterState = cdcMapper.toServiceMap(relationship);
        cdcProducer.publishCreate(DoctorProfileEntityType.SERVICE, afterState, doctorProfile.getId(), userId);
        log.info("Doctor {} added service: {}", userId, service.getName());
        return serviceRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorServiceResponse updateServiceInProfile(Long relationshipId, DoctorServiceRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorServiceRelationship relationship = doctorProfile.getServices().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SERVICE_NOT_FOUND));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toServiceMap(relationship);

        if (!relationship.getService().getId().equals(request.getServiceId())) {
            MedicalService newService = medicalServiceRepository
                    .findById(request.getServiceId())
                    .orElseThrow(() -> new AppException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND));

            boolean alreadyExists = doctorProfile.getServices().stream()
                    .anyMatch(rel -> !rel.getId().equals(relationshipId)
                            && rel.getService().getId().equals(request.getServiceId()));

            if (alreadyExists) {
                throw new AppException(ErrorCode.MEDICAL_SERVICE_ALREADY_ADDED);
            }

            relationship.setService(newService);
        }

        serviceRelationshipMapper.updateRelationship(relationship, request);
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // Capture after state for CDC
        Map<String, Object> afterState = cdcMapper.toServiceMap(relationship);

        // Publish CDC event for service update
        cdcProducer.publishUpdate(
                DoctorProfileEntityType.SERVICE, beforeState, afterState, doctorProfile.getId(), userId);

        log.info("Doctor {} updated service relationship {}", userId, relationshipId);
        return serviceRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public void removeServiceFromProfile(Long relationshipId) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorServiceRelationship relationshipToRemove = doctorProfile.getServices().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SERVICE_NOT_FOUND));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toServiceMap(relationshipToRemove);

        doctorProfile.getServices().remove(relationshipToRemove);
        doctorProfileRepository.save(doctorProfile);

        // Publish CDC event for service deletion
        cdcProducer.publishDelete(DoctorProfileEntityType.SERVICE, beforeState, doctorProfile.getId(), userId);

        log.info("Doctor {} removed service relationship {}", userId, relationshipId);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public List<DoctorServiceResponse> getMyServices() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfile.getServices().stream()
                .map(serviceRelationshipMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorSpecialtyResponse addSpecialtyToProfile(DoctorSpecialtyRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Specialty specialty = specialtyRepository
                .findById(request.getSpecialtyId())
                .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        boolean alreadyExists = doctorProfile.getSpecialties().stream()
                .anyMatch(rel -> rel.getSpecialty().getId().equals(request.getSpecialtyId()));

        if (alreadyExists) {
            throw new AppException(ErrorCode.SPECIALTY_ALREADY_ADDED);
        }

        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            doctorProfile.getSpecialties().forEach(rel -> rel.setIsPrimary(false));
        }

        DoctorSpecialtyRelationship relationship = specialtyRelationshipMapper.toRelationship(request);
        relationship.setSpecialty(specialty);

        doctorProfile.getSpecialties().add(relationship);
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // Publish CDC event for specialty creation
        Map<String, Object> afterState = cdcMapper.toSpecialtyMap(relationship);
        cdcProducer.publishCreate(DoctorProfileEntityType.SPECIALTY, afterState, doctorProfile.getId(), userId);

        log.info("Doctor {} added specialty: {}", userId, specialty.getName());
        return specialtyRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorSpecialtyResponse updateSpecialtyInProfile(Long relationshipId, DoctorSpecialtyRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorSpecialtyRelationship relationship = doctorProfile.getSpecialties().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SPECIALTY_NOT_FOUND));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toSpecialtyMap(relationship);

        if (!relationship.getSpecialty().getId().equals(request.getSpecialtyId())) {
            Specialty newSpecialty = specialtyRepository
                    .findById(request.getSpecialtyId())
                    .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

            boolean alreadyExists = doctorProfile.getSpecialties().stream()
                    .anyMatch(rel -> !rel.getId().equals(relationshipId)
                            && rel.getSpecialty().getId().equals(request.getSpecialtyId()));

            if (alreadyExists) {
                throw new AppException(ErrorCode.SPECIALTY_ALREADY_ADDED);
            }

            relationship.setSpecialty(newSpecialty);
        }

        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            doctorProfile.getSpecialties().forEach(rel -> {
                if (!rel.getId().equals(relationshipId)) {
                    rel.setIsPrimary(false);
                }
            });
        }

        specialtyRelationshipMapper.updateRelationship(relationship, request);
        doctorProfile = doctorProfileRepository.save(doctorProfile);

        // Capture after state for CDC
        Map<String, Object> afterState = cdcMapper.toSpecialtyMap(relationship);

        // Publish CDC event for specialty update
        cdcProducer.publishUpdate(
                DoctorProfileEntityType.SPECIALTY, beforeState, afterState, doctorProfile.getId(), userId);

        log.info("Doctor {} updated specialty relationship {}", userId, relationshipId);
        return specialtyRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public void removeSpecialtyFromProfile(Long relationshipId) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorSpecialtyRelationship relationshipToRemove = doctorProfile.getSpecialties().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SPECIALTY_NOT_FOUND));

        // Capture before state for CDC
        Map<String, Object> beforeState = cdcMapper.toSpecialtyMap(relationshipToRemove);

        doctorProfile.getSpecialties().remove(relationshipToRemove);
        doctorProfileRepository.save(doctorProfile);

        // Publish CDC event for specialty deletion
        cdcProducer.publishDelete(DoctorProfileEntityType.SPECIALTY, beforeState, doctorProfile.getId(), userId);

        log.info("Doctor {} removed specialty relationship {}", userId, relationshipId);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public List<DoctorSpecialtyResponse> getMySpecialties() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfile.getSpecialties().stream()
                .map(specialtyRelationshipMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    @CacheEvict(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorExperienceResponse updateExperienceMetadata(
            String experienceId, DoctorExperienceUpdateRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorExperienceRelationship relationship = doctorProfile.getPracticeExperience().stream()
                .filter(rel -> rel.getExperience().getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PRACTICE_EXPERIENCE_NOT_OWNED));

        experienceRelationshipMapper.updateRelationshipMetadata(relationship, request);
        doctorProfileRepository.save(doctorProfile);

        log.info("Doctor {} updated experience metadata for {}", userId, experienceId);
        return experienceRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public List<DoctorExperienceResponse> getMyExperiences() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfile.getPracticeExperience().stream()
                .map(experienceRelationshipMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Cacheable(
            value = "DOCTOR_PROFILE_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public DoctorProfileResponse getMyDoctorProfile() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfile basicUserProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        log.info("Fetching doctor profile from database for userId: {}", userId);
        return doctorProfileMapper.toResponse(doctorProfile, basicUserProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<DoctorProfileResponse> getAllDoctorProfile(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // Step 1: Paginate DoctorProfiles
        var paginatedDoctorProfiles = doctorProfileRepository.findAllBy(pageable);

        // Step 2: Extract userIds from paginated results
        Set<String> doctorUserIds = paginatedDoctorProfiles.getContent().stream()
                .map(DoctorProfile::getUserId)
                .collect(Collectors.toSet());

        // Step 3: Fetch corresponding UserProfiles (non-paginated, just the needed ones)
        List<UserProfile> baseDoctorProfiles = userProfileRepository.findAllByUserIdIn(doctorUserIds);

        // Step 4: Create lookup map
        Map<String, UserProfile> baseDoctorProfileHashMap = baseDoctorProfiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));

        // Step 5: Build response with paginated DoctorProfiles
        return PageResponse.<DoctorProfileResponse>builder()
                .currentPage(page)
                .pageSize(paginatedDoctorProfiles.getSize())
                .totalElements(paginatedDoctorProfiles.getTotalElements())
                .totalPages(paginatedDoctorProfiles.getTotalPages())
                .data(paginatedDoctorProfiles.getContent().stream()
                        .map(doctorProfile -> {
                            UserProfile userProfile = baseDoctorProfileHashMap.get(doctorProfile.getUserId());
                            return doctorProfileMapper.toResponse(doctorProfile, userProfile);
                        })
                        .toList())
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_DOCTOR_PROFILES')")
    @Cacheable(value = "DOCTOR_PROFILE_CACHE", key = "#userId")
    public DoctorProfileResponse getOneDoctorProfile(String userId) {
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfile basicUserProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        log.info("Fetching doctor profile from database for userId: {}", userId);
        return doctorProfileMapper.toResponse(doctorProfile, basicUserProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "DOCTOR_PROFILE_CACHE", key = "#userId")
    public DoctorProfileResponse adminUpdateOneDoctorProfile(String userId, DoctorProfileUpdateRequest request) {
        return updateBaseDoctorProfile(userId, request);
    }

    private DoctorProfileResponse updateBaseDoctorProfile(String userId, DoctorProfileUpdateRequest request) {
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Map<String, Object> beforeState = cdcMapper.toProfileMap(doctorProfile);

        doctorProfileMapper.updateDoctorBaseProfile(doctorProfile, request);

        doctorProfileRepository.save(doctorProfile);

        Map<String, Object> afterState = cdcMapper.toProfileMap(doctorProfile);

        cdcProducer.publishUpdate(
                DoctorProfileEntityType.PROFILE,
                beforeState,
                afterState,
                doctorProfile.getId(),
                doctorProfile.getUserId());

        UserProfile basicUserProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfileMapper.toResponse(doctorProfile, basicUserProfile);
    }
}
