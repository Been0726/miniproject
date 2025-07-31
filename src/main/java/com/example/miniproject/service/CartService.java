package com.example.miniproject.service;

import com.example.miniproject.dto.CartDetailDto;
import com.example.miniproject.dto.CartItemDto;
import com.example.miniproject.dto.CartOrderDto;
import com.example.miniproject.dto.OrderDto;
import com.example.miniproject.entity.*;
import com.example.miniproject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;
    private final PointHistoryRepository pointHistoryRepository;

    public Long addCart(CartItemDto cartItemDto, String email) {
        Item item = itemRepository.findById(cartItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember();

        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }

        return true;
    }

    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {
        Member member = memberRepository.findByEmail(email);

        int totalPrice = 0;
        for (CartOrderDto dto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(dto.getCartItemId())
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 항목이 존재하지 않습니다."));
            totalPrice += cartItem.getItem().getPrice() * cartItem.getCount();
        }

        // 포인트 부족 검사
        if (member.getPoint() < totalPrice) {
            throw new IllegalStateException("포인트가 부족합니다. 현재 포인트 : " + member.getPoint());
        }

        // 주문 처리
        List<OrderDto> orderDtoList = new ArrayList<>();
        for (CartOrderDto dto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(dto.getCartItemId())
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 항목이 존재하지 않습니다."));
            orderDtoList.add(new OrderDto(cartItem.getItem().getId(), cartItem.getCount()));
        }
        Long orderId = orderService.orders(orderDtoList, email);

        // 포인트 차감
        member.usePoint(totalPrice);

        // 포인트 사용 내역 기록
        PointHistory history = PointHistory.builder()
                .member(member)
                .amount(-totalPrice)
                .description("장바구니 상품 주문")
                .spotName(null)
                .build();
        pointHistoryRepository.save(history);

        // 장바구니 삭제
        for (CartOrderDto dto : cartOrderDtoList) {
            cartItemRepository.deleteById(dto.getCartItemId());
        }

        return orderId;
    }
}
