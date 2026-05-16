package com.backend.gamesales.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;

@Repository
public interface SellerRepository  extends JpaRepository<Seller,Long> {
    Optional<Seller> findByUserId(Long userId);
    boolean existsByUser(Users users);
    Page<Seller> findByStatus(StatusSeller status, Pageable pageable);
    Optional<Seller> findByUser(Users user);
    Optional<Seller> findByUserEmail(String email);}
