package com.example.miniproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@ToString
@Setter
@Getter
public class Article extends BaseEntity {

    public Article(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String content;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private Long viewCount = 0L;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void patch(Article article) {
        if (article.title != null) {
            this.title = article.title;
        }
        if (article.content != null) {
            this.content = article.content;
        }
    }
}
