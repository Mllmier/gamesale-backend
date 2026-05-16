package com.backend.gamesales.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.gamesales.Model.Tags;
@Repository
public interface TagRepository extends JpaRepository<Tags,Long> {
    Optional<Tags> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
