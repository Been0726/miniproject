package com.example.miniproject.service;

import com.example.miniproject.constant.OrderStatus;
import com.example.miniproject.dto.CartOrderDto;
import com.example.miniproject.dto.OrderDto;
import com.example.miniproject.dto.OrderHistDto;
import com.example.miniproject.dto.OrderItemDto;
import com.example.miniproject.entity.*;
import com.example.miniproject.repository.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final CartItemRepository cartItemRepository;

    public Long order(OrderDto orderDto, String email) {
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityExistsException::new);
        Member member = memberRepository.findByEmail(email);

        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);

        Order order = Order.createOrder(member, orderItemList);
        int totalPrice = order.getTotalPrice();

        // 포인트 차감 처리
        if (member.getPoint() < totalPrice) {
            throw new IllegalStateException("보유 포인트가 부족합니다.");
        }
        member.usePoint(totalPrice);

        // 포인트 내역 저장
        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(-totalPrice)
                .description("상품 구매")
                .spotName(null)
                .build();
        pointHistoryRepository.save(pointHistory);

        orderRepository.save(order);
        return order.getId();
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    @Transactional
    public boolean validateOrder(Long orderId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = order.getMember();

        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }

        return true;
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        order.cancelOrder();

        Member member = order.getMember();
        int refundAmount = order.getTotalPrice();
        member.addPoint(refundAmount);

        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(refundAmount)
                .description("상품 주문 취소 환불")
                .spotName(null)
                .build();
        pointHistoryRepository.save(pointHistory);
    }

    public Long orders(List<OrderDto> orderDtoList, String email) {

        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList = new ArrayList<>();

        int totalPrice = 0;

        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
            totalPrice += item.getPrice() * orderDto.getCount();
        }

        if (member.getPoint() < totalPrice) {
            throw new IllegalStateException("보유 포인트가 부족합니다. 현재 포인트 : " + member.getPoint());
        }

        member.usePoint(totalPrice);

        PointHistory pointHistory = PointHistory.builder()
                .member(member)
                .amount(-totalPrice)
                .description("상품 구매")
                .spotName(null)
                .build();
        pointHistoryRepository.save(pointHistory);

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

}
