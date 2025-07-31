package com.example.miniproject.repository;

import com.example.miniproject.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByMatchIdOrderByRegTimeAsc(Long matchId);
}
