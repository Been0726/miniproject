//package com.example.miniproject.controller;
//
//import com.example.miniproject.config.CustomOAuth2User;
//import com.example.miniproject.config.CustomUserDetails;
//import com.example.miniproject.entity.MatchRating;
//import com.example.miniproject.entity.MatchRequest;
//import com.example.miniproject.entity.Member;
//import com.example.miniproject.repository.MatchRatingRepository;
//import com.example.miniproject.repository.MatchRequestRepository;
//import com.example.miniproject.service.MatchService;
//import com.example.miniproject.service.MemberService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Slf4j
//@Controller
//@RequiredArgsConstructor
//public class MatchRatingController {
//
//    private final MatchRatingRepository matchRatingRepository;
//    private final MatchRequestRepository matchRequestRepository;
//    private final MemberService memberService;
//    private final MatchService matchService;
//
//    @GetMapping("/match/{id}/rate")
//    public String showRatingForm(@PathVariable Long id,
//                                 @AuthenticationPrincipal CustomUserDetails userDetails,
//                                 Model model) {
//
//        MatchRequest matchRequest = matchRequestRepository.findById(id).orElseThrow();
//        Member rater = userDetails.getMember();
//
//        // 자기 자신 평가 방지
//        if (matchRequest.getMember().getId().equals(rater.getId())) {
//            throw new AccessDeniedException("자기 자신은 평가할 수 없습니다.");
//        }
//
//        // 평가 중복 확인
//        if (matchRatingRepository.findByMatchRequestAndRater(matchRequest, rater).isPresent()) {
//            model.addAttribute("msg", "이미 평가를 완료했습니다.");
//            return "redirect:/match/list";
//        }
//
//        Member target = matchRequest.getMember();
//
//        model.addAttribute("match", matchRequest);
//        model.addAttribute("target", target);
//        return "match/rating";
//    }
//
//    @PostMapping("/match/{id}/rate")
//    public String submitRating(@PathVariable Long id,
//                               @RequestParam int score,
//                               @RequestParam(required = false) String comment,
//                               @AuthenticationPrincipal Object principal) {
//
//        Member rater;
//        if (principal instanceof CustomUserDetails customUserDetails) {
//            rater = customUserDetails.getMember();
//        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
//            rater = customOAuth2User.getMember();
//        } else {
//            throw new IllegalStateException("로그인된 사용자만 이용할 수 있습니다.");
//        }
//
//        MatchRequest matchRequest = matchRequestRepository.findById(id).orElseThrow(()
//                -> new IllegalArgumentException("해당 매치를 찾을 수 없습니다: " + id));
//
//
//        Member target;
//        // 평가 대상 결정 (요청자가 평가하면 상대방이 target, 반대도 가능)
//        if (rater.getId().equals(matchRequest.getMember().getId())) {
//            target = matchRequest.getOpponent();
//        } else if (matchRequest.getOpponent() != null && rater.getId().equals(matchRequest.getOpponent().getId())) {
//            target = matchRequest.getMember();
//        } else {
//            throw new AccessDeniedException("이 매칭의 당사자만 평가할 수 있습니다.");
//        }
//
//        // 자기 자신 평가 방지 (혹시라도 동일인 잘못 연결 시)
//        if (rater.getId().equals(target.getId())) {
//            throw new AccessDeniedException("자기 자신은 평가할 수 없습니다.");
//        }
//
//        // 중복 평가 방지
//        if (matchRatingRepository.findByMatchRequestAndRater(matchRequest, rater).isPresent()) {
//            throw new IllegalStateException("이미 평가했습니다.");
//        }
//
//        // 평가 저장
//        MatchRating rating = new MatchRating();
//        rating.setMatchRequest(matchRequest);
//        rating.setRater(rater);
//        rating.setTarget(target);
//        rating.setScore(score);
//        rating.setComment(comment);
//
//        matchRatingRepository.save(rating);
//
//        return "redirect:/myMatches";
//    }
//
//
//    @GetMapping("/myRatings")
//    public String viewMyRatings(@AuthenticationPrincipal(expression = "member") Member member,
//                                Model model) {
//
//        List<MatchRating> receivedRatings = matchRatingRepository.findByTarget(member);
//        model.addAttribute("ratings", receivedRatings);
//        return "match/myRatings";
//    }
//}


package com.example.miniproject.controller;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.entity.MatchRating;
import com.example.miniproject.entity.MatchRequest;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MatchRatingRepository;
import com.example.miniproject.repository.MatchRequestRepository;
import com.example.miniproject.service.MatchService;
import com.example.miniproject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MatchRatingController {

    private final MatchRatingRepository matchRatingRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final MemberService memberService;
    private final MatchService matchService;

    // 평가 폼 보여주기
    @GetMapping("/match/{id}/rate")
    public String showRatingForm(@PathVariable Long id,
                                 @AuthenticationPrincipal Object principal,
                                 Model model) {

        MatchRequest matchRequest = matchRequestRepository.findById(id).orElseThrow();
        Member rater = extractMember(principal);

        // 자기 자신 평가 방지
        if (matchRequest.getMember().getId().equals(rater.getId())) {
            throw new AccessDeniedException("자기 자신은 평가할 수 없습니다.");
        }

        // 중복 평가 방지
        if (matchRatingRepository.findByMatchRequestAndRater(matchRequest, rater).isPresent()) {
            model.addAttribute("msg", "이미 평가를 완료했습니다.");
            return "redirect:/match/list";
        }

        Member target = matchRequest.getMember();
        model.addAttribute("match", matchRequest);
        model.addAttribute("target", target);
        return "match/rating";
    }

    // 평가 저장
    @PostMapping("/match/{id}/rate")
    public String submitRating(@PathVariable Long id,
                               @RequestParam int score,
                               @RequestParam(required = false) String comment,
                               @AuthenticationPrincipal Object principal) {

        MatchRequest matchRequest = matchRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭 요청입니다."));

        Member rater = extractMember(principal);

        // 평가 대상 결정
        Member target;
        if (rater.getId().equals(matchRequest.getMember().getId())) {
            target = matchRequest.getOpponent();
        } else if (matchRequest.getOpponent() != null && rater.getId().equals(matchRequest.getOpponent().getId())) {
            target = matchRequest.getMember();
        } else {
            throw new AccessDeniedException("이 매칭의 당사자만 평가할 수 있습니다.");
        }

        // 자기 자신 평가 방지
        if (rater.getId().equals(target.getId())) {
            throw new AccessDeniedException("자기 자신은 평가할 수 없습니다.");
        }

        // 중복 평가 방지
        if (matchRatingRepository.findByMatchRequestAndRater(matchRequest, rater).isPresent()) {
            throw new IllegalStateException("이미 평가했습니다.");
        }

        MatchRating rating = new MatchRating();
        rating.setMatchRequest(matchRequest);
        rating.setRater(rater);
        rating.setTarget(target);
        rating.setScore(score);
        rating.setComment(comment);

        matchRatingRepository.save(rating);
        return "redirect:/myMatches";
    }

    // 내가 받은 평가 목록
    @GetMapping("/myRatings")
    public String viewMyRatings(@AuthenticationPrincipal Object principal,
                                Model model) {

        Member member = extractMember(principal);
        List<MatchRating> receivedRatings = matchRatingRepository.findByTarget(member);
        model.addAttribute("ratings", receivedRatings);
        return "match/myRatings";
    }

    // 공통 메서드: 로그인된 Member 추출
    private Member extractMember(Object principal) {
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getMember();
        } else if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getMember();
        } else {
            throw new IllegalStateException("로그인된 사용자만 이용할 수 있습니다.");
        }
    }
}
