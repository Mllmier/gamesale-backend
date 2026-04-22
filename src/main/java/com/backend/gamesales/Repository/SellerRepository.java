package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository  extends JpaRepository<Seller,Long> {
    Optional<Seller> findByUserId(Long userId);
    boolean existsByUser(Users users);


}
