package com.example.youthCare.User;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입 처리
    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {
        // 중복 이메일 체크
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // role ENUM 값 기본 설정
        User.Role parsedRole = User.Role.valueOf(
                Optional.ofNullable(requestDTO.getRole())
                        .orElse("USER")
                        .toUpperCase()
        );

        // User 엔티티 생성
        User user = User.builder()
                .email(requestDTO.getEmail())
                .passwordHash(passwordEncoder.encode(requestDTO.getPassword()))
                .name(requestDTO.getName())
                .role(parsedRole)
                .build();

        User saved = userRepository.save(user); // DB 저장

        return new UserResponseDTO(saved); // 응답 DTO 반환
    }

    // 로그인 처리 (비밀번호 검증 포함)
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 비밀번호 비교 (평문 vs hash)
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    // 전체 사용자 조회
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ID로 사용자 단건 조회
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
        return new UserResponseDTO(user);
    }

}
