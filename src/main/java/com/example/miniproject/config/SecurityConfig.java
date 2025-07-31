package com.example.miniproject.config;

import com.example.miniproject.handler.OAuth2LoginFailureHandler;
import com.example.miniproject.service.CustomOAuth2UserService;
import com.example.miniproject.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberService memberService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .formLogin(login -> login
                        .loginPage("/members/login")
                        .defaultSuccessUrl("/", true)
                        .usernameParameter("email")
                        .failureUrl("/members/login/error")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/members/login")
                        .failureHandler(oAuth2LoginFailureHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            HttpSession session = request.getSession();
                            if (session.getAttribute("socialTempUser") != null) {
                                response.sendRedirect("/members/join-extra");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                        .logoutSuccessUrl("/")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                );
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/img/**", "/images/**");
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder) throws Exception {

        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(memberService)
                .passwordEncoder(passwordEncoder);

        return auth.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Profile("test")
    @Configuration
    public class TestSecurityConfig {

        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .build();
        }
    }

}
