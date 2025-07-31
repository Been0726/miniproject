package com.example.miniproject.dto;

import com.example.miniproject.entity.Article;
import com.example.miniproject.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class CommentDto {

    private Long id;
    private Long articleId;
    private String articleTitle;
    private String nickname;
    private String body;
    private String email;
    private LocalDateTime regTime;

    public static CommentDto createCommentDto(Comment comment) {
        Article article = comment.getArticle();
        return new CommentDto(
                comment.getId(),
                article != null ? article.getId() : null,
                article != null ? article.getTitle() : "[삭제된 글]",
                comment.getNickname(),
                comment.getBody(),
                comment.getCreatedBy(),
                comment.getRegTime());
    }
}
