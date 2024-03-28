package com.api19_4.api19_4.DigitalSignature;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class PDFSignatureExample {

    private static KeyPair keyPair;

    public static void generateKeyPairAndStore(String keyStorePath, String keyAlias, String keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();

        X509Certificate selfSignedCert = null;
        try {
            selfSignedCert = generateSelfSignedCertificate(keyPair);
        } catch (NoSuchProviderException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        Certificate[] certificateChain = new Certificate[1];
        certificateChain[0] = selfSignedCert;

        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(),
                certificateChain);
        keyStore.setEntry(keyAlias, privateKeyEntry, new KeyStore.PasswordProtection(keyPassword.toCharArray()));

        try (FileOutputStream fos = new FileOutputStream(keyStorePath)) {
            keyStore.store(fos, keyPassword.toCharArray());
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair)
            throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException,
            SignatureException {

        X509Certificate cert;
        try {
            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(new X500Principal("CN=SelfSigned"),
                    BigInteger.valueOf(System.currentTimeMillis()), new Date(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L),
                    new X500Principal("CN=SelfSigned"), keyPair.getPublic());

            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withDSA").build(keyPair.getPrivate());

            cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
        } catch (OperatorCreationException e) {
            throw new RuntimeException("Error generating self-signed certificate", e);
        }

        return cert;
    }

    public static PrivateKey getPrivateKeyFromKeyStore(String keyStorePath, String keyAlias, String keyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
            UnrecoverableEntryException {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keyStorePath)) {
            keyStore.load(fis, keyPassword.toCharArray());
        }

        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(keyPassword.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias,
                keyStorePP);
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
            byte[] pdfContent = readFileToByteArray(filePath);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(pdfContent);

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
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        while ((bytesRead = fis.read(buffer)) != -1) {
            content.write(buffer, 0, bytesRead);
        }
        fis.close();
        return content.toByteArray();
    }

    private static byte[] serializeSignatureInfo(SignatureInfo info) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(info);
            return bos.toByteArray();
        }
    }

    private static SignatureInfo deserializeSignatureInfo(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (SignatureInfo) ois.readObject();
        }
    }

    public static void main(String[] args) {
        try {
            String keyStorePath = "C:\\Users\\Admin\\Downloads\\keystore.jks";
            String keyAlias = "test1";
            String keyPassword = "key1";
            String pdfFilePath = "C:\\Users\\Admin\\Downloads\\test1.pdf";

            generateKeyPairAndStore(keyStorePath, keyAlias, keyPassword);
            PrivateKey privateKey = getPrivateKeyFromKeyStore(keyStorePath, keyAlias, keyPassword);

            byte[] pdfContent = Files.readAllBytes(Paths.get(pdfFilePath));
            String hashedData = hashPDF(pdfFilePath);
            byte[] pdfHash = hashedData.getBytes();

            byte[] signature = signData(privateKey, pdfHash);

            // Create and serialize the signature information
            SignatureInfo signatureInfo = new SignatureInfo(new Date(), "CT CPTMDD Thang Long");
            byte[] serializedInfo = serializeSignatureInfo(signatureInfo);

            // Append the serialized information after the signature
            byte[] signedPdfContent = new byte[pdfContent.length + signature.length + serializedInfo.length];
            System.arraycopy(pdfContent, 0, signedPdfContent, 0, pdfContent.length);
            System.arraycopy(signature, 0, signedPdfContent, pdfContent.length, signature.length);
            System.arraycopy(serializedInfo, 0, signedPdfContent, pdfContent.length + signature.length,
                    serializedInfo.length);

            //System.out.println(byteArrayToHexString(signedPdfContent));
            // Save the signed PDF with additional information
            Files.write(Paths.get("C:\\Users\\Admin\\Downloads\\signed_file1.pdf"), signedPdfContent);

            byte[] tests = signedPdfContent;
            // Read the signature and additional information from the file and verify
            int pdfLength = pdfContent.length;
            byte[] readSignature = Arrays.copyOfRange(signedPdfContent, pdfLength, pdfLength + signature.length);
            byte[] readInfo = Arrays.copyOfRange(signedPdfContent, pdfLength + signature.length,
                    signedPdfContent.length);

            boolean isVerified = verifySignature(readSignature, keyPair.getPublic(), pdfHash);

            if (isVerified) {
                System.out.println("Digital signature is valid.");
                // Deserialize and access signing time and signer information
                SignatureInfo extractedInfo = deserializeSignatureInfo(readInfo);
                System.out.println("Signing Time: " + extractedInfo.getSigningTime());
                System.out.println("Signer Information: " + extractedInfo.getSignerInfo());
            } else {
                System.out.println("Digital signature is not valid.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
