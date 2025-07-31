package com.example.miniproject.repository;

import com.example.miniproject.dto.ItemSearchDto;
import com.example.miniproject.dto.MainItemDto;
import com.example.miniproject.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
}
