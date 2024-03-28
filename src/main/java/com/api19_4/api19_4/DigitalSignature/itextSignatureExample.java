package com.api19_4.api19_4.DigitalSignature;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

public class itextSignatureExample {

    public static final String SRC = "C:\\Users\\Admin\\Downloads\\test1.pdf";
    public static final String DEST = "C:\\Users\\Admin\\Downloads\\signed.pdf";
    public static final String KEYSTORE_PATH = "C:\\Users\\Admin\\Downloads\\keystore.p12";
    public static final String KEYSTORE_PASSWORD = "keystore_1";
    public static final String ALIAS = "alias_1";

    public static void main(String[] args) throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        KeyStore ks = KeyStore.getInstance("pkcs12", provider.getName());
        ks.load(new java.io.FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

        String dest = DEST;
        String src = SRC;

        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

        // Creating the signature appearance
        Rectangle rect = new Rectangle(36, 648, 200, 100);
        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason("Test")
                .setLocation("World")
                .setReuseAppearance(false);

        signer.setFieldName("sig");

        IExternalSignature pks = new PrivateKeySignature((PrivateKey) ks.getKey(ALIAS, KEYSTORE_PASSWORD.toCharArray()), DigestAlgorithms.SHA256, provider.getName());
        IExternalDigest digest = new BouncyCastleDigest();

        signer.signDetached(digest, pks, new Certificate[]{ks.getCertificate(ALIAS)}, null, null, null, 0, PdfSigner.CryptoStandard.CMS);
    }
}
