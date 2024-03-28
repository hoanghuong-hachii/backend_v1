package com.api19_4.api19_4.DigitalSignature;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PDFHasher {
    public static String hashPDF(String filePath) {
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

        String pdfFilePath = "C:\\Users\\Admin\\Downloads\\test.pdf";
        String hashedPDF = hashPDF(pdfFilePath);
        System.out.println("Hashed PDF (SHA-256): " + hashedPDF);
    }
}
