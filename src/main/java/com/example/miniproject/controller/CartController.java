package com.example.miniproject.controller;

import com.example.miniproject.dto.CartDetailDto;
import com.example.miniproject.dto.CartItemDto;
import com.example.miniproject.dto.CartOrderDto;
import com.example.miniproject.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/cart")
    public @ResponseBody ResponseEntity<?> order(@RequestBody @Valid CartItemDto cartItemDto,
                                                 BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        try {
            Long cartItemId = cartService.addCart(cartItemDto, principal.getName());
            return new ResponseEntity<>(cartItemId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("장바구니 추가 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/cart")
    public String orderHist(Principal principal, Model model) {
        List<CartDetailDto> cartDetailDtoList = cartService.getCartList(principal.getName());
        model.addAttribute("cartItems", cartDetailDtoList);
        return "cart/cartList";
    }

    @PatchMapping("/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity<?> updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                          int count, Principal principal) {
        if (count <= 0) {
            return new ResponseEntity<>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        } else if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return new ResponseEntity<>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<>(cartItemId, HttpStatus.OK);
    }

    @DeleteMapping("/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity<?> deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal) {
        if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return new ResponseEntity<>("삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<>(cartItemId, HttpStatus.OK);
    }

    @PostMapping("/cart/orders")
    public @ResponseBody ResponseEntity<?> orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal) {
        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if (cartOrderDtoList == null || cartOrderDtoList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message","주문할 상품을 선택해주세요"));
        }

        for (CartOrderDto cartOrder : cartOrderDtoList) {
            if (!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message","주문 권한이 없습니다."));
            }
        }

        try {
            Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
            return ResponseEntity.ok(Map.of("orderId", orderId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
