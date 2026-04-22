package com.backend.gamesales.Services;


import com.backend.gamesales.Dto.AuthResponse;
import com.backend.gamesales.Dto.RegisterRequest;
import com.backend.gamesales.Model.Profile;
import com.backend.gamesales.Model.Enums.Role;
import com.backend.gamesales.Model.RefreshToken;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UsersRepository;
import com.backend.gamesales.Utils.PasswordValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;


    @Autowired
    RefreshTokenService refreshTokenService;
    public AuthResponse  authenticate(String email, String password){
            Users users =usersRepository.findByEmail(email)
                    .orElseThrow(()-> new RuntimeException("User not found"));

            if(!passwordEncoder.matches(password, users.getPassword())){
                throw new RuntimeException("Incorrect Password");
            }

            Map<String,Object> extraClaims=new HashMap<>();
            extraClaims.put("email",users.getEmail());
            extraClaims.put("role", users.getRole());
             String token=jwtService.generateToken(extraClaims,users);
             RefreshToken refreshToken = refreshTokenService.createRefreshToken(users);
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .role(users.getRole())
                .email(users.getEmail())
                .userId(users.getId())
                .build();
        }
        public AuthResponse  register(RegisterRequest request){
            if(usersRepository.findByEmail(request.getEmail()).isPresent()){
                throw  new RuntimeException("Email already registered");
            }

            if (!PasswordValidator.isValid(request.getPassword())) {
                throw new RuntimeException(
                        "Password must be at least 12 characters long and include " +
                                "uppercase, lowercase, a number and a special character"
                );
            }
            Users users=new Users();
            users.setBirthday(request.getBirthday());
            users.setEmail(request.getEmail());
            users.setPassword(passwordEncoder.encode(request.getPassword()));
            users.setRole(Role.CUSTOMER);
            Users savedUser = usersRepository.save(users);

            Profile profile = new Profile();
            profile.setFirstName(request.getName());
            profile.setLastName(request.getLastName());
            profile.setUser(savedUser);
            users.setProfile(profile);
            savedUser.setProfile(profile);
            usersRepository.save(savedUser);

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("email", users.getEmail());
            extraClaims.put("role", users.getRole());
            String jwtToken = jwtService.generateToken(extraClaims, users);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(users);
            emailService.sendWelcomeEmail(users.getEmail(), request.getName());

            return buildAuthResponse(users, refreshToken, jwtToken);
        }
       public AuthResponse buildAuthResponse(Users users, RefreshToken oldToken) {
          RefreshToken newRefresh = refreshTokenService.rotateRefreshToken(oldToken);

          Map<String, Object> extraClaims = new HashMap<>();
          extraClaims.put("email", users.getEmail());
          extraClaims.put("role", users.getRole());

          String jwt = jwtService.generateToken(extraClaims, users);
          return buildAuthResponse(users, newRefresh, jwt);
       }

       private AuthResponse buildAuthResponse(Users users, RefreshToken refresh, String jwt) {
           return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refresh.getToken())
                .email(users.getEmail())
                .role(users.getRole())
                   .name(users.getProfile() != null ? users.getProfile().getFirstName() : null)
                   .build();
       }

    }
