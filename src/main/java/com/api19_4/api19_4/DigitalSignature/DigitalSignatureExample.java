package com.api19_4.api19_4.DigitalSignature;

import java.security.cert.Certificate;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.io.IOException;
public class DigitalSignatureExample {
    private static KeyPair keyPair; // Declare the KeyPair as a class variable

    public static void generateKeyPairAndStore(String keyStorePath, String keyAlias, String keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {

        // Tạo cặp khóa RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(2048); // Độ dài của khóa
        keyPair = keyPairGenerator.generateKeyPair(); // Lưu cặp khóa

        // Tạo chứng chỉ tự ký để tạo chuỗi chứng chỉ
        X509Certificate selfSignedCert = null;
        try {
            selfSignedCert = generateSelfSignedCertificate(keyPair);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }

        // Lưu cặp khóa và chứng chỉ vào keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null); // Load keystore trống
        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(keyPassword.toCharArray());

        // Tạo chuỗi chứng chỉ
        Certificate[] certificateChain = new Certificate[1];
        certificateChain[0] = selfSignedCert;

        // Tạo đối tượng PrivateKeyEntry với chuỗi chứng chỉ
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), certificateChain);
        keyStore.setEntry(keyAlias, privateKeyEntry, keyStorePP);

        try (FileOutputStream fos = new FileOutputStream(keyStorePath)) {
            keyStore.store(fos, keyPassword.toCharArray());
        }
    }

    // Phương thức để tạo chứng chỉ tự ký
    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair)
            throws CertificateEncodingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {

        // Create a self-signed X.509 certificate using X509v3CertificateBuilder
        X509Certificate cert;
        try {
            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    new X500Principal("CN=SelfSigned"),
                    BigInteger.valueOf(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L), // 1 year validity
                    new X500Principal("CN=SelfSigned"),
                    keyPair.getPublic()
            );

            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withDSA").build(keyPair.getPrivate());

            cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
        } catch (CertificateException | OperatorCreationException e) {
            throw new RuntimeException("Error generating self-signed certificate", e);
        }

        return cert;
    }
    public static PrivateKey getPrivateKeyFromKeyStore(String keyStorePath, String keyAlias, String keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keyStorePath)) {
            keyStore.load(fis, keyPassword.toCharArray());
        }

        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(keyPassword.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, keyStorePP);
        return privateKeyEntry.getPrivateKey();
    }

    public static byte[] signData(PrivateKey privateKey, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withDSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static boolean verifySignature(byte[] signature, PublicKey publicKey, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withDSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }
    private static String hashPDF(String filePath) {
        try {
            // Đọc nội dung của file PDF thành mảng byte
            byte[] pdfContent = readFileToByteArray(filePath);

            // Tạo đối tượng MessageDigest với thuật toán SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Áp dụng thuật toán băm lên mảng byte của file PDF
            byte[] hashedBytes = md.digest(pdfContent);

            // Chuyển đổi mảng byte thành chuỗi hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] readFileToByteArray(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[1024];
        int bytesRead;
        StringBuilder content = new StringBuilder();
        while ((bytesRead = fis.read(buffer)) != -1) {
            content.append(new String(buffer, 0, bytesRead));
        }
        fis.close();
        return content.toString().getBytes();
    }

    public static void main(String[] args) {
        try {
            String keyStorePath = "C:\\Users\\Admin\\Downloads\\keystore.jks";
            String keyAlias = "test1";
            String keyPassword = "key1";
            String pdfFilePath = "C:\\Users\\Admin\\Downloads\\test1.pdf";

            // Tạo cặp khóa và lưu vào keystore
            generateKeyPairAndStore(keyStorePath, keyAlias, keyPassword);

            // Lấy khóa riêng từ keystore
            PrivateKey privateKey = getPrivateKeyFromKeyStore(keyStorePath, keyAlias, keyPassword);

            // Đọc nội dung của file PDF
            byte[] pdfContent = Files.readAllBytes(Paths.get(pdfFilePath));

            String hashedData = hashPDF("C:\\Users\\Admin\\Downloads\\test1.pdf");
            byte[] pdfC = hashedData.getBytes();
            // Ký và lưu chữ ký vào file
            byte[] signature = signData(privateKey, pdfC);
            Files.write(Paths.get("C:\\Users\\Admin\\Downloads\\signed_file1.pdf"), signature);

            // Đọc chữ ký từ file và xác minh
            byte[] readSignature = Files.readAllBytes(Paths.get("C:\\Users\\Admin\\Downloads\\signed_file1.pdf"));
            PublicKey publicKey = keyPair.getPublic();
            boolean isVerified = verifySignature(readSignature, publicKey, pdfC);

            if (isVerified) {
                System.out.println("Digital signature is valid.");
            } else {
                System.out.println("Digital signature is not valid.");
            }

//            // Đọc nội dung của file PDF
//            byte[] pdfContent = Files.readAllBytes(Paths.get(pdfFilePath));
//
//            // Băm nội dung của file PDF
//            String hashedData = hashPDF(pdfFilePath);
//            byte[] pdfHash = hashedData.getBytes();
//
//            // Ký và lưu chữ ký vào file
//            byte[] signature = signData(privateKey, pdfHash);
//
//            // Ghép chữ ký vào nội dung PDF
//            byte[] signedPdfContent = new byte[pdfContent.length + signature.length];
//            System.arraycopy(pdfContent, 0, signedPdfContent, 0, pdfContent.length);
//            System.arraycopy(signature, 0, signedPdfContent, pdfContent.length, signature.length);
//
//            // Lưu nội dung PDF đã ký vào file
//            Files.write(Paths.get("C:\\Users\\Admin\\Downloads\\signed_file1.pdf"), signedPdfContent);
//
//            // Đọc chữ ký từ file và xác minh
//            int pdfLength = pdfContent.length;
//            byte[] readSignature = Arrays.copyOfRange(signedPdfContent, pdfLength, signedPdfContent.length);
//            boolean isVerified = verifySignature(readSignature, keyPair.getPublic(), pdfHash);
//
//            if (isVerified) {
//                System.out.println("Digital signature is valid.");
//            } else {
//                System.out.println("Digital signature is not valid.");
//            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
