package com.api19_4.api19_4.UserSystemSQL;

import com.api19_4.api19_4.dto.LoginDto;
import com.api19_4.api19_4.dto.LoginUserDTO;
import com.api19_4.api19_4.dto.UserDto;
import com.api19_4.api19_4.entity.RefreshToken;
import com.api19_4.api19_4.models.ResponseObject;
import com.api19_4.api19_4.models.UserInfo;
import com.api19_4.api19_4.repositories.UserRepositories;
import com.api19_4.api19_4.serviceJWT.JwtService;
import com.api19_4.api19_4.serviceJWT.RefreshTokenService;
import com.api19_4.api19_4.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class AuthController {
    @Autowired
    private JwtService jwtService;

    private Long loginTimeMillis;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepositories repositories;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseObject login(@RequestBody LoginDto loginRequest) {
        boolean isAuthenticated = authService.authenticateSA(loginRequest.getUsername(), loginRequest.getPassword());
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        if (isAuthenticated) {
            String token = jwtService.generateToken(loginRequest.getUsername());
            loginUserDTO.setAccessToken(token);
            loginTimeMillis = System.currentTimeMillis();
            return new ResponseObject("true", "Login Success", loginUserDTO);
        } else {
            return new ResponseObject("false", "Login Failed", "");
        }
    }

    @PostMapping(path = "/signupAdmin")
    public ResponseEntity<?> signupAdmin(@RequestBody UserDto userDto) {
        // Kiểm tra đăng nhập
        Authentication authentication = checkLogin();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject("failed", "Unauthorized access", "")
            );
        }

        // Kiểm tra trùng lặp thông tin đăng ký
        UserInfo foundUsers = repositories.findByUserName(userDto.getUserName().trim());
        if (foundUsers != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("failed", "User name already taken", "")
            );
        }

        List<UserInfo> foundPhone = repositories.findByPhoneNumber(userDto.getPhoneNumber().trim());
        if (!foundPhone.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("failed", "Phone number already taken", "")
            );
        }

        UserInfo foundUser = repositories.findByEmail(userDto.getEmail().trim());
        if (foundUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("failed", "Email already taken", "")
            );
        }

        // Kiểm tra thời gian giữa đăng nhập và đăng ký admin
        //long lastLoginTime = authService.getLastSuccessfulLogin(authentication.getName());
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - loginTimeMillis;
        long allowedTimeDifference = 5 * 60 * 1000; // 5 phút expressed in milliseconds

        if (timeDifference > allowedTimeDifference) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject("failed", "Time limit exceeded for admin registration", "")
            );
        }

        // Thực hiện đăng ký admin
        ResponseObject signupMessage = userService.addUser(userDto);
        return ResponseEntity.ok(signupMessage);
    }

    // Phương thức kiểm tra đăng nhập
    private Authentication checkLogin() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}