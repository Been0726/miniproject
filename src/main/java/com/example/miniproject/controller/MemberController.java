package com.example.miniproject.controller;

import com.example.miniproject.constant.Role;
import com.example.miniproject.dto.MemberFormDto;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MemberRepository;
import com.example.miniproject.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    @PostMapping("/new")
    public String memberForm(@Valid MemberFormDto memberFormDto,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        log.info(memberFormDto.toString());

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            member.setProvider("local");
            memberService.saveNormalMember(member);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginMember(HttpServletRequest request, Model model) {
        String errorMessage = (String) request.getSession().getAttribute("errorMessage");
        log.info("🔥 로그인 에러 메시지: {}", errorMessage); // 추가
        if (errorMessage != null) {
            model.addAttribute("sessionErrorMessage", errorMessage);
        }
        return "member/memberLoginForm";
    }

    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "member/memberLoginForm";
    }

    @GetMapping("/join-extra")
    public String showExtraForm(HttpSession session, Model model) {
        Member temp = (Member) session.getAttribute("socialTempUser");
        if (temp == null) {
            return "redirect:/";
        }

        model.addAttribute("email", temp.getEmail());
        model.addAttribute("name", temp.getName());
        return "member/join-extra";
    }

    @PostMapping("/join-extra")
    public String joinExtraSubmit(@RequestParam String email,
                                  @RequestParam String name,
                                  @RequestParam String nickname,
                                  @RequestParam String postcode,
                                  @RequestParam String address,
                                  @RequestParam String detailAddress,
                                  @RequestParam String extraAddress,
                                  HttpSession session,
                                  Model model) {

        // 닉네임 중복 체크
        if (memberRepository.findByNickname(nickname).isPresent()) {
            model.addAttribute("errorMessage", "이미 사용중인 닉네임입니다.");
            model.addAttribute("email", email);
            model.addAttribute("name", name);
            return "member/join-extra";
        }

        // 이메일 중복 체크
        if (memberRepository.findByEmail(email) != null) {
            model.addAttribute("errorMessage", "이미 가입된 이메일입니다.");
            return "member/memberLoginForm";
        }

        // 세션에서 provider 꺼내기
        Member temp = (Member) session.getAttribute("socialTempUser");
        String provider = temp != null ? temp.getProvider() : "social";

        // 저장
        Member saved = Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .postcode(postcode)
                .address(address)
                .detailAddress(detailAddress)
                .extraAddress(extraAddress)
                .role(Role.USER)
                .password("social_login")
                .provider(provider)
                .build();

        memberRepository.save(saved);
        session.removeAttribute("socialTempUser");

        return "redirect:/";
    }

    @PostMapping("/login/clear-error")
    @ResponseBody
    public void clearLoginError(HttpSession session) {
        session.removeAttribute("errorMessage");
    }
}
