package com.example.miniproject.repository;

import com.example.miniproject.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByMemberIdOrderByRegTimeDesc(Long memberId);
}
