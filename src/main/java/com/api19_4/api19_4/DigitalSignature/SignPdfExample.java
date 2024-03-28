package com.api19_4.api19_4.DigitalSignature;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

public class SignPdfExample {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        String inputPdf = "./static/images/20240305114440434.pdf";
        String outputPdf = "outputt_signed.pdf";
        String keystorePath = "keystorecer.p12";
        String keystorePassword = "123456";
        String alias = "DS00007";
        String reason = "hello0";
        String conpany = "cntt";
        String name = "huong";
        int page = 1; // Trang số 1
        float x = 0;
        float y = 0;
        float width = 316f;
        float height = 153f;


        signPdf(inputPdf, outputPdf, keystorePath, keystorePassword, alias,reason,  page, x, y);
        verify(outputPdf, alias);
    }

    public static void signPdf(String inputPath, String outputPath, String keystorePath, String keystorePassword, String alias,
                               String reason, int page,float x,float y) throws GeneralSecurityException, IOException {

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new File(keystorePath).toURI().toURL().openStream(), keystorePassword.toCharArray());

        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfWriter writer = new PdfWriter(new FileOutputStream(outputPath));

        PdfSigner signer = new PdfSigner(new PdfReader(inputPath), writer, new StampingProperties());

        // Set signature appearance
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

    public static void verify(String inputPath, String alias)
            throws GeneralSecurityException, IOException {

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputPath));

        boolean genuineAndWasNotModified = false;

        String signatureFieldName = alias;
        SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
        try {
            PdfPKCS7 signature1 = signatureUtil.readSignatureData(signatureFieldName);
            if (signature1 != null) {
                genuineAndWasNotModified = signature1.verifySignatureIntegrityAndAuthenticity();
                System.out.println("sign xac minh: "+ genuineAndWasNotModified);
            }
        } catch (Exception ignored) {
            System.out.println(ignored);
            System.out.println("sign xac minh: "+ genuineAndWasNotModified);
        }

        Boolean completeDocumentIsSigned = signatureUtil.signatureCoversWholeDocument(signatureFieldName);
        if (!completeDocumentIsSigned)
        {
            System.out.println("file xac minh: "+ completeDocumentIsSigned);
        }
        else{
            System.out.println("file xac minh: "+ completeDocumentIsSigned);

        }

        if(completeDocumentIsSigned==true && genuineAndWasNotModified==true)
        {
            System.out.println("XÁC MINH THÀNH CÔNG");
        }
        else {
            System.out.println("XÁC MINH KHÔNG THÀNH CÔNG, NỘI DUNG FILE HOẶC CHỮ KÝ ĐÃ BỊ SỬA ĐỔI");
        }
        pdfDocument.close();

    }




}
