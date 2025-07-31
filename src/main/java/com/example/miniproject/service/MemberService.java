package com.example.miniproject.service;

import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 일반 로그인용만 허용
        Member member = memberRepository.findByEmailAndProvider(email, "local");

        if (member == null) {
            throw new UsernameNotFoundException("존재하지 않는 회원입니다 : " + email);
        }

        return new CustomUserDetails(member);
    }

    // 일반 회원가입
    public Member saveNormalMember(Member member) {
        validateDuplicateEmail(member.getEmail());          // 이메일 중복 금지
        validateDuplicateNickname(member.getNickname());    // 닉네임 중복 금지
        return memberRepository.save(member);
    }

    // 소셜 회원가입
    public Member saveSocialMember(Member member) {
        validateDuplicateEmail(member.getEmail());
        validateDuplicateNickname(member.getNickname());
        return memberRepository.save(member);
    }

    private void validateDuplicateNickname(String nickname) {
        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.findByEmail(email) != null) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateDuplicateEmailAndProvider(String email, String provider) {
        if (memberRepository.findByEmailAndProvider(email, provider) != null) {
            throw new IllegalStateException("이미 해당 방식으로 가입된 이메일입니다.");
        }
    }
}
