package com.example.miniproject.config;

import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalMemberInfoAdvice {

    private final MemberRepository memberRepository;

    @ModelAttribute("loginNickname")
    public String loginMember(@AuthenticationPrincipal Object principal) {
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getMember().getNickname();
        }
        if (principal instanceof CustomOAuth2User oAuth2User) {
            return oAuth2User.getMember().getNickname();
        }

        return null;
    }

    @ModelAttribute("loginPoint")
    public int loginMemberPoint(@AuthenticationPrincipal Object principal) {
        String email = null;
        if (principal instanceof CustomUserDetails userDetails) {
            email = userDetails.getMember().getEmail();
        }
        if (principal instanceof CustomOAuth2User oAuth2User) {
            email = oAuth2User.getMember().getEmail();
        }

        if (email != null) {
            return memberRepository.findByEmail(email).getPoint();
        }

        return 0;
    }
}
