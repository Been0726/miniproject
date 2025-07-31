package com.example.miniproject.service;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.constant.Role;
import com.example.miniproject.dto.OAuthAttributes;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email;
        String name;

        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");

            email = (String) response.get("email");
            name = (String) response.get("name");
            attributes = response;
        } else if ("kakao".equals(registrationId)){
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            attributes = kakaoAccount;
        } else {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }

        if (email == null || name == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_info"),
                    "이메일 또는 이름이 존재하지 않습니다."
            );
        }

        // 이미 local 가입된 이메일은 소셜 로그인 막기
        Member existingLocal = memberRepository.findByEmailAndProvider(email, "local");
        if (existingLocal != null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_conflict"),
                    "이미 동일 이메일로 일반 계정이 존재합니다."
            );
        }

        // provider별 사용자 조회
        Member socialUser = memberRepository.findByEmailAndProvider(email, registrationId);
        if (socialUser == null) {
            Member temp = Member.builder()
                    .email(email)
                    .name(name)
                    .provider(registrationId)
                    .role(Role.USER)
                    .build();

            session.setAttribute("socialTempUser", temp);

            throw new OAuth2AuthenticationException(
                    new OAuth2Error("REDIRECT_JOIN_EXTRA"),
                    "추가 정보가 필요합니다."
            );
        }

        // 이미 가입된 유저 -> 그대로 로그인
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + socialUser.getRole().name())),
                attributes,
                "email",
                socialUser
        );
    }
}
