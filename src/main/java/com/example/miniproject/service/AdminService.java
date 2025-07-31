package com.example.miniproject.service;

import com.example.miniproject.constant.MatchStatus;
import com.example.miniproject.dto.AdminMatchStatsDto;
import com.example.miniproject.dto.DailyMatchCountDto;
import com.example.miniproject.dto.SpotMatchStatsDto;
import com.example.miniproject.entity.FutsalSpot;
import com.example.miniproject.repository.FutsalSpotRepository;
import com.example.miniproject.repository.MatchRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MatchRequestRepository matchRequestRepository;
    private final FutsalSpotRepository futsalSpotRepository;

    public List<AdminMatchStatsDto> getMatchStatusForSpot(Long spotId, int year, int month) {
        FutsalSpot spot = futsalSpotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("풋살장을 찾을 수 없습니다."));

        List<AdminMatchStatsDto> result = new ArrayList<>();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            long total = matchRequestRepository.countBySpotAndDate(spot, date);

            List<Long> confirmedSlotIds = matchRequestRepository.findActuallyConfirmedSlotIds(spot, date);
            long confirmed = confirmedSlotIds.size();

            int pointUsed = (int) confirmed * 25000 * 2;

            result.add(new AdminMatchStatsDto(
                    spot.getName(), date, total, confirmed, pointUsed
            ));
        }
        return result;
    }
}
