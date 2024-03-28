package com.api19_4.api19_4.services;

import com.api19_4.api19_4.dto.LoginDto;
import com.api19_4.api19_4.dto.UserDto;
import com.api19_4.api19_4.models.ResponseObject;

public interface UserService {


//
//    ResponseObject addUser(User user);
    ResponseObject addUser(UserDto userDto);
    ResponseObject loginUser(LoginDto loginDto);

   // ResponseObject loginUserSystem(LoginDto loginDto);
}
