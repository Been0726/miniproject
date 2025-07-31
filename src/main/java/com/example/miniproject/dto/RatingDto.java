package com.example.miniproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RatingDto {

    private int score;
    private String comment;
    private String raterNickname;
}
