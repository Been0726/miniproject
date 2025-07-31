package com.example.miniproject.controller;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.constant.MatchStatus;
import com.example.miniproject.dto.ChatMessageDto;
import com.example.miniproject.entity.ChatMessage;
import com.example.miniproject.entity.MatchRequest;
import com.example.miniproject.entity.MatchSlot;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.ChatMessageRepository;
import com.example.miniproject.repository.MatchRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate template;
    private final MatchRequestRepository matchRequestRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 채팅 메세지 수신 처리
    @MessageMapping("/chat/{slotId}")
    public void sendMessage(@DestinationVariable Long slotId, ChatMessage message) {
        // 저장
        ChatMessage saved = ChatMessage.builder()
                .matchId(slotId)
                .sender(message.getSender())
                .content(message.getContent())
                .build();

        chatMessageRepository.save(saved);
        // 실시간
        template.convertAndSend("/topic/chat/" + slotId, message);
    }

    // 채팅방 입장
    @GetMapping("/chat/slot/{slotId}")
    public String enterChatBySlot(@PathVariable Long slotId,
                                  @AuthenticationPrincipal Object principal,
                                  Model model) {
        Member me = null;
        if (principal instanceof CustomUserDetails userDetails) {
            me = userDetails.getMember();
        } else if (principal instanceof CustomOAuth2User oAuth2User) {
            me = oAuth2User.getMember();
        }

        if (me == null) return "redirect:/members/login";

        MatchRequest match = matchRequestRepository
                .findByMatchSlotIdAndMemberId(slotId, me.getId()).orElse(null);

        if (match == null || match.getStatus() != MatchStatus.CONFIRMED) {
            return "redirect:/";
        }

        List<ChatMessage> messageHistory = chatMessageRepository.findByMatchIdOrderByRegTimeAsc(slotId);
        model.addAttribute("slotId", slotId);
        model.addAttribute("nickname", me.getNickname());
        model.addAttribute("messages", messageHistory);

        MatchSlot slot = match.getMatchSlot();
        String spotName = slot.getSpot().getName();

        String formattedDate = slot.getDate().format(DateTimeFormatter.ofPattern("M월 d일"));
        String formattedStart = slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String formattedEnd = slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        String title = spotName + " - " + formattedDate + " "
                + formattedStart + " ~ " + formattedEnd + " 경기";
        model.addAttribute("chatRoomTitle", title);

        return "match/matchRoom";
    }
}
