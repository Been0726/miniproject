package com.example.miniproject.repository;

import com.example.miniproject.entity.MatchRating;
import com.example.miniproject.entity.MatchRequest;
import com.example.miniproject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRatingRepository extends JpaRepository<MatchRating, Long> {

    // 평가 중복 여부 확인
    Optional<MatchRating> findByMatchRequestAndRater(MatchRequest matchRequest, Member rater);

    List<MatchRating> findAllByRater(Member rater);

    List<MatchRating> findByTarget(Member target);

    List<MatchRating> findAllByTarget(Member target);

    @Query("SELECT AVG(m.score) FROM MatchRating m WHERE m.target = :target")
    Double findAverageScoreByTarget(@Param("target") Member target);
}
