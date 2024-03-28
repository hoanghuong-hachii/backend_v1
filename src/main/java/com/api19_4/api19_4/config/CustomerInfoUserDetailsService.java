//package com.api19_4.api19_4.config;
//
//
//import com.api19_4.api19_4.models.UserInfo;
//import com.api19_4.api19_4.repositories.UserRepositories;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CustomerInfoUserDetailsService implements UserDetailsService {
//    @Autowired
//    private UserRepositories userRepositories;
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserInfo user = userRepositories.findByUserName(username);
//
//        if (user == null) {
//            throw new UsernameNotFoundException("User not found: " + username);
//        }
//
//        return new CustomerInfoUserDetails(user);
//    }
//
//
//
//}
