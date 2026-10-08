package com.campusforum.repository;

import com.campusforum.model.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {
    List<EventAttendee> findByEventId(Long eventId);
    List<EventAttendee> findByUserId(Long userId);
    Optional<EventAttendee> findByEventIdAndUserId(Long eventId, Long userId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    void deleteByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EventAttendee ea WHERE ea.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EventAttendee ea WHERE ea.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
