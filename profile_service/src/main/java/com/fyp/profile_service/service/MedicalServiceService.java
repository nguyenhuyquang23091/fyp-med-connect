package com.fyp.profile_service.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fyp.profile_service.dto.request.MedicalServiceRequest;
import com.fyp.profile_service.dto.response.MedicalServiceResponse;
import com.fyp.profile_service.entity.MedicalService;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.MedicalServiceMapper;
import com.fyp.profile_service.repository.MedicalServiceRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicalServiceService {

    MedicalServiceRepository medicalServiceRepository;
    MedicalServiceMapper medicalServiceMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MedicalServiceResponse createMedicalService(MedicalServiceRequest request) {
        if (medicalServiceRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.MEDICAL_SERVICE_NAME_EXISTED);
        }

        MedicalService service = medicalServiceMapper.toMedicalService(request);
        service = medicalServiceRepository.save(service);

        log.info("Created medical service: {}", service.getName());
        return medicalServiceMapper.toMedicalServiceResponse(service);
    }

    public List<MedicalServiceResponse> getAllMedicalServices() {
        List<MedicalService> services = medicalServiceRepository.findAll();
        return services.stream()
                .map(medicalServiceMapper::toMedicalServiceResponse)
                .toList();
    }

    public MedicalServiceResponse getMedicalServiceById(String id) {
        MedicalService service = medicalServiceRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND));
        return medicalServiceMapper.toMedicalServiceResponse(service);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MedicalServiceResponse updateMedicalService(String id, MedicalServiceRequest request) {
        MedicalService service = medicalServiceRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND));

        if (!service.getName().equals(request.getName()) && medicalServiceRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.MEDICAL_SERVICE_NAME_EXISTED);
        }

        medicalServiceMapper.updateMedicalService(service, request);
        service = medicalServiceRepository.save(service);

        log.info("Updated medical service: {} with id: {}", service.getName(), service.getId());
        return medicalServiceMapper.toMedicalServiceResponse(service);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteMedicalService(String id) {
        if (!medicalServiceRepository.existsById(id)) {
            throw new AppException(ErrorCode.MEDICAL_SERVICE_NOT_FOUND);
        }

        medicalServiceRepository.deleteById(id);
        log.info("Deleted medical service with id: {}", id);
    }
}
