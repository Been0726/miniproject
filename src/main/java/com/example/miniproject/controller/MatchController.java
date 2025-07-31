package com.example.miniproject.controller;

import com.example.miniproject.dto.RatingDto;
import com.example.miniproject.entity.FutsalSpot;
import com.example.miniproject.entity.MatchRequest;
import com.example.miniproject.entity.MatchSlot;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.FutsalSpotRepository;
import com.example.miniproject.repository.MatchRatingRepository;
import com.example.miniproject.repository.MemberRepository;
import com.example.miniproject.service.MatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final FutsalSpotRepository futsalSpotRepository;
    private final MemberRepository memberRepository;
    private final MatchRatingRepository matchRatingRepository;

    @PostMapping("/match")
    public String submitMatch(@AuthenticationPrincipal(expression = "member") Member member,
                              @RequestParam Long spotId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                              Model model,
                              RedirectAttributes rttr) {

        FutsalSpot spot = futsalSpotRepository.findById(spotId).orElseThrow();

        try {
            MatchRequest request = matchService.submitRequest(member, spot, date, startTime, endTime);

            model.addAttribute("requests", request);
            model.addAttribute("slot", request.getMatchSlot());
            model.addAttribute("spot", spot);

            if (request.getMatched()) {
                MatchSlot slot = request.getMatchSlot();
                model.addAttribute("slot", slot);
                model.addAttribute("slotId", slot.getId());
                return "match/success";
            } else {
                return "match/requested";
            }
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("msg", e.getMessage());
            return "redirect:/point/charge";
        }
    }

    @GetMapping("/myMatches")
    public String viewMatches(Model model, @AuthenticationPrincipal(expression = "member") Member member) {

        List<MatchRequest> requests = matchService.getMatchRequestsForUser(member);
        System.out.println(">>> 요청 개수 : " + requests.size());
        System.out.println(">>> 로그인한 사용자: " + member.getId() + ", " + member.getNickname());


        List<Long> ratedMatchIds = matchRatingRepository.findAllByRater(member)
                .stream()
                .map(r -> r.getMatchRequest().getId())
                .collect(Collectors.toList());

        Map<String, List<RatingDto>> nicknameAllRatings = matchService.getAllRatingsByOpponentNickname(requests);

        model.addAttribute("requests", requests);
        model.addAttribute("ratedMatchIds", ratedMatchIds);
        model.addAttribute("nicknameAllRatings", nicknameAllRatings);

        return "match/list";
    }
}
