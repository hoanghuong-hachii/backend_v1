package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto_sign.DigitalCertificateDTO;
import com.api19_4.api19_4.model.DigitalCertificate;
import com.api19_4.api19_4.repositories.DigitalCertificateRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/digital-certificates")
public class DigitalSignController {

    @Autowired
    private DigitalCertificateRepository digitalCertificateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/all")
    public ResponseEntity<List<DigitalCertificateDTO>> getAllDigitalCertificates() {
        List<DigitalCertificate> digitalCertificates = digitalCertificateRepository.findAll();
        List<DigitalCertificateDTO> digitalCertificateDTOs = digitalCertificates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(digitalCertificateDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalCertificateDTO> getDigitalCertificateById(@PathVariable Long id) {
        Optional<DigitalCertificate> digitalCertificateOptional = digitalCertificateRepository.findById(id);
        if (digitalCertificateOptional.isPresent()) {
            DigitalCertificate digitalCertificate = digitalCertificateOptional.get();
            DigitalCertificateDTO digitalCertificateDTO = convertToDTO(digitalCertificate);
            return new ResponseEntity<>(digitalCertificateDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerDigitalCertificate(@RequestBody DigitalCertificateDTO digitalCertificateDTO) throws Exception {

        Optional<DigitalCertificate> lastCertificate = digitalCertificateRepository.findFirstByOrderByCertSerialNumberDesc();
        Long newSerialNumber = lastCertificate.map(cert -> Long.parseLong(cert.getCertSerialNumber().substring(2)) + 1).orElse(1L);
        String certSerialNumber = "DS" + String.format("%05d", newSerialNumber);

        DigitalCertificate digitalCertificate = convertToEntity(digitalCertificateDTO);

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expiryTime = currentTime.plusYears(10);
        Date timeRegister = Date.from(currentTime.toInstant(ZoneOffset.UTC));
        Date dateTimeExpired = Date.from(expiryTime.toInstant(ZoneOffset.UTC));
        digitalCertificate.setDate_register(timeRegister.toString());
        digitalCertificate.setExpried(dateTimeExpired.toString());
        digitalCertificate.setCertSerialNumber(certSerialNumber);


        //=======sign==========
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = generateKeyPair();

        // Save private key in PKCS#12 keystore
        String alias = digitalCertificate.getCertSerialNumber();
        String passwd = digitalCertificate.getPassword();
        savePrivateKeyToKeystore(keyPair, "keystorecer.p12", passwd, alias);

        digitalCertificateRepository.save(digitalCertificate);
        return new ResponseEntity<>("Digital certificate registered successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDigitalCertificate(@PathVariable Long id) {
        if (digitalCertificateRepository.existsById(id)) {
            digitalCertificateRepository.deleteById(id);
            return new ResponseEntity<>("Digital certificate deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Digital certificate not found", HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllDigitalCertificates() {
        digitalCertificateRepository.deleteAll();
        return new ResponseEntity<>("All digital certificates deleted successfully", HttpStatus.OK);
    }
    private DigitalCertificateDTO convertToDTO(DigitalCertificate digitalCertificate) {
        return modelMapper.map(digitalCertificate, DigitalCertificateDTO.class);
    }

    private DigitalCertificate convertToEntity(DigitalCertificateDTO digitalCertificateDTO) {
        return modelMapper.map(digitalCertificateDTO, DigitalCertificate.class);
    }

    //==============
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    private static void savePrivateKeyToKeystore(KeyPair keyPair, String keystorePath, String keystorePassword, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        X509Certificate cert = generateSelfSignedCertificate(keyPair);

        keyStore.setKeyEntry(alias, keyPair.getPrivate(), keystorePassword.toCharArray(), new java.security.cert.Certificate[]{cert});

        // Save the keystore to a file
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal subjectName = new X500Principal("CN=SelfSigned");

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(subjectName);
        certGen.setIssuerDN(subjectName);
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30)); // 30 days before now
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10)); // 10 years from now
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        return certGen.generate(keyPair.getPrivate(), "BC");
    }
}
