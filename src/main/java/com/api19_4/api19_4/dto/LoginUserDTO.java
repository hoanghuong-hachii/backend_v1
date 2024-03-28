package com.api19_4.api19_4.dto;

import lombok.*;

import java.time.Instant;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginUserDTO {
    private String idUser;
    private String accessToken;
    private String token;
    private Instant expirationTime;
}
