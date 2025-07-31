package com.example.miniproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchSlotDto {

    private String startTime;
    private String endTime;
    private boolean matched;
    private boolean requested;
    private String status;
    private boolean mine;

}
