package com.example.miniproject.api;

import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.entity.Member;
import com.example.miniproject.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MatchApiController {

    private final MatchService matchService;

    @DeleteMapping("/match/{id}/cancel")
    public ResponseEntity<?> cancelMatch(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal Object principal) {

        Member me;

        if (principal instanceof CustomUserDetails) {
            me = ((CustomUserDetails) principal).getMember();
        } else {
            me = ((com.example.miniproject.config.CustomOAuth2User) principal).getMember();
        }

        if (me == null) {
            String body = "<script>alert('로그인이 필요해요!'); window.location.href='/login';</script>";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("Content-Type", "text/html; chartset=UTF-8")
                    .body(body);
        }

        boolean result = matchService.cancelRequest(id, me);

        if (result) {
            String body = "<script>alert('매칭 요청이 취소되었습니다.'); window.location.href='/myMatches';</script>";
            return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(body);
        } else {
            String body = "<script>alert('이미 매칭된 요청은 취소할 수 없습니다.'); window.location.href='/myMatches';</script>";
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(body);
        }
    }

    @GetMapping("/rating/{nickname}")
    @ResponseBody
    public Map<String, Object> getRatings(@PathVariable String nickname) {
        List<String> comments = matchService.getAllCommentByNickname(nickname);
        Double avg = matchService.getAverageScoreByNickname(nickname);

        Map<String, Object> result = new HashMap<>();
        result.put("average", avg);
        result.put("comments", comments);
        return result;
    }

}
