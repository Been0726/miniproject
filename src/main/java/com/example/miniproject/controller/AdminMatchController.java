package com.example.miniproject.controller;

import com.example.miniproject.dto.AdminMatchStatsDto;
import com.example.miniproject.dto.DailyMatchCountDto;
import com.example.miniproject.dto.ItemFormDto;
import com.example.miniproject.dto.ItemSearchDto;
import com.example.miniproject.entity.FutsalSpot;
import com.example.miniproject.entity.Item;
import com.example.miniproject.repository.FutsalSpotRepository;
import com.example.miniproject.service.AdminService;
import com.example.miniproject.service.ItemService;
import com.example.miniproject.service.MatchService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequiredArgsConstructor
public class AdminMatchController {

    private final AdminService adminService;
    private final ItemService itemService;
    private final MatchService matchService;
    private final FutsalSpotRepository futsalSpotRepository;

    @GetMapping("/admin/match-status")
    public String showMonthlyStats (@RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Integer month,
                                    @RequestParam(required = false) Long spotId,
                                    Model model) throws JsonProcessingException {
        LocalDate now = LocalDate.now();
        int selectedYear = (year != null) ? year : now.getYear();
        int selectedMonth = (month != null) ? month : now.getMonthValue();

        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        List<LocalDate> allDates = IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
                .mapToObj(day -> LocalDate.of(selectedYear, selectedMonth, day))
                .toList();

        List<DailyMatchCountDto> merged = matchService.getMergedDailyMatchStats(selectedYear, selectedMonth);
        Map<LocalDate, DailyMatchCountDto>  dtoMap = merged.stream()
                .collect(Collectors.toMap(DailyMatchCountDto::getDate, Function.identity()));

        List<String> labels = new ArrayList<>();
        List<Long> requestCounts = new ArrayList<>();
        List<Long> confirmCounts = new ArrayList<>();

        for (LocalDate date : allDates) {
            labels.add(date.format(DateTimeFormatter.ofPattern("M/d")));
            DailyMatchCountDto dto = dtoMap.getOrDefault(date, new DailyMatchCountDto(date, 0L, 0L));
            requestCounts.add(dto.getRequestCount());
            confirmCounts.add(dto.getConfirmCount());
        }

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartDataRequest", requestCounts);
        model.addAttribute("chartDataConfirm", confirmCounts);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);

        // 전체 풋살장 목록
        List<FutsalSpot> spotList = futsalSpotRepository.findAll().stream()
                .sorted(Comparator.comparing(FutsalSpot::getName))
                .toList();
        model.addAttribute("spotList", spotList);

        // 풋살장 상세 매칭 현황
        if (spotId != null) {
            List<AdminMatchStatsDto> spotStats = adminService.getMatchStatusForSpot(spotId, selectedYear, selectedMonth);
            model.addAttribute("selectedSpotId", spotId);
            model.addAttribute("spotStats", spotStats);

            FutsalSpot selectedSpot = futsalSpotRepository.findById(spotId).orElse(null);
            if (selectedSpot != null) {
                model.addAttribute("selectedSpotName", selectedSpot.getName());
            }
        }

        return "admin/match-status";
    }


    @GetMapping("/admin/item/new")
    public String itemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }

    @PostMapping("/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                          Model model, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
        return "redirect:/shop";
    }

    @GetMapping("/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }

        return "item/itemForm";
    }

    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList, Model model) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    @GetMapping({"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page,
                             Model model) {

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);
        return "item/itemMng";
    }
}
