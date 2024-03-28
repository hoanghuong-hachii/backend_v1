package com.api19_4.api19_4.model;

import java.nio.file.Path;

public class SignPdfRequest {
    private String nameInputFile; // Đường dẫn đến file PDF nguồn
    private Path outputPath; // Đường dẫn đến file PDF ký
    private String keystorePassword; // Mật khẩu của keystore
    private String alias; // Alias của chứng chỉ trong keystore
    private String reason;
    private int page;
    private float x;
    private float y;
    // Constructors, getters, and setters


    public SignPdfRequest(String nameInputFile, Path outputPath, String keystorePassword, String alias, String reason, int page, float x, float y) {
        this.nameInputFile = nameInputFile;
        this.outputPath = outputPath;
        this.keystorePassword = keystorePassword;
        this.alias = alias;
        this.reason = reason;
        this.page = page;
        this.x = x;
        this.y = y;
    }

    public String getNameInputFile() {
        return nameInputFile;
    }

    public void setNameInputFile(String nameInputFile) {
        this.nameInputFile = nameInputFile;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }


    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
