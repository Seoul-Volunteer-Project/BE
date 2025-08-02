package com.example.youthCare.Post;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 등록
    @PostMapping("/create")
    public ResponseEntity<PostResponseDTO> createPost(@RequestParam String title,
                                        @RequestParam String content,
                                        @RequestParam Post.BoardType boardType,
                                        @RequestParam(required = false) List<String> imageUrls,
                                        HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        PostResponseDTO created = postService.createPost(userId, title, content, boardType, imageUrls);
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
}
