package com.api19_4.api19_4.services;


import com.api19_4.api19_4.dto.LoginDto;
import com.api19_4.api19_4.dto.LoginUserDTO;
import com.api19_4.api19_4.dto.UserDto;

import com.api19_4.api19_4.entity.RefreshToken;

import com.api19_4.api19_4.generator.IDGenerator;
import com.api19_4.api19_4.models.ResponseObject;
import com.api19_4.api19_4.models.UserInfo;
import com.api19_4.api19_4.repositories.UserRepositories;

import com.api19_4.api19_4.serviceJWT.JwtService;
import com.api19_4.api19_4.serviceJWT.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Service
public class UserImpl implements UserService{
    @Autowired
    private UserRepositories userRepositories;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

//    @Override
//    public ResponseObject addUser(User user ) {
//
//        user = new User(
//                user.getId(),
//                user.getUserName(),
//                this.passwordEncoder.encode(user.getPassword()),
//                user.getEmail(),
//
//                user.getPhoneNumber(),
//                user.getGender()
//        );
//        userRepositories.save(user);
//        return new ResponseObject("true", "SignUp Success", "");
//    }
@Override
public ResponseObject addUser(UserDto userDto) {
    int numberOfExistingUser = userRepositories.countAllUsers();
    if(numberOfExistingUser == 0){
        numberOfExistingUser = 1;
    }else{
        numberOfExistingUser += 1;
    }
    IDGenerator idGenerator = new IDGenerator("US", numberOfExistingUser);
    UserInfo user = new UserInfo(idGenerator);
    user.setUserName(userDto.getUserName());
    user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));
    user.setEmail(userDto.getEmail());
    user.setPhoneNumber(userDto.getPhoneNumber());
    user.setAddress(userDto.getAddress());
    user.setGender(userDto.getGender());
    user.setRoles(userDto.getRoles());
    userRepositories.save(user);
    return new ResponseObject("true", "SignUp Success", "");
}

    //UserDto userDto;


    @Override
    public ResponseObject loginUser(LoginDto loginDto) {
        String msg = "";
        UserInfo user1 = userRepositories.findByUserName(loginDto.getUsername());
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        if (user1 != null) {
            String password = loginDto.getPassword();
            String encodedPassword = user1.getPassword();
            Boolean isPwdRight = passwordEncoder.matches(password, encodedPassword);
            if (isPwdRight) {
                Optional<UserInfo> employee = userRepositories.findOneByUserNameAndPassword(loginDto.getUsername(), encodedPassword);
                if (employee.isPresent()) {
                    System.out.println("employee " + employee.toString());
                    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
                    System.out.println("employee " + employee.toString());
                    if (authentication.isAuthenticated()){
                        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginDto.getUsername());
                        loginUserDTO.setAccessToken(jwtService.generateTokenUser(loginDto.getUsername()));
                        loginUserDTO.setExpirationTime(new Date(System.currentTimeMillis()+1000*60*2).toInstant());
                        loginUserDTO.setToken(refreshToken.getToken());
                        loginUserDTO.setIdUser(user1.getIdUser());
                        System.out.println("employee " + loginUserDTO.toString());
                    }
                    return new ResponseObject("true", "Login Success", loginUserDTO);

                } else {
                    return new ResponseObject("false", "Login Failed", "");
                }
            } else {

                return new ResponseObject("false", "password Not Match", "");

            }
        }else {
            return new ResponseObject("false", "Account not exits", "");
        }
    }

//    @Override
//    public ResponseObject loginUserSystem(LoginDto loginDto) throws UsernameNotFoundException {
//        UserInfo user = userRepositories.findByUserName(loginDto.getUsername());
//        if (user == null) {
//            throw new UsernameNotFoundException("User not found with username: " + loginDto.getUsername());
//        }
//        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), new ArrayList<>());
////        return new org.springframework.security.core.userdetails.User(
////                user.getUserName(), user.getPassword(), new ArrayList<>()
////        );
//    }
}

