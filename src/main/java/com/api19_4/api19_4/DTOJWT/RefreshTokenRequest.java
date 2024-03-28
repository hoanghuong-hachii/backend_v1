package com.api19_4.api19_4.DTOJWT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    private String token;
}