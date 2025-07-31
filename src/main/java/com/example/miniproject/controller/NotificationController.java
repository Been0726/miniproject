package com.example.miniproject.controller;

import com.example.miniproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 읽음 처리
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 삭제 처리
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
