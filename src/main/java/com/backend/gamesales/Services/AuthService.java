package com.backend.gamesales.Services;


import com.backend.gamesales.Dto.RegisterRequest;
import com.backend.gamesales.Model.Profile;
import com.backend.gamesales.Model.Role;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

        public Users authenticate(String email, String password){
            Users users =usersRepository.findByEmail(email)
                    .orElseThrow(()-> new RuntimeException("User not found"));

            if(!passwordEncoder.matches(password, users.getPassword())){
                throw new RuntimeException("Incorrect Password");
            }
            return users;
        }
        public Users register(RegisterRequest request){
            if(usersRepository.findByEmail(request.getEmail()).isPresent()){
                throw  new RuntimeException("Email already registered");
            }
            Users users=new Users();
            users.setName(request.getName());
            users.setBirthday(request.getBirthday());
            users.setEmail(request.getEmail());
            users.setPassword(passwordEncoder.encode(request.getPassword()));
            users.setRole(Role.CUSTOMER);

            Profile profile = new Profile();
            profile.setFirstName(request.getName());
            profile.setLastName(request.getLastName());
            users.setProfile(profile);
            Users savedUser=usersRepository.save(users);

            return  savedUser;
        }

        }
