package com.example.youthCare.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostCreateRequestDTO {
    private String title;
    private String content;
    private Post.BoardType boardType;
}
