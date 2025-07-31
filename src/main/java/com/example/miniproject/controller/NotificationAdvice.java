package com.example.miniproject.controller;

import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.entity.Notification;
import com.example.miniproject.repository.NotificationRepository;
import com.example.miniproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationAdvice {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @ModelAttribute("notifications")
    public List<Notification> notifications(@AuthenticationPrincipal Object principal) {
        if (principal instanceof CustomUserDetails custom) {
            return notificationRepository.findAllByTargetOrderByRegTimeDesc(custom.getMember());
        } else if (principal instanceof OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            return notificationRepository.findAllByTargetOrderByRegTimeDesc(
                    notificationService.getMemberByEmail(email));
        }
        return List.of();
    }

    @ModelAttribute("notificationCount")
    public Long unreadCount(@AuthenticationPrincipal Object principal) {
        if (principal instanceof CustomUserDetails custom) {
            return notificationRepository.countByTargetAndReadFalse(custom.getMember());
        } else if (principal instanceof OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            return notificationRepository.countByTargetAndReadFalse(
                    notificationService.getMemberByEmail(email));
        }
        return 0L;
    }

//    @ModelAttribute("notifications")
//    public List<Notification> notifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        if (userDetails == null) return List.of();
//        return notificationRepository.findAllByTargetOrderByRegTimeDesc(userDetails.getMember());
//    }
//
//    @ModelAttribute("notificationCount")
//    public Long unreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        if (userDetails == null) return 0L;
//        return notificationRepository.countByTargetAndReadFalse(userDetails.getMember());
//    }


}
