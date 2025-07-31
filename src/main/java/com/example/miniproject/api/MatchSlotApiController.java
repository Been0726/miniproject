package com.example.miniproject.api;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.dto.MatchSlotDto;
import com.example.miniproject.entity.MatchRequest;
import com.example.miniproject.entity.MatchSlot;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MatchRequestRepository;
import com.example.miniproject.repository.MatchSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MatchSlotApiController {

    private final MatchSlotRepository matchSlotRepository;
    private final MatchRequestRepository matchRequestRepository;

    @GetMapping("/match-times")
    public List<MatchSlotDto> getSlots(@RequestParam Long spotId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @AuthenticationPrincipal Object principal) {

        Member me;
        if (principal instanceof CustomUserDetails) {
            me = ((CustomUserDetails) principal).getMember();
        } else if (principal instanceof CustomOAuth2User) {
            me = ((CustomOAuth2User) principal).getMember();
        } else {
            throw new IllegalStateException("로그인 정보가 유효하지 않습니다.");
        }

        List<MatchSlot> slots = matchSlotRepository.findAllBySpotIdAndDate(spotId, date);

        return slots.stream().map(slot -> {
            List<MatchRequest> requests = matchRequestRepository.findByMatchSlot(slot);

            boolean matched = slot.isMatched();
            boolean mine = requests.stream()
                    .anyMatch(req -> req.getMember().getId().equals(me.getId()));
            boolean others = requests.stream()
                    .anyMatch(req -> !req.getMember().getId().equals(me.getId()));

            String status;
            if (matched) {
                status = "BLOCKED"; // 회색
            } else if (mine) {
                status = "MINE"; // 파랑
            } else if (others) {
                status = "REQUESTED"; // 초록 (상대 대기 중)
            } else {
                status = "AVAILABLE"; // 기본 파랑
            }

            return new MatchSlotDto(
                    slot.getStartTime().toString(),
                    slot.getEndTime().toString(),
                    matched,
                    others, // requested = 상대방 신청 여부
                    status,
                    mine
            );
        }).collect(Collectors.toList());
    }
}
