package com.example.youthCare.Post;

import com.example.youthCare.PostImage.PostImageDTO;
import com.example.youthCare.User.UserSimpleDTO;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String boardType;
    private int viewContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSimpleDTO author;
    private List<PostImageDTO> images;

    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.boardType = post.getBoardType().name();
        this.viewContent = post.getViewContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.author = new UserSimpleDTO(post.getAuthor());
        this.images = post.getImages().stream()
                .map(PostImageDTO::new)
                .collect(Collectors.toList());
    }
}
