package com.api19_4.api19_4.DigitalSignature;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

public class keySignatureExample {

    public static void main(String[] args) throws Exception {
        // Register Bouncy Castle as a security provider
        Security.addProvider(new BouncyCastleProvider());

        // Generate a key pair
        KeyPair keyPair = generateKeyPair();

        // Save private key in PKCS#12 keystore
        String alias = "DS00002";
        String company = "CT TNHH DONG A";
        String name = "Hoang Van Nam";
        savePrivateKeyToKeystore(keyPair, "keystorecer.p12", "123456", alias,company, name);
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    private static void savePrivateKeyToKeystore(KeyPair keyPair, String keystorePath, String keystorePassword, String alias, String company, String name) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        // Create X509 certificate (self-signed)
        X509Certificate cert = generateSelfSignedCertificate(keyPair,company,name);

        keyStore.setKeyEntry(alias, keyPair.getPrivate(), keystorePassword.toCharArray(), new java.security.cert.Certificate[]{cert});

        // Save the keystore to a file
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String company, String name) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        X500Principal subjectName = new X500Principal("CN="+company+" \n"+ name);

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(subjectName);
        certGen.setIssuerDN(subjectName);
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        return certGen.generate(keyPair.getPrivate(), "BC");
    }
}
