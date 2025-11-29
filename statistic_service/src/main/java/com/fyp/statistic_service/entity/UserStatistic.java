package com.fyp.statistic_service.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_statistics", indexes = {
        @Index(name = "idx_user_stats_date", columnList = "date")
})
public class UserStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    LocalDate date;

    @Column(nullable = false)
    @Builder.Default
    Long totalUsers = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long newUsers = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long activeUsers = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long doctorCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long patientCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long adminCount = 0L;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
