package com.api19_4.api19_4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto{
    private String idUser;
    private String userName;
    private String password;
    private String email;
    private String phoneNumber;
    private String address;
    private String gender;
    private String avatar;
    private String background;
    private String roles;

}
