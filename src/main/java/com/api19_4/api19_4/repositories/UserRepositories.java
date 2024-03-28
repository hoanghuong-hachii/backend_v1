package com.api19_4.api19_4.repositories;


import com.api19_4.api19_4.models.UserInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@EnableJpaRepositories
public interface UserRepositories extends JpaRepository<UserInfo, String> {
    Optional<UserInfo> findOneByEmailAndPassword(String email, String password);
    UserInfo findByUserName(String userName);

    UserInfo findByEmail(String email);

    List<UserInfo> findByPhoneNumber(String phoneNumber);

    Optional<UserInfo> findById(String idUser);
    @Query("SELECT COUNT(u) FROM UserInfo u")
    int countAllUsers();

    List<UserInfo> findByUserNameContainingIgnoreCase(String userName);

    Optional<UserInfo> findOneByUserNameAndPassword(String username, String encodedPassword);

//
//    List<User> findByphoneNumber(String phoneNumber);
}
