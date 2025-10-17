package com.fyp.profile_service.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fyp.profile_service.dto.request.DoctorExperienceUpdateRequest;
import com.fyp.profile_service.dto.request.DoctorServiceRequest;
import com.fyp.profile_service.dto.request.DoctorSpecialtyRequest;
import com.fyp.profile_service.dto.response.DoctorExperienceResponse;
import com.fyp.profile_service.dto.response.DoctorProfileResponse;
import com.fyp.profile_service.dto.response.DoctorServiceResponse;
import com.fyp.profile_service.dto.response.DoctorSpecialtyResponse;
import com.fyp.profile_service.entity.*;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.DoctorExperienceRelationshipMapper;
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

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
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
        doctorProfileRepository.save(doctorProfile);

        log.info("Doctor {} added service: {}", userId, service.getName());
        return serviceRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public DoctorServiceResponse updateServiceInProfile(Long relationshipId, DoctorServiceRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorServiceRelationship relationship = doctorProfile.getServices().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SERVICE_NOT_FOUND));

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
        doctorProfileRepository.save(doctorProfile);

        log.info("Doctor {} updated service relationship {}", userId, relationshipId);
        return serviceRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public void removeServiceFromProfile(Long relationshipId) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorServiceRelationship relationshipToRemove = doctorProfile.getServices().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SERVICE_NOT_FOUND));

        doctorProfile.getServices().remove(relationshipToRemove);
        doctorProfileRepository.save(doctorProfile);

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
        doctorProfileRepository.save(doctorProfile);

        log.info("Doctor {} added specialty: {}", userId, specialty.getName());
        return specialtyRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public DoctorSpecialtyResponse updateSpecialtyInProfile(Long relationshipId, DoctorSpecialtyRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorSpecialtyRelationship relationship = doctorProfile.getSpecialties().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SPECIALTY_NOT_FOUND));

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
        doctorProfileRepository.save(doctorProfile);

        log.info("Doctor {} updated specialty relationship {}", userId, relationshipId);
        return specialtyRelationshipMapper.toResponse(relationship);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public void removeSpecialtyFromProfile(Long relationshipId) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        DoctorSpecialtyRelationship relationshipToRemove = doctorProfile.getSpecialties().stream()
                .filter(rel -> rel.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_SPECIALTY_NOT_FOUND));

        doctorProfile.getSpecialties().remove(relationshipToRemove);
        doctorProfileRepository.save(doctorProfile);

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
    public DoctorProfileResponse getMyDoctorProfile() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfile basicUserProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfileMapper.toResponse(doctorProfile, basicUserProfile);
    }

    @PreAuthorize("hasAuthority('VIEW_DOCTOR_PROFILES')")
    public List<DoctorProfileResponse> getAllDoctorProfile() {

        List<DoctorProfile> doctorProfiles = doctorProfileRepository.findAll();

        Set<String> doctorProfileUserId =
                doctorProfiles.stream().map(DoctorProfile::getUserId).collect(Collectors.toSet());

        List<UserProfile> baseDoctorProfiles = userProfileRepository.findAllByUserIdIn(doctorProfileUserId);

        Map<String, UserProfile> baseDoctorProfileHashMap =
                baseDoctorProfiles.stream().collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));

        return doctorProfiles.stream()
                .map(doctorProfile -> {
                    UserProfile userProfile = baseDoctorProfileHashMap.get(doctorProfile.getUserId());
                    return doctorProfileMapper.toResponse(doctorProfile, userProfile);
                })
                .toList();
    }

    @PreAuthorize("hasAuthority('VIEW_DOCTOR_PROFILES')")
    public DoctorProfileResponse getOneDoctorProfile(String userId) {
        DoctorProfile doctorProfile = doctorProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfile basicUserProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return doctorProfileMapper.toResponse(doctorProfile, basicUserProfile);
    }
}
