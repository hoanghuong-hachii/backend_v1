package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.DigitalSignature.SignPdfExample;
import com.api19_4.api19_4.model.SignPdfRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;
@RestController
@RequestMapping("api/v1/signDigital")
public class PdfSignController {

    @PostMapping("/sign")
    public ResponseEntity<String> signPdf1(@RequestBody SignPdfRequest signPdfRequest) {
        String outputPdf = "C:\\Users\\Admin\\Downloads\\file_signed.pdf";
        String inputPdf = "./static/images/" + signPdfRequest.getNameInputFile();
        String keyStorePath = "keystorecer.p12";
        String reason = signPdfRequest.getReason();

        int page = signPdfRequest.getPage();
        float x = signPdfRequest.getX();
        float y = signPdfRequest.getY();
        try {
            signPdf(inputPdf, outputPdf,
                    keyStorePath, signPdfRequest.getKeystorePassword(), signPdfRequest.getAlias(),reason, page, x, y);
            return ResponseEntity.ok("PDF signed successfully!");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during PDF signing.");
        }
    }

    public static void signPdf(String inputPath, String outputPath, String keystorePath, String keystorePassword, String alias,
                               String reason,int page, float x, float y) throws GeneralSecurityException, IOException {

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new File(keystorePath).toURI().toURL().openStream(), keystorePassword.toCharArray());

        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfWriter writer = new PdfWriter(new FileOutputStream(outputPath));
        PdfSigner signer = new PdfSigner(new PdfReader(inputPath), writer, new StampingProperties());

        signer.setFieldName(alias);
        signer.getSignatureAppearance()
                .setPageRect(new Rectangle(x, y, 300, 100)
                );
        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason(reason)
                .setPageNumber(page)
                .setReuseAppearance(false);

        IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, null);
        IExternalDigest digest = new BouncyCastleDigest();
        signer.signDetached(digest, signature, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPdf(@RequestBody SignPdfRequest signPdfRequest) {
        String inputPdf = "C:\\Users\\Admin\\Downloads\\file_signed.pdf";
        String alias = signPdfRequest.getAlias();

        try {
            boolean res = verify(inputPdf, alias);
            if(res == true){
                return ResponseEntity.ok("PDF verified successfully!");

            }
            else {
                return ResponseEntity.ok("Not success!");

            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during PDF verification.");
        }
    }

    public static boolean verify(String inputPath, String alias)
            throws GeneralSecurityException, IOException {

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPath));
        boolean genuineAndWasNotModified = false;
        String signatureFieldName = alias;
        SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
        try {
            PdfPKCS7 signature1 = signatureUtil.readSignatureData(signatureFieldName);
            if (signature1 != null) {
                genuineAndWasNotModified = signature1.verifySignatureIntegrityAndAuthenticity();
                System.out.println("Signature verification result: " + genuineAndWasNotModified);
            }
        } catch (Exception e) {
            System.out.println("Error occurred during signature verification: " + e.getMessage());
            return false;
        }

        Boolean completeDocumentIsSigned = signatureUtil.signatureCoversWholeDocument(signatureFieldName);
        if (!completeDocumentIsSigned) {
            System.out.println("The entire document is not signed.");
            return false;
        }

        if (completeDocumentIsSigned && genuineAndWasNotModified) {
            System.out.println("Verification successful");
            pdfDocument.close();
            return true;
        } else {
            System.out.println("Verification failed. The content of the file or the signature has been tampered with.");
            pdfDocument.close();
            return false;
        }
    }


}
