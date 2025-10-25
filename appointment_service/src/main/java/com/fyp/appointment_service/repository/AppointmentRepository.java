package com.fyp.appointment_service.repository;

import com.fyp.appointment_service.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, String> {
    List<AppointmentEntity> findAllByDoctorId(String doctorId);
    List<AppointmentEntity> findAllByUserId(String userId);

    Optional<AppointmentEntity> findByUserId(String userId);
}
