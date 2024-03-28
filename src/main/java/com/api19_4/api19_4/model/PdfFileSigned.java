package com.api19_4.api19_4.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pdfSigned")
public class PdfFileSigned {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fileName")
    private String fileName;

    private String certSerialNumber;

    @Column(name = "note")
    private String note;

    private String date_register;


    @ManyToOne
    @JoinColumn(name = "digital_certificate_id")
    private DigitalCertificate digitalCertificate;

}
