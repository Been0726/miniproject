package com.example.miniproject.repository;

import com.example.miniproject.entity.FutsalSpot;
import com.example.miniproject.entity.MatchSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MatchSlotRepository extends JpaRepository<MatchSlot, Long> {

    List<MatchSlot> findBySpotIdAndDate(Long spotId, LocalDate date);

    Optional<MatchSlot> findBySpotAndDateAndStartTimeAndEndTime
            (FutsalSpot spot, LocalDate date, LocalTime startTime, LocalTime endTime);

    List<MatchSlot> findAllBySpotIdAndDate(Long spotId, LocalDate date);

    Optional<MatchSlot> findOptionalBySpotAndDateAndStartTimeAndEndTime(FutsalSpot spot, LocalDate date, LocalTime startTime, LocalTime endTime);

}
