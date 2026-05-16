package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Enums.RequestStatus;
import com.backend.gamesales.Model.PromotionProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestRepository extends JpaRepository<PromotionProposal, Long> {
    List<PromotionProposal> findBySellerId(Long sellerId);
    boolean existsByGameIdAndStatus(Long gameId, RequestStatus status);

}