package com.example.miniproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AdminMatchStatsDto {

    private String spotName;
    private LocalDate date;
    private long totalRequests;
    private long confirmed;
    private int totalPointUsed;
}
