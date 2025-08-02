package com.example.youthCare.PostImage;


import lombok.Getter;

@Getter
public class PostImageDTO {
    private Long id;
    private String imageUrl;

    public PostImageDTO(PostImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
    }
}
