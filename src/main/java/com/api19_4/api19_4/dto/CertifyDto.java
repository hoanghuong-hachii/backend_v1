package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.models.Unit;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CertifyDto {

    private Long id;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String issued;
    private String date_register;
    private String expried;
    private String companyName;
    private String status;

}
