package com.api19_4.api19_4.config;

import com.api19_4.api19_4.UserSystemSQL.AuthService;
import com.api19_4.api19_4.filter.JwtAuthFilter;
//import com.api19_4.api19_4.filter.JwtAuthFilterCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthFilter authFilter;
//    @Autowired
//    private JwtAuthFilterCustomer authFilterCustomer;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoUserDetailsService();
    }
//    @Bean
//    public UserDetailsService customerDetailsService() {
//        return new CustomerInfoUserDetailsService();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return  http.cors().and().csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("api/v1/signDigital/**","api/v1/register-certificates/**", "api/v1/digital-certificates/**","api/v10/pdf/**","/api/v2/users/getPasswordById","/api/v2/users/deleteAllUsers","/api/active/**","/api/testOTP/**","/api/v2/users/getUsernameByEmail","/api/v2/users/changePassword","/api/phoneNumber/**","/api/system/login","api/salts/**","api/salts","/api/v9/pay/**","/api/v9/pay","/notification/token/**","/send-notification","/register-device").permitAll()
               // .requestMatchers("/api/v2/users/signupAdmin").authenticated()
                .requestMatchers(
                        //"/products/signUp", "/products/login", "/products/refreshToken",
                        "/api/v2/users/signupUser", "/api/v2/users/refreshToken","/api/v2/users/login","/api/system/**",
                        "/swagger-resources", "/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**","/images/**",
                        "/swagger-ui.html", "/webjars/**", "/configuration/ui", "/webjars/swagger-ui/**", "/swagger-ui/index.html"
                ).permitAll()
                .requestMatchers(
                        "/api/v5/Bill/statusUpdates","/api/v5/Bill/bills/**","/api/v5/Bill/GetStatus","/api/v5/Bill/**","api/v1/Products/updateProduct/**", "/api/v1/Products/delete",
                        "/api/v3/order/**","/api/v6/ProdBill/**","/api/v6/ProdLike/**","/api/v4/shoppingCart/**",
                        "/api/v1/Products/roleUser/**", "/api/v2/users/id","/api/v2/users/updateUser/**","/api/v1/warehouses/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(
                        "/api/v5/Bill/**","/api/v3/order/**","/api/v1/ManagerWH/**","/api/v6/ProdBill/**",
                        "/api/v1/Products**","/api/v1/Products/**","/api/v6/ProdLike/**","/api/v1/purchaseOrder/**","/api/v1/productStandard/**", "/api/v5/InventoryCheck/**",
                        "/api/v4/shoppingCart/**","/api/v1/supplier/**","/api/v2/users/roleAdmin/**","/api/v2/users/id","/api/v2/users/updateUser/**"
                        ).hasRole("ADMIN")
                .and()
              //  .authorizeHttpRequests().requestMatchers("/products/**", "/api/v1/Products/**", "/api/v2/users/**")
               // .authenticated().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

//    @Bean
//    public AuthenticationProvider authenticationProviderCustomer(){
//        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
//        authenticationProvider.setUserDetailsService(customerDetailsService());
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//        return authenticationProvider;
//    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}