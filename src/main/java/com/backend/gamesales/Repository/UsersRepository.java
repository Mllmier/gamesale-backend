package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Enums.Role;
import com.backend.gamesales.Model.Users;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users,Long>  {

    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role rol);
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.profile LEFT JOIN FETCH u.seller WHERE u.email = :email")
    Optional<Users> findByEmailWithRelations(@NotNull String email);
    @Modifying
    @Transactional
    @Query("UPDATE Users u SET u.password = :password WHERE u.email = :email")
    void updatePassword(@Param("email") String email,
                        @Param("password") String password);


}


