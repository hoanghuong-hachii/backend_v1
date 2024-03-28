package com.api19_4.api19_4.dto_sign;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DigitalCertificateDTO {
    private String certSerialNumber;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String issued;
    private String dateRegister;
    private String expired;
    private String companyName;
}
