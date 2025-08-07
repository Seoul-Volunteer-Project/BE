package com.example.youthCare.Post;

import com.example.youthCare.PostImage.PostImage;
import com.example.youthCare.S3.S3Uploader;
import com.example.youthCare.User.User;
import com.example.youthCare.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    // 게시글 생성
    public PostResponseDTO createPost(Long userId, PostCreateRequestDTO request, MultipartFile[] files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        Post post = Post.builder()
                .author(user)
                .title(request.getTitle())
                .content(request.getContent())
                .boardType(request.getBoardType())
                .viewContent(0)
                .build();

        // 1. 먼저 게시글만 저장 (postId 확보)
        Post savedPost = postRepository.save(post);

        // 2. 이미지가 있다면 S3에 업로드
        if (files != null) {
            for (MultipartFile file : files) {
                try {
                    // 폴더 경로: post-images/{boardType}/{postId}
                    String dirPath = String.format("post-images/%s/%d",
                            request.getBoardType().name(), savedPost.getId());

                    String imageUrl = s3Uploader.upload(file, dirPath);

                    savedPost.getImages().add(PostImage.builder()
                            .post(savedPost)
                            .imageUrl(imageUrl)
                            .build());
                } catch (IOException e) {
                    // 예외 로그 출력 후 계속 진행하거나 적절히 처리
                    log.error("이미지 업로드 중 오류 발생: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("이미지 업로드 실패", e);
                }
            }
        }

        return new PostResponseDTO(postRepository.save(savedPost)); // 이미지까지 저장된 상태로 반환
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
