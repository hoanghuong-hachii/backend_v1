package com.api19_4.api19_4.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name="deviceToken")
public class TokenDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idToken;

    @Column(name = "idUser")
    private String idUser;

    @Column(name = "token")
    private String tokenDevice;
}
