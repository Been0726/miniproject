package com.example.miniproject.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = exception.getMessage();
        log.error("🔴 소셜 로그인 실패: {}", errorMessage);

        if ("추가 정보가 필요합니다.".equals(errorMessage)) {
            request.getSession().setAttribute("errorMessage", errorMessage);
            getRedirectStrategy().sendRedirect(request, response, "/members/join-extra");
        } else {
            request.getSession().setAttribute("errorMessage", errorMessage);
            getRedirectStrategy().sendRedirect(request, response, "/members/login?error");
        }
    }
}
