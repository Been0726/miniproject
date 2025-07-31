package com.example.miniproject.entity;

import com.example.miniproject.dto.RatingDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter @Setter
public class MatchRating extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Member rater;

    @ManyToOne
    private Member target;

    @ManyToOne
    private MatchRequest matchRequest;

    private int score;

    private String comment;

    public RatingDto toDto() {
        return RatingDto.builder()
                .score(this.score)
                .comment(this.comment)
                .raterNickname(this.rater.getNickname())
                .build();
    }
}
