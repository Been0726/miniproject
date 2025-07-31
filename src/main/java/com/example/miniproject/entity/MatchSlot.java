package com.example.miniproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_slot")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "spot_id", nullable = false)
    private FutsalSpot spot;

    @OneToMany(mappedBy = "matchSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchRequest> matchRequests = new ArrayList<>();

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private boolean matched;
}
