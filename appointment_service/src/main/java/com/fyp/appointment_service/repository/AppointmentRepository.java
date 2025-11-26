package com.fyp.appointment_service.repository;

import com.fyp.appointment_service.constant.AppointmentStatus;
import com.fyp.appointment_service.constant.ConsultationType;
import com.fyp.appointment_service.entity.AppointmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, String> {
    List<AppointmentEntity> findAllByDoctorId(String doctorId);
    List<AppointmentEntity> findAllByUserId(String userId);

    
    Page<AppointmentEntity> findAllByUserId(String userId, Pageable pageable);

    
    Page<AppointmentEntity> findAllByDoctorId(String doctorId, Pageable pageable);

    Optional<AppointmentEntity> findByUserId(String userId);

    Optional<AppointmentEntity> findByUserIdAndId(String userId, String id);


    Page<AppointmentEntity> findByUserIdAndAppointmentDateTimeAfterAndAppointmentStatus
            (String userId,
             LocalDateTime
                     appointmentDateTimeAfter,
             AppointmentStatus appointmentStatus, Pageable pageable);



}
