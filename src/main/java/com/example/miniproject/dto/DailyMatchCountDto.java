package com.example.miniproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyMatchCountDto {

    private LocalDate date;
    private long requestCount;
    private long confirmCount;

    public DailyMatchCountDto(LocalDate date, long requestCount, long confirmCount) {
        this.date = date;
        this.requestCount = requestCount;
        this.confirmCount = confirmCount;
    }

    // 요청 수용도 가능 (확인용)
    public DailyMatchCountDto(String dateStr, long requestCount) {
        this.date = LocalDate.parse(dateStr);
        this.requestCount = requestCount;
        this.confirmCount = 0;
    }

    // 성사 수용도 가능 (확인용)
    public static DailyMatchCountDto confirmedOnly(String dateStr, long confirmedCount) {
        DailyMatchCountDto dto = new DailyMatchCountDto(LocalDate.parse(dateStr), 0, confirmedCount);
        return dto;
    }
}

