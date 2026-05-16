package com.backend.gamesales.Controller;
import com.backend.gamesales.Dto.Response.OrderResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Model.Order;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UsersRepository;
import com.backend.gamesales.Services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService    orderService;
    private final UsersRepository usersRepository;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        Users user = findUser(userDetails.getUsername());
        return ResponseEntity.ok(orderService.getOrdersByUser(user));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Users user = findUser(userDetails.getUsername());
        return ResponseEntity.ok(orderService.getOrderById(orderId, user));
    }

    private Users findUser(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }
}