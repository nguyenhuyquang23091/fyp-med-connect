package com.fyp.profile_service.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fyp.profile_service.dto.request.SpecialtyRequest;
import com.fyp.profile_service.dto.response.SpecialtyResponse;
import com.fyp.profile_service.entity.Specialty;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.SpecialtyMapper;
import com.fyp.profile_service.repository.SpecialtyRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpecialtyService {

    SpecialtyRepository specialtyRepository;
    SpecialtyMapper specialtyMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        if (specialtyRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.SPECIALTY_CODE_EXISTED);
        }

        if (specialtyRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SPECIALTY_NAME_EXISTED);
        }

        Specialty specialty = specialtyMapper.toSpecialty(request);
        specialty = specialtyRepository.save(specialty);

        log.info("Created specialty: {} with code: {}", specialty.getName(), specialty.getCode());
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    public List<SpecialtyResponse> getAllSpecialties() {
        List<Specialty> specialties = specialtyRepository.findAll();
        return specialties.stream().map(specialtyMapper::toSpecialtyResponse).toList();
    }

    public SpecialtyResponse getSpecialtyById(String id) {
        Specialty specialty =
                specialtyRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    public SpecialtyResponse getSpecialtyByCode(String code) {
        Specialty specialty =
                specialtyRepository.findByCode(code).orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SpecialtyResponse updateSpecialty(String id, SpecialtyRequest request) {
        Specialty specialty =
                specialtyRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        if (!specialty.getCode().equals(request.getCode()) && specialtyRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.SPECIALTY_CODE_EXISTED);
        }

        if (!specialty.getName().equals(request.getName()) && specialtyRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SPECIALTY_NAME_EXISTED);
        }

        specialtyMapper.updateSpecialty(specialty, request);
        specialty = specialtyRepository.save(specialty);

        log.info("Updated specialty: {} with id: {}", specialty.getName(), specialty.getId());
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteSpecialty(String id) {
        if (!specialtyRepository.existsById(id)) {
            throw new AppException(ErrorCode.SPECIALTY_NOT_FOUND);
        }

        specialtyRepository.deleteById(id);
        log.info("Deleted specialty with id: {}", id);
    }
}
