package com.example.miniproject.repository;

import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 목록
    List<Notification> findAllByTargetOrderByRegTimeDesc(Member target);

    // 알림 숫자
    long countByTargetAndReadFalse(Member member);

}
