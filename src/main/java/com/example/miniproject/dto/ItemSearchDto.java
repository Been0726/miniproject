package com.example.miniproject.dto;

import com.example.miniproject.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemSearchDto {

    private String searchDateType;

    private ItemSellStatus searchSellStatus;

    private String searchBy;

    private String searchQuery = "";
}
