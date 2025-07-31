package com.example.miniproject.controller;

import com.example.miniproject.entity.FutsalSpot;
import com.example.miniproject.repository.FutsalSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FutsalController {

    private final FutsalSpotRepository futsalSpotRepository;

    @GetMapping("/map")
    public String mapPage() {
        return "futsal/map";
    }

    @ResponseBody
    @GetMapping("/api/spots")
    public List<FutsalSpot> getAllSpots() {
        return futsalSpotRepository.findAll();
    }
}
