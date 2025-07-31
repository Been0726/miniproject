package com.example.miniproject.dto;

import com.example.miniproject.entity.Article;
import com.example.miniproject.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ArticleDto extends BaseEntity {

    private Long id;
    private String title;
    private String content;
    private String writerNickname;
    private int commentSize;
    private LocalDateTime regTime;

    public static ArticleDto fromEntity(Article article, String nickname) {
        return new ArticleDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                nickname,
                article.getComments().size(),
                article.getRegTime());
    }
}
