package com.api19_4.api19_4.entity;

import com.api19_4.api19_4.models.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String token;

    private Instant expiryDate;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    private UserInfo userInfo;

    @OneToOne
    @JoinColumn(name = "idUser", referencedColumnName = "idUser")
    private UserInfo user;

}