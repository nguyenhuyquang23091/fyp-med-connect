package com.fyp.statistic_service.service;

import com.fyp.statistic_service.dto.response.UserStatisticResponse;
import com.fyp.statistic_service.entity.UserStatistic;
import com.fyp.statistic_service.mapper.UserStatisticMapper;
import com.fyp.statistic_service.repository.UserStatisticRepository;
import event.dto.UserEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserStatisticService {

    UserStatisticRepository userStatisticRepository;
    UserStatisticMapper userStatisticMapper;
    SocketIOEventEmitter socketIOEventEmitter;


    @Transactional
    @CacheEvict(value = {"USER_STATISTICS_CACHE", "DASHBOARD_SUMMARY_CACHE", "USER_TREND_CACHE"}, allEntries = true)
    public void processUserCreatedEvent(UserEvent userEvent) {
        LocalDate today = LocalDate.now();

        UserStatistic statistic = userStatisticRepository.findByDate(today)
                .orElseGet(() -> createInitialStatistic(today));

        statistic.setNewUsers(statistic.getNewUsers() + 1);
        statistic.setTotalUsers(statistic.getTotalUsers() + 1);
        statistic.setActiveUsers(statistic.getActiveUsers() + 1);

        updateRoleCounters(statistic, userEvent, true);

        UserStatistic savedStatistic = userStatisticRepository.save(statistic);
        log.info("Updated user statistics for date: {} - New user: {}", today, userEvent.getUserId());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("date", today.toString());
        eventData.put("totalUsers", savedStatistic.getTotalUsers());
        eventData.put("newUsers", savedStatistic.getNewUsers());
        eventData.put("activeUsers", savedStatistic.getActiveUsers());
        eventData.put("doctorCount", savedStatistic.getDoctorCount());
        eventData.put("patientCount", savedStatistic.getPatientCount());

        socketIOEventEmitter.emitUserStatisticUpdate(eventData);
    }


    @Transactional
    public void recordUserActivity(String userId, java.util.Set<String> roles) {
        LocalDate today = LocalDate.now();

        UserStatistic statistic = userStatisticRepository.findByDate(today)
                .orElseGet(() -> createInitialStatistic(today));

        // Increment active users if not already counted today
        // Reason: In a production system, you'd track individual user IDs per day in a separate table
        // For now, we increment based on activity events
        statistic.setActiveUsers(statistic.getActiveUsers() + 1);

        userStatisticRepository.save(statistic);
        log.debug("Recorded user activity for date: {} - User: {}", today, userId);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "USER_STATISTICS_CACHE", key = "#date.toString()")
    public UserStatisticResponse getStatisticsByDate(LocalDate date) {
        UserStatistic entity = userStatisticRepository.findByDate(date)
                .orElseThrow(() -> new RuntimeException("No statistics found for date: " + date));
        return userStatisticMapper.toResponse(entity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "USER_STATISTICS_CACHE", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<UserStatisticResponse> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<UserStatistic> entities = userStatisticRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
        return userStatisticMapper.toResponseList(entities);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "USER_STATISTICS_CACHE", key = "'daily_active_' + #date.toString()")
    public Long getDailyActiveUsers(LocalDate date) {
        return userStatisticRepository.findByDate(date)
                .map(UserStatistic::getActiveUsers)
                .orElse(0L);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'weekly_active_users'")
    public Double getWeeklyActiveUsers() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return userStatisticRepository.avgActiveUsersByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'monthly_active_users'")
    public Double getMonthlyActiveUsers() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return userStatisticRepository.avgActiveUsersByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "USER_TREND_CACHE", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<UserStatisticResponse> getUserGrowthTrend(LocalDate startDate, LocalDate endDate) {
        List<UserStatistic> entities = userStatisticRepository.findUserGrowthTrend(startDate, endDate);
        return userStatisticMapper.toResponseList(entities);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "USER_STATISTICS_CACHE", key = "'new_users_' + #startDate.toString() + '_' + #endDate.toString()")
    public Long getNewUsersByDateRange(LocalDate startDate, LocalDate endDate) {
        return userStatisticRepository.sumNewUsersByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'latest_total_users'")
    public Long getLatestTotalUsers() {
        return userStatisticRepository.findLatestTotalUsers().orElse(0L);
    }


    private UserStatistic createInitialStatistic(LocalDate date) {
        // Get total users from previous day's record to maintain continuity
        Long previousTotalUsers = userStatisticRepository.findByDate(date.minusDays(1))
                .map(UserStatistic::getTotalUsers)
                .orElse(0L);

        return UserStatistic.builder()
                .date(date)
                .totalUsers(previousTotalUsers)
                .newUsers(0L)
                .activeUsers(0L)
                .doctorCount(0L)
                .patientCount(0L)
                .adminCount(0L)
                .build();
    }

    private void updateRoleCounters(UserStatistic statistic, UserEvent userEvent, boolean increment) {
        int delta = increment ? 1 : -1;

        if (userEvent.getRoles() != null) {
            for (String role : userEvent.getRoles()) {
                switch (role.toUpperCase()) {
                    case "DOCTOR":
                        statistic.setDoctorCount(statistic.getDoctorCount() + delta);
                        break;
                    case "PATIENT":
                        statistic.setPatientCount(statistic.getPatientCount() + delta);
                        break;
                    case "ADMIN":
                        statistic.setAdminCount(statistic.getAdminCount() + delta);
                        break;
                    default:
                        log.warn("Unknown role encountered: {}", role);
                }
            }
        }
    }
}
