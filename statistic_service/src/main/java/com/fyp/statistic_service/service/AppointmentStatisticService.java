package com.fyp.statistic_service.service;

import com.fyp.statistic_service.dto.response.AppointmentStatisticResponse;
import com.fyp.statistic_service.entity.AppointmentStatistic;
import com.fyp.statistic_service.mapper.AppointmentStatisticMapper;
import com.fyp.statistic_service.repository.AppointmentStatisticRepository;
import event.dto.AppointmentEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentStatisticService {

    AppointmentStatisticRepository appointmentStatisticRepository;
    AppointmentStatisticMapper appointmentStatisticMapper;
    SocketIOEventEmitter socketIOEventEmitter;


    @Transactional
    @CacheEvict(value = {"APPOINTMENT_STATISTICS_CACHE", "DASHBOARD_SUMMARY_CACHE", "APPOINTMENT_TREND_CACHE"}, allEntries = true)
    public void processAppointmentCreatedEvent(AppointmentEvent appointmentEvent) {
        LocalDate today = LocalDate.now();

        AppointmentStatistic statistic = appointmentStatisticRepository.findByDate(today)
                .orElseGet(() -> createInitialStatistic(today));

        statistic.setTotalAppointments(statistic.getTotalAppointments() + 1);
        updateStatusCounters(statistic, appointmentEvent.getAppointmentStatus(), true);
        updateConsultationTypeBreakdown(statistic, appointmentEvent.getConsultationType(), true);

        if (appointmentEvent.getSpecialty() != null) {
            updateDepartmentBreakdown(statistic, appointmentEvent.getSpecialty(), true);
        }

        recalculateCancellationRate(statistic);

        AppointmentStatistic savedStatistic = appointmentStatisticRepository.save(statistic);
        log.info("Updated appointment statistics for date: {} - Appointment: {}", today, appointmentEvent.getAppointmentId());

        Map<String, Object> eventData = getStringObjectMap(today, savedStatistic);

        socketIOEventEmitter.emitAppointmentStatisticUpdate(eventData);
    }

    private static Map<String, Object> getStringObjectMap(LocalDate today, AppointmentStatistic savedStatistic) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("date", today.toString());
        eventData.put("totalAppointments", savedStatistic.getTotalAppointments());
        eventData.put("completedAppointments", savedStatistic.getCompletedAppointments());
        eventData.put("cancelledAppointments", savedStatistic.getCancelledAppointments());
        eventData.put("pendingAppointments", savedStatistic.getPendingAppointments());
        eventData.put("cancellationRate", savedStatistic.getCancellationRate());
        return eventData;
    }


    @Transactional
    public void processAppointmentStatusUpdate(String appointmentId, String oldStatus, String newStatus) {
        LocalDate today = LocalDate.now();

        AppointmentStatistic statistic = appointmentStatisticRepository.findByDate(today)
                .orElseGet(() -> createInitialStatistic(today));

        // Decrement old status counter
        updateStatusCounters(statistic, oldStatus, false);

        // Increment new status counter
        updateStatusCounters(statistic, newStatus, true);

        // Recalculate cancellation rate
        recalculateCancellationRate(statistic);

        appointmentStatisticRepository.save(statistic);
        log.info("Updated appointment status for date: {} - Appointment: {} ({} -> {})",
                today, appointmentId, oldStatus, newStatus);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "APPOINTMENT_STATISTICS_CACHE", key = "#date.toString()")
    public AppointmentStatisticResponse getStatisticsByDate(LocalDate date) {
        AppointmentStatistic entity = appointmentStatisticRepository.findByDate(date)
                .orElseThrow(() -> new RuntimeException("No statistics found for date: " + date));
        return appointmentStatisticMapper.toResponse(entity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "APPOINTMENT_STATISTICS_CACHE", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<AppointmentStatisticResponse> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AppointmentStatistic> entities = appointmentStatisticRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
        return appointmentStatisticMapper.toResponseList(entities);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'total_appointments_' + #startDate.toString() + '_' + #endDate.toString()")
    public Long getTotalAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentStatisticRepository.sumTotalAppointmentsByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "APPOINTMENT_STATISTICS_CACHE", key = "'completed_' + #startDate.toString() + '_' + #endDate.toString()")
    public Long getCompletedAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentStatisticRepository.sumCompletedAppointmentsByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "APPOINTMENT_STATISTICS_CACHE", key = "'cancelled_' + #startDate.toString() + '_' + #endDate.toString()")
    public Long getCancelledAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentStatisticRepository.sumCancelledAppointmentsByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "APPOINTMENT_STATISTICS_CACHE", key = "'pending_' + #startDate.toString() + '_' + #endDate.toString()")
    public Long getPendingAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentStatisticRepository.sumPendingAppointmentsByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'avg_cancellation_rate'")
    public BigDecimal getAvgCancellationRate(LocalDate startDate, LocalDate endDate) {
        return appointmentStatisticRepository.avgCancellationRateByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "APPOINTMENT_TREND_CACHE", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<AppointmentStatisticResponse> getCancellationRateTrend(LocalDate startDate, LocalDate endDate) {
        List<AppointmentStatistic> entities = appointmentStatisticRepository.findCancellationRateTrend(startDate, endDate);
        return appointmentStatisticMapper.toResponseList(entities);
    }


    private AppointmentStatistic createInitialStatistic(LocalDate date) {
        return AppointmentStatistic.builder()
                .date(date)
                .totalAppointments(0L)
                .completedAppointments(0L)
                .cancelledAppointments(0L)
                .pendingAppointments(0L)
                .inProgressAppointments(0L)
                .cancellationRate(BigDecimal.ZERO)
                .consultationTypeBreakdown(new HashMap<>())
                .departmentBreakdown(new HashMap<>())
                .build();
    }


    private void updateStatusCounters(AppointmentStatistic statistic, String status, boolean increment) {
        int delta = increment ? 1 : -1;

        if (status == null) {
            log.warn("Appointment status is null, defaulting to PENDING");
            status = "PENDING";
        }

        switch (status.toUpperCase()) {
            case "COMPLETED":
                statistic.setCompletedAppointments(statistic.getCompletedAppointments() + delta);
                break;
            case "CANCELLED":
                statistic.setCancelledAppointments(statistic.getCancelledAppointments() + delta);
                break;
            case "PENDING":
                statistic.setPendingAppointments(statistic.getPendingAppointments() + delta);
                break;
            case "IN_PROGRESS":
            case "ONGOING":
                statistic.setInProgressAppointments(statistic.getInProgressAppointments() + delta);
                break;
            default:
                log.warn("Unknown appointment status: {}", status);
                statistic.setPendingAppointments(statistic.getPendingAppointments() + delta);
        }
    }


    private void updateConsultationTypeBreakdown(AppointmentStatistic statistic, String consultationType, boolean increment) {
        if (consultationType == null) {
            consultationType = "UNKNOWN";
        }

        Map<String, Long> breakdown = statistic.getConsultationTypeBreakdown();
        if (breakdown == null) {
            breakdown = new HashMap<>();
            statistic.setConsultationTypeBreakdown(breakdown);
        }

        Long currentCount = breakdown.getOrDefault(consultationType, 0L);
        breakdown.put(consultationType, currentCount + (increment ? 1 : -1));
    }


    private void updateDepartmentBreakdown(AppointmentStatistic statistic, String department, boolean increment) {
        Map<String, Long> breakdown = statistic.getDepartmentBreakdown();
        if (breakdown == null) {
            breakdown = new HashMap<>();
            statistic.setDepartmentBreakdown(breakdown);
        }

        Long currentCount = breakdown.getOrDefault(department, 0L);
        breakdown.put(department, currentCount + (increment ? 1 : -1));
    }


    private void recalculateCancellationRate(AppointmentStatistic statistic) {
        if (statistic.getTotalAppointments() > 0) {
            statistic.setCancellationRate(
                    BigDecimal.valueOf(statistic.getCancelledAppointments())
                            .divide(BigDecimal.valueOf(statistic.getTotalAppointments()), 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
            );
        } else {
            statistic.setCancellationRate(BigDecimal.ZERO);
        }
    }
}
