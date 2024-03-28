package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.dto.*;
import com.api19_4.api19_4.model.DigitalCertificate;
import com.api19_4.api19_4.model.RgisterCertificate;
import com.api19_4.api19_4.repositories.DigitalCertificateRepository;
import com.api19_4.api19_4.repositories.RegisterCertificateRepository;
import com.api19_4.api19_4.util.SearchUtil;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/register-certificates")
public class RegisterSignController {
    private static final int MAX_ATTEMPTS = 3;
    @Autowired
    private RegisterCertificateRepository digitalCertificateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/all")
    public ResponseEntity<List<RgisterCertificate>> getAllDigitalCertificates() {
        List<RgisterCertificate> digitalCertificates = digitalCertificateRepository.findAll();

        return new ResponseEntity<>(digitalCertificates, HttpStatus.OK);
    }
    @PutMapping("/{id}/update-status")
    public ResponseEntity<String> updateCertificateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<RgisterCertificate> certificateOptional = digitalCertificateRepository.findById(id);
        if (certificateOptional.isPresent()) {
            RgisterCertificate certificate = certificateOptional.get();
            certificate.setStatus(status);
            if(status.equals("Active")){
                certificate.setWrongAttempts(0);
            }
            digitalCertificateRepository.save(certificate);
            return new ResponseEntity<>("Status updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Certificate not found", HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/status-count")
    public ResponseEntity<Map<String, Integer>> getCertificateStatusCount() {
        // Khởi tạo một HashMap để lưu số lượng cho mỗi trạng thái
        Map<String, Integer> statusCount = new HashMap<>();

        // Lấy tất cả các bản ghi từ cơ sở dữ liệu
        List<RgisterCertificate> certificates = digitalCertificateRepository.findAll();

        // Đếm số lượng bản ghi cho mỗi trạng thái
        for (RgisterCertificate certificate : certificates) {
            String status = certificate.getStatus();
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        // Trả về kết quả
        return new ResponseEntity<>(statusCount, HttpStatus.OK);
    }
    @GetMapping("/all/pagition")
    public ResponseEntity<PageResponse<RgisterCertificate>> getAllDigitalCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RgisterCertificate> digitalCertificatesPage = digitalCertificateRepository.findAll(PageRequest.of(page, size));
        PageResponse<RgisterCertificate> pageResponse = new PageResponse<>(digitalCertificatesPage);
        return new ResponseEntity<>(pageResponse, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RgisterCertificate>> searchCertificates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String status
    ) {
        List<RgisterCertificate> certificates;

        if (name != null && !name.isEmpty()) {
            certificates = digitalCertificateRepository.findByNameContaining(name);
        } else if (id != null) {
            Optional<RgisterCertificate> certificateOptional = digitalCertificateRepository.findById(id);
            certificates = certificateOptional.map(List::of).orElse(List.of());
        } else if (status != null && !status.isEmpty()) {
            certificates = digitalCertificateRepository.findByStatus(status);
        } else {
            // If none of the parameters are provided, return all certificates
            certificates = digitalCertificateRepository.findAll();
        }

        if (certificates.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(certificates, HttpStatus.OK);
        }
    }
    @GetMapping("/active")
    public ResponseEntity<List<RgisterCertificate>> getActiveCertificates() {
        List<RgisterCertificate> activeCertificates = digitalCertificateRepository.findByStatus("Active");

        if (activeCertificates.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(activeCertificates, HttpStatus.OK);
        }
    }


//    @GetMapping("/search-pagition")
//    public ResponseEntity<Page<RgisterCertificate>> searchCertificates(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) Long id,
//            @RequestParam(required = false) String status,
//            Pageable pageable
//    ) {
//        Page<RgisterCertificate> certificates;
//
//        if (name != null && !name.isEmpty()) {
//            certificates = digitalCertificateRepository.findByNameContaining(name, pageable);
//        } else if (id != null) {
//            Optional<RgisterCertificate> certificateOptional = digitalCertificateRepository.findById(id);
//            certificates = certificateOptional.map(cert -> new PageImpl<>(Collections.singletonList(cert), pageable, 1))
//                    .orElse(new PageImpl<>(Collections.emptyList(), pageable, 0));
//        } else if (status != null && !status.isEmpty()) {
//            certificates = digitalCertificateRepository.findByStatus(status, pageable);
//        } else {
//            // If none of the parameters are provided, return all certificates
//            certificates = digitalCertificateRepository.findAll(pageable);
//        }
//
//        if (certificates.isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        } else {
//            return new ResponseEntity<>(certificates, HttpStatus.OK);
//        }
//    }
    @GetMapping("/{id}")
    public ResponseEntity<RgisterCertificate> getDigitalCertificateById(@PathVariable Long id) {
        Optional<RgisterCertificate> digitalCertificateOptional = digitalCertificateRepository.findById(id);
        if (digitalCertificateOptional.isPresent()) {
            RgisterCertificate digitalCertificate = digitalCertificateOptional.get();
            return new ResponseEntity<>(digitalCertificate, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerDigitalCertificate(@RequestBody RgisterCertificate rgisterCertificate) throws Exception {

        Optional<RgisterCertificate> lastCertificate = digitalCertificateRepository.findFirstByOrderByCertSerialNumberDesc();
        Long newSerialNumber = 1L;
        if (lastCertificate.isPresent() && lastCertificate.get().getCertSerialNumber() != null) {
            newSerialNumber = Long.parseLong(lastCertificate.get().getCertSerialNumber().substring(2)) + 1;
        }
        String certSerialNumber = "DS" + String.format("%05d", newSerialNumber);
        RgisterCertificate digitalCertificate = rgisterCertificate;

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expiryTime = currentTime.plusYears(10);
        Date timeRegister = Date.from(currentTime.toInstant(ZoneOffset.UTC));
        Date dateTimeExpired = Date.from(expiryTime.toInstant(ZoneOffset.UTC));
        digitalCertificate.setDate_register(timeRegister.toString());
        digitalCertificate.setExpried(dateTimeExpired.toString());
        digitalCertificate.setStatus("Pending");
        digitalCertificate.setIssued("FAST CA SHA-256");
        digitalCertificate.setCertSerialNumber(certSerialNumber);

        //=======sign==========
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = generateKeyPair();

        // Save private key in PKCS#12 keystore
        String alias = digitalCertificate.getCertSerialNumber();
        String passwd = digitalCertificate.getPassword();
        String name =removeDiacritics(digitalCertificate.getName()) ;

        String company = digitalCertificate.getCompanyName();
        savePrivateKeyToKeystore(keyPair, "keystorecer.p12", passwd, alias,company, name);

        digitalCertificateRepository.save(digitalCertificate);
        return new ResponseEntity<>("registered successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDigitalCertificate(@PathVariable Long id) {
        if (digitalCertificateRepository.existsById(id)) {
            digitalCertificateRepository.deleteById(id);
            return new ResponseEntity<>("deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("certificate not found", HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllDigitalCertificates() {
        digitalCertificateRepository.deleteAll();
        return new ResponseEntity<>("All digital certificates deleted successfully", HttpStatus.OK);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<String> verifyPassword(@RequestParam Long id, @RequestParam String password) {
        Optional<RgisterCertificate> certificateOptional = digitalCertificateRepository.findById(id);
        if (certificateOptional.isPresent()) {
            RgisterCertificate certificate = certificateOptional.get();
            if (password.equals(certificate.getPassword())) {
                // Mật khẩu đúng, reset số lần nhập sai
                certificate.setWrongAttempts(0);
                digitalCertificateRepository.save(certificate);
                return new ResponseEntity<>("Password verified successfully", HttpStatus.OK);
            } else {
                // Mật khẩu sai, tăng số lần nhập sai
                int wrongAttempts = certificate.getWrongAttempts() + 1;
                certificate.setWrongAttempts(wrongAttempts);
                digitalCertificateRepository.save(certificate);

                // Kiểm tra số lần nhập sai
                if (wrongAttempts >= MAX_ATTEMPTS) {
                    // Quá số lần nhập sai, khóa tài khoản
                    certificate.setStatus("Suspended");
                    digitalCertificateRepository.save(certificate);
                    return new ResponseEntity<>("Account locked due to too many wrong attempts", HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<>("Wrong password "+ wrongAttempts, HttpStatus.UNAUTHORIZED);
                }
            }
        } else {
            return new ResponseEntity<>("Certificate not found", HttpStatus.NOT_FOUND);
        }
    }

    //==============
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    private static void savePrivateKeyToKeystore(KeyPair keyPair, String keystorePath, String keystorePassword, String alias,String company, String name) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        X509Certificate cert = generateSelfSignedCertificate(keyPair,company,name);

        keyStore.setKeyEntry(alias, keyPair.getPrivate(), keystorePassword.toCharArray(), new java.security.cert.Certificate[]{cert});

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String company, String name) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal subjectName = new X500Principal("CN="+name+" \n"+ company);

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(subjectName);
        certGen.setIssuerDN(subjectName);
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        return certGen.generate(keyPair.getPrivate(), "BC");
    }

    public static String removeDiacritics(String input) {
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizedString).replaceAll("");
    }

}
