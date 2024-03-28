package com.api19_4.api19_4.DigitalSignature;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.PdfPKCS7;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.itextpdf.signatures.CertificateVerification;
import com.itextpdf.signatures.VerificationException;

public class SignatureVerification {

    public static void verifySignatures(String pdfPath, KeyStore keystore) throws IOException, GeneralSecurityException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfPath));
        SignatureUtil signUtil = new SignatureUtil(pdfDoc);
        List<String> names = signUtil.getSignatureNames();

        for (String name : names) {
            System.out.println("===== " + name + " =====");
            verifySignature(signUtil, name, keystore);
        }
    }

    public static void verifySignature(SignatureUtil signUtil, String name, KeyStore keystore) throws GeneralSecurityException, IOException {
        PdfPKCS7 pkcs7 = getSignatureData(signUtil, name);
        Certificate[] certs = pkcs7.getSignCertificateChain();

        // Timestamp is a secure source of signature creation time,
        // because it's based on Time Stamping Authority service.
        Calendar cal = pkcs7.getTimeStampDate();

        // If there is no timestamp, use the current date
        if (cal == null) {
            cal = Calendar.getInstance();
        }

        // Check if the certificate chain, presented in the PDF, can be verified against
        // the provided keystore.
        List<VerificationException> errors = CertificateVerification.verifyCertificates(certs, keystore, cal);
        if (errors.size() == 0) {
            System.out.println("Certificates verified against the KeyStore");
        } else {
            for (Exception error : errors) {
                System.out.println(error.getMessage());
            }
        }

        // Find out if certificates were valid on the signing date, and if they are still valid today
        for (int i = 0; i < certs.length; i++) {
            X509Certificate cert = (X509Certificate) certs[i];
            System.out.println("=== Certificate " + i + " ===");
            showCertificateInfo(cert, cal.getTime());
        }

        // Check validity of the document at the time of signing
        System.out.println("=== Checking validity of the document at the time of signing ===");
        // You can implement this part according to your requirements

        // Check validity of the document today
        System.out.println("=== Checking validity of the document today ===");
        checkRevocation(pkcs7, (X509Certificate) certs[0], (certs.length > 1 ? (X509Certificate) certs[1] : null), new Date());
    }

    public static PdfPKCS7 getSignatureData(SignatureUtil signUtil, String name) throws GeneralSecurityException {
        PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
        System.out.println("Signature covers whole document: " + signUtil.signatureCoversWholeDocument(name));
        System.out.println("Document revision: " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
        System.out.println("Integrity check OK? " + pkcs7.verifySignatureIntegrityAndAuthenticity());
        return pkcs7;
    }

    public static void showCertificateInfo(X509Certificate cert, Date signDate) {
        System.out.println("Issuer: " + cert.getIssuerDN());
        System.out.println("Subject: " + cert.getSubjectDN());
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        date_format.setTimeZone(TimeZone.getTimeZone("Universal"));
        System.out.println("Valid from: " + date_format.format(cert.getNotBefore()));
        System.out.println("Valid to: " + date_format.format(cert.getNotAfter()));

        // Check if a certificate was valid on the signing date
        try {
            cert.checkValidity(signDate);
            System.out.println("The certificate was valid at the time of signing.");
        } catch (Exception e) {
            System.out.println("The certificate was not valid at the time of signing.");
        }

        // Check if a certificate is still valid now
        try {
            cert.checkValidity();
            System.out.println("The certificate is still valid.");
        } catch (Exception e) {
            System.out.println("The certificate has expired or is not yet valid.");
        }
    }

    public static void checkRevocation(PdfPKCS7 pkcs7, X509Certificate signCert, X509Certificate issuerCert, Date date)
            throws GeneralSecurityException, IOException {
        // You can implement this part according to your requirements
    }


}
