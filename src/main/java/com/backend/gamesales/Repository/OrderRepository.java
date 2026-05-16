package com.backend.gamesales.Repository;
import com.backend.gamesales.Model.Order;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByBuyerOrderByCreatedAtDesc(Users buyer);

}
