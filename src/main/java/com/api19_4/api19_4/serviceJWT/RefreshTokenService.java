package com.api19_4.api19_4.serviceJWT;

import com.api19_4.api19_4.entity.RefreshToken;
import com.api19_4.api19_4.repositories.UserRepositories;
import com.api19_4.api19_4.repositoriesJWT.RefreshTokenRepository;
//import com.api19_4.api19_4.repositoriesJWT.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    //private UserInfoRepository userInfoRepository;
    private UserRepositories repositories;
    @Autowired
    private UserRepositories customerRepositories;

    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(repositories.findByUserName(username))
              //  .userInfo(repositories.findByUserName(username))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(Duration.ofDays(1))) // 1 giờ
                // .expiryDate(Instant.now().plusMillis(600000000))//10
               // .expiryDate(Instant.now().plus(Duration.ofDays(1))) // Set expiry to one day
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

//    public RefreshToken createRefreshTokenCustomer(String username) {
//        RefreshToken refreshToken = RefreshToken.builder()
//                .user(customerRepositories.findByUserName(username))
//                //.userInfo(customerRepositories.findByEmail(email))
//                .token(UUID.randomUUID().toString())
//                .expiryDate(Instant.now().plusMillis(600000))//10
//                .build();
//        return refreshTokenRepository.save(refreshToken);
//    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

}