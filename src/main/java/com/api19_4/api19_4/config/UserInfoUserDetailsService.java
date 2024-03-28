package com.api19_4.api19_4.config;

//import com.api19_4.api19_4.entity.UserInfo;
import com.api19_4.api19_4.models.UserInfo;
import com.api19_4.api19_4.repositories.UserRepositories;
//import com.api19_4.api19_4.repositoriesJWT.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserInfoUserDetailsService implements UserDetailsService {

    @Autowired
   // private UserInfoRepository repository;
    private UserRepositories repositories;

    @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = repositories.findByUserName(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new UserInfoUserDetails(user);
    }
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserInfo userInfo = repositories.findByUserName(username);
//        return userInfo.map(UserInfoUserDetails::new)
//                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
//
//    }

}