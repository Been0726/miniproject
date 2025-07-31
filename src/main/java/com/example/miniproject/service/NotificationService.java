package com.example.miniproject.service;

import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.Notification;
import com.example.miniproject.repository.MemberRepository;
import com.example.miniproject.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    public void notifyMatch(Member target, String content) {
        Notification notification = new Notification();
        notification.setTarget(target);
        notification.setContent(content);
        notification.setRead(false);
        notification.setRegTime(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // 읽음 처리
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // 삭제 처리
    @Transactional
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
