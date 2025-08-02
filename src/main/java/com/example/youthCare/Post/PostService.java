package com.example.youthCare.Post;

import com.example.youthCare.PostImage.PostImage;
import com.example.youthCare.User.User;
import com.example.youthCare.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 생성
    public PostResponseDTO createPost(Long userId, String title, String content, Post.BoardType boardType, List<String> imageUrls) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        Post post = Post.builder()
                .author(user)
                .title(title)
                .content(content)
                .boardType(boardType)
                .viewContent(0)
                .build();

        // 이미지 리스트가 null이 아니고 비어있지 않은 경우에만 처리
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                post.getImages().add(PostImage.builder()
                        .post(post)
                        .imageUrl(url)
                        .build());
            }
        }

        return new PostResponseDTO(postRepository.save(post));
    }

    // 게시글 단건 조회
    public PostResponseDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return new PostResponseDTO(post);
    }

    // 게시판별 전체 조회
    public List<PostResponseDTO> getPostsByBoardType(Post.BoardType boardType) {
        return postRepository.findByBoardType(boardType).stream()
                .map(PostResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 전체 게시글 조회
    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponseDTO::new)
                .collect(Collectors.toList());
    }
}
