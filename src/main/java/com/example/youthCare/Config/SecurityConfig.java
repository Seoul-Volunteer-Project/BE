package com.example.youthCare.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF는 REST API에선 보통 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll() // 회원가입, 로그인 허용
                        .anyRequest().permitAll() // 나머지 모든 요청 허용 (임시 테스트), 필요한 경우 authenticated()로 변경
                )
                .formLogin(login -> login.disable()) // 폼 로그인 사용 X, 우리가 API로 처리
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable()); // logout API로 수동 구현

        return http.build();
    }
}
