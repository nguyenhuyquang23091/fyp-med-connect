package com.fyp.video_call_service.repository;


import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.entity.SessionVideoCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionVideoCallRepository extends JpaRepository<SessionVideoCall, String> {

    Optional<List<SessionVideoCall>> findBySessionStatusAndScheduledStartTimeBefore(SessionStatus sessionStatus, LocalDateTime scheduledStartTimeBefore);

}
