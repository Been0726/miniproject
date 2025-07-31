package com.example.miniproject.service;

import com.example.miniproject.constant.MatchStatus;
import com.example.miniproject.dto.DailyMatchCountDto;
import com.example.miniproject.dto.RatingDto;
import com.example.miniproject.entity.*;
import com.example.miniproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchSlotRepository matchSlotRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final MatchRatingRepository matchRatingRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final PointHistoryRepository pointHistoryRepository;
    private final FutsalSpotRepository futsalSpotRepository;
    private final int MATCH_COST = 50000;

    @Transactional
    public MatchRequest submitRequest(Member me, FutsalSpot spot,
                                      LocalDate date, LocalTime startTime, LocalTime endTime) {

        Member persistentMember = memberRepository.findById(me.getId())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        int halfCost = MATCH_COST / 2;

        // ✅ 포인트 부족 시 차단
        if (persistentMember.getPoint() < halfCost) {
            throw new IllegalStateException("포인트가 부족합니다. 매칭 신청은 최소 " + halfCost + "P가 필요해요!");
        }

        // ✅ 매칭 슬롯 찾기 or 생성
        MatchSlot slot = matchSlotRepository
                .findBySpotAndDateAndStartTimeAndEndTime(spot, date, startTime, endTime)
                .orElseGet(() -> {
                    MatchSlot newSlot = new MatchSlot();
                    newSlot.setSpot(spot);
                    newSlot.setDate(date);
                    newSlot.setStartTime(startTime);
                    newSlot.setEndTime(endTime);
                    newSlot.setMatched(false);
                    return matchSlotRepository.save(newSlot);
                });

        // ✅ 중복 신청 방지
        Optional<MatchRequest> existing = matchRequestRepository.findByMemberAndMatchSlot(persistentMember, slot);
        if (existing.isPresent()) {
            throw new IllegalStateException("이미 해당 시간대에 신청한 매치가 있습니다!");
        }

        // ✅ 상대방 매칭 가능성 탐색
        Optional<MatchRequest> maybeOpponent = matchRequestRepository
                .findFirstByMatchSlotAndMatchedFalseAndMemberNot(slot, persistentMember);

        MatchRequest myRequest = new MatchRequest();
        myRequest.setMember(persistentMember);
        myRequest.setMatchSlot(slot);
        myRequest.setMatched(false);
        myRequest.setStatus(MatchStatus.PENDING);

        // ✅ 포인트 선차감 (신청자 본인)
        persistentMember.setPoint(persistentMember.getPoint() - halfCost);
        memberRepository.save(persistentMember);

        pointHistoryRepository.save(PointHistory.builder()
                .member(persistentMember)
                .amount(-halfCost)
                .description("매칭 신청 비용")
                .spotName(slot.getSpot().getName())
                .build());

        // ✅ 상대방이 존재하는 경우 → 매칭 성사 시도
        if (maybeOpponent.isPresent()) {
            MatchRequest opponent = maybeOpponent.get();
            Member opponentMember = memberRepository.findById(opponent.getMember().getId())
                    .orElseThrow(() -> new IllegalStateException("상대방 정보 오류"));

            // ❗ 본인이 자기 자신과 매칭되는 경우 차단
            if (opponentMember.getId().equals(persistentMember.getId())) {
                return matchRequestRepository.save(myRequest); // 매칭 없이 저장만
            }

            // ✅ 매칭 설정
            myRequest.setMatched(true);
            myRequest.setOpponent(opponentMember);
            myRequest.setStatus(MatchStatus.CONFIRMED);

            opponent.setMatched(true);
            opponent.setOpponent(persistentMember);
            opponent.setStatus(MatchStatus.CONFIRMED);
            matchRequestRepository.save(opponent);

            slot.setMatched(true);

            // ✅ 알림 발송
            notificationService.notifyMatch(opponent.getMember(), persistentMember.getNickname() + "님과 매칭이 성사되었습니다!");
            notificationService.notifyMatch(persistentMember, opponent.getMember().getNickname() + "님과 매칭이 성사되었습니다!");
        }

        return matchRequestRepository.save(myRequest);
    }



    public List<MatchRequest> getMatchRequestsForUser(Member member) {
        return matchRequestRepository.findByMemberOrderByCreatedByDesc(member);
    }

    @Transactional
    public boolean cancelRequest(Long requestId, Member me) {
        Optional<MatchRequest> optional = matchRequestRepository.findById(requestId);
        if (optional.isEmpty()) return false;

        MatchRequest request = optional.get();

        // 본인이 아닐 경우
        if (!request.getMember().getId().equals(me.getId())) return false;

        // 상대가 있을 경우 (CONFIRMED 상태는 취소 불가)
        if (request.getStatus() == MatchStatus.CONFIRMED) return false;

        // 포인트 환불
        int halfCost = MATCH_COST / 2;
        me.setPoint(me.getPoint() + halfCost);
        memberRepository.save(me);

        pointHistoryRepository.save(PointHistory.builder()
                .member(me)
                .amount(+halfCost)
                .description("매칭 취소 환불")
                .spotName(request.getMatchSlot().getSpot().getName())
                .build());

        matchRequestRepository.delete(request);

        return true;
    }

    public Map<String, List<RatingDto>> getAllRatingsByOpponentNickname(List<MatchRequest> requests) {
        return requests.stream()
                .map(MatchRequest::getOpponent)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Member::getNickname,
                        opp -> matchRatingRepository.findAllByTarget(opp).stream()
                                .map(MatchRating::toDto)
                                .collect(Collectors.toList())
                ));
    }
    public Double getAverageScoreByNickname(String nickname) {
        Member target = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("해당 닉네임의 회원이 없습니다."));
        return matchRatingRepository.findAverageScoreByTarget(target);
    }

    public List<String> getAllCommentByNickname(String nickname) {
        Member target = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("해당 닉네임의 회원이 없습니다."));
        return matchRatingRepository.findAllByTarget(target).stream()
                .map(MatchRating::getComment)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMatchStatusByHour(Long spotId, LocalDate date, Member member) {

        List<Map<String, Object>> result = new ArrayList<>();

        FutsalSpot spot = futsalSpotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("풋살장을 찾을 수 없어요"));

        for (int hour = 0; hour < 24; hour += 2) {
            LocalTime startTime = LocalTime.of(hour, 0);
            LocalTime endTime = LocalTime.of((hour + 2) % 24, 0);

            Optional<MatchSlot> slotOpt = matchSlotRepository
                    .findOptionalBySpotAndDateAndStartTimeAndEndTime(spot, date, startTime, endTime);

            boolean matched = false;
            boolean requested = false;
            boolean myRequest = false;

            if (slotOpt.isPresent()) {
                MatchSlot slot = slotOpt.get();
                matched = slot.isMatched();

                List<MatchRequest> requests = matchRequestRepository.findByMatchSlot(slot);
                requested = !requests.isEmpty();

                // ✅ 내가 신청한 시간인지도 확인
                myRequest = requests.stream()
                        .anyMatch(r -> r.getMember().getId().equals(member.getId()));
            }

            Map<String, Object> map = new HashMap<>();
            map.put("startTime", startTime.toString());
            map.put("endTime", endTime.toString());
            map.put("matched", matched);
            map.put("requested", requested);
            map.put("mine", myRequest);

            result.add(map);
        }

        return result;
    }

    public List<DailyMatchCountDto> getMergedDailyMatchStats(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<LocalDate> allDates = IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
                .mapToObj(day -> LocalDate.of(year, month, day))
                .toList();

        List<DailyMatchCountDto> result = new ArrayList<>();

        for (LocalDate date : allDates) {
            long requestCount = matchRequestRepository.countByDate(date); // 전체 요청 수
            long confirmedCount = matchRequestRepository.findActuallyConfirmedSlotIdsByDate(date).size(); // slot 기준

            result.add(new DailyMatchCountDto(date, requestCount, confirmedCount));
        }

        return result;
    }


}
