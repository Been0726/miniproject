package com.example.miniproject.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class SpotMatchStatsDto {

    private LocalDate date;
    private long requestCount;
    private long confirmCount;
    private int pointUsed;
}
