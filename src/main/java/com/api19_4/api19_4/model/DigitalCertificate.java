package com.api19_4.api19_4.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "DigitalCertificate")
public class DigitalCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String certSerialNumber;
    @Column( columnDefinition = "nvarchar(1000)")
    private String name;
    private String password;
    private String email;
    private String phone;
    private String issued;
    private String date_register;
    private String expried;
    @Column( columnDefinition = "nvarchar(1000)")
    private String companyName;

    @OneToMany(mappedBy = "digitalCertificate")
    private List<PdfFileSigned> pdfSigned;

}
