package com.example.miniproject.entity;

import com.example.miniproject.constant.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_request")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_slot_id", nullable = false)
    private MatchSlot matchSlot;

    private Boolean matched = false;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_id")
    private Member opponent;
}
