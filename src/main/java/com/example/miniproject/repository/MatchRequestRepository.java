package com.example.miniproject.repository;

import com.example.miniproject.constant.MatchStatus;
import com.example.miniproject.dto.DailyMatchCountDto;
import com.example.miniproject.entity.FutsalSpot;
import com.example.miniproject.entity.MatchRequest;
import com.example.miniproject.entity.MatchSlot;
import com.example.miniproject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByMemberOrderByCreatedByDesc(Member opponent);

    Optional<MatchRequest> findFirstByMatchSlotAndMatchedFalseAndMemberNot
            (MatchSlot matchSlot, Member member);


    Optional<MatchRequest> findByMemberAndMatchSlot(Member me, MatchSlot slot);

    List<MatchRequest> findByMatchSlot(MatchSlot matchSlot);

    @Query("SELECT COUNT(m) FROM MatchRequest m " +
            "WHERE m.matchSlot.spot = :spot AND m.matchSlot.date = :date")
    long countBySpotAndDate(@Param("spot") FutsalSpot spot,
                            @Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM MatchRequest m " +
            "WHERE m.matchSlot.spot = :spot AND m.matchSlot.date = :date AND m.status = :status")
    long countBySpotAndDateAndStatus(@Param("spot") FutsalSpot spot,
                                     @Param("date") LocalDate date,
                                     @Param("status") MatchStatus status);

    @Query("SELECT new com.example.miniproject.dto.DailyMatchCountDto(mr.matchSlot.date, COUNT(mr), 0) " +
            "FROM MatchRequest mr " +
            "WHERE FUNCTION('YEAR', mr.matchSlot.date) = :year AND FUNCTION('MONTH', mr.matchSlot.date) = :month " +
            "GROUP BY mr.matchSlot.date")
    List<DailyMatchCountDto> getDailyRequestCounts(@Param("year") int year,
                                                   @Param("month") int month);

    @Query("SELECT new com.example.miniproject.dto.DailyMatchCountDto(mr.matchSlot.date, 0, COUNT(mr)) " +
            "FROM MatchRequest mr " +
            "WHERE FUNCTION('YEAR', mr.matchSlot.date) = :year AND FUNCTION('MONTH', mr.matchSlot.date) = :month " +
            "AND mr.status = 'CONFIRMED' " +
            "GROUP BY mr.matchSlot.date")
    List<DailyMatchCountDto> getDailyConfirmedCounts(@Param("year") int year,
                                                     @Param("month") int month);

    @Query("""
    SELECT mr.matchSlot.id
    FROM MatchRequest mr
    WHERE mr.matchSlot.spot = :spot
      AND mr.matchSlot.date = :date
      AND mr.status = 'CONFIRMED'
    GROUP BY mr.matchSlot.id
    HAVING COUNT(mr.id) >= 2
    """)
    List<Long> findActuallyConfirmedSlotIds(@Param("spot") FutsalSpot spot,
                                            @Param("date") LocalDate date);


    @Query("""
    SELECT mr.matchSlot.id
    FROM MatchRequest mr
    WHERE mr.matchSlot.date = :date
      AND mr.status = 'CONFIRMED'
    GROUP BY mr.matchSlot.id
    HAVING COUNT(mr.id) >= 2
    """)
    List<Long> findActuallyConfirmedSlotIdsByDate(@Param("date") LocalDate date);

    @Query("""
    SELECT COUNT(mr)
    FROM MatchRequest mr
    WHERE mr.matchSlot.date = :date
    """)
    long countByDate(@Param("date") LocalDate date);

    Optional<MatchRequest> findByMatchSlotIdAndMemberId(Long slotId, Long memberId);
}
