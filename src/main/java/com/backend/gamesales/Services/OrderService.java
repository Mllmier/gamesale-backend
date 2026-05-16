package com.backend.gamesales.Services;


import com.backend.gamesales.Dto.Response.OrderResponse;
import com.backend.gamesales.Dto.Response.PaymentItemResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Model.*;
import com.backend.gamesales.Repository.OrderRepository;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderResponse> getOrdersByUser(Users user) {
        return orderRepository.findByBuyerOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long orderId, Users user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new NotFoundException("Order not found: " + orderId);
        }

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .items(order.getPayment().getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .build();
    }

    private PaymentItemResponse toItemResponse(PaymentItem item) {
        return PaymentItemResponse.builder()
                .gameId(item.getGame().getId())
                .gameTitle(item.getGame().getTitle())
                .sellerId(item.getSeller().getId())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }
}