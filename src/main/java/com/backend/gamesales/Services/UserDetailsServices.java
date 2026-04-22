package com.backend.gamesales.Services;

import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Setter
@Service
public class UserDetailsServices implements UserDetailsService {

     @Autowired
     private UsersRepository usersRepository;
    public UserDetails loadUserByUsername(String email)throws UsernameNotFoundException{
        return usersRepository.findByEmailWithRelations(email)
                .orElseThrow(()-> {
                    log.error("User not found with email {}", email);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
