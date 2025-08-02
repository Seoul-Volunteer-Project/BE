package com.example.youthCare.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO request) {
        UserResponseDTO result = userService.registerUser(request);
        return ResponseEntity.ok(result);
    }

    // 로그인 - 세션에 userId 저장
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO request, HttpSession session) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            session.setAttribute("userId", user.getId()); // 세션에 로그인 정보 저장
            return ResponseEntity.ok(new UserResponseDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 로그아웃 - 세션 삭제
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 세션 제거
        return ResponseEntity.ok("로그아웃 완료");
    }

    // 현재 로그인된 사용자 정보 반환
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        // 로그인하지 않은 경우
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 상태가 아닙니다.");
        }

        // 유저 정보 조회
        UserResponseDTO userInfo = userService.findById(userId);
        return ResponseEntity.ok(userInfo);
    }

    // 모든 유저 조회 (관리자용)
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.findAll();
    }

    // ID로 유저 조회
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
}
