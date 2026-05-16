package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Enums.CategoryGame;
import com.backend.gamesales.Model.Game;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game,Long> {
    List<Game> findBySellerId(Long sellerId);
    List<Game> findByTagsId(Long tagId);
    boolean existsByTitleIgnoreCaseAndSellerId(String title, Long sellerId);
    List<Game> findByTitleContainingIgnoreCase(String title);
    List<Game> findByCategoryGame(CategoryGame categoryGame);
    List<Game> findByPriceLessThanEqual(Double price);
    Page<Game> findAll(Pageable pageable);


    @Query("""
      SELECT g FROM Game g
      WHERE (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:category IS NULL OR g.categoryGame = :category)
      AND (:price IS NULL OR g.price <= :price)
      """)
    List<Game> searchFilters(@Param("title") String name,
                             @Param("category") CategoryGame category,
                             @Param("price") Double price);
}
