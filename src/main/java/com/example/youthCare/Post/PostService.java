package com.example.youthCare.Post;

import com.example.youthCare.PostImage.PostImage;
import com.example.youthCare.S3.S3Uploader;
import com.example.youthCare.User.User;
import com.example.youthCare.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // 게시글 단건 삭제
    @Transactional
    public void deletePost(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 권한 체크: 작성자(또는 관리자 로직이 있다면 추가)
        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        // 1) S3에서 이미지 정리
        //   - 저장된 URL 기반으로 개별 삭제 + 혹시 DB에 없는 파일이 있을 수 있으니 폴더 프리픽스도 정리
        for (PostImage img : post.getImages()) {
            s3Uploader.deleteByUrl(img.getImageUrl());
        }
        String prefix = String.format("post-images/%s/%d/",
                post.getBoardType().name(), post.getId());
        s3Uploader.deleteFolder(prefix);

        // 2) DB 삭제 (자식은 cascade + orphanRemoval 로 함께 삭제)
        postRepository.delete(post);
    }
}
