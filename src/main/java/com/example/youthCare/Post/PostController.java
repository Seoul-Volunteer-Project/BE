package com.example.youthCare.Post;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Post.BoardType boardType,
            @RequestPart(required = false) MultipartFile[] files,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        // PostService에 전달할 DTO 직접 생성
        PostCreateRequestDTO request = new PostCreateRequestDTO(title, content, boardType);

        PostResponseDTO created = postService.createPost(userId, request, files);
        return ResponseEntity.ok(created);
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // 게시판별 전체 조회
    @GetMapping("/board/{boardType}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByBoard(@PathVariable Post.BoardType boardType) {
        return ResponseEntity.ok(postService.getPostsByBoardType(boardType));
    }

    // 전체 게시글 조회
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // 게시글 단건 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build(); // 204
    }
}
