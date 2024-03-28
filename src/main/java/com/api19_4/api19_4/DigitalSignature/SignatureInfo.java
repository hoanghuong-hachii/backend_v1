package com.api19_4.api19_4.DigitalSignature;

import java.io.Serializable;
import java.util.Date;

public class SignatureInfo implements Serializable {
    private Date signingTime;
    private String signerInfo;

    public SignatureInfo(Date signingTime, String signerInfo) {
        this.signingTime = signingTime;
        this.signerInfo = signerInfo;
    }

    public Date getSigningTime() {
        return signingTime;
    }

    public void setSigningTime(Date signingTime) {
        this.signingTime = signingTime;
    }

    public String getSignerInfo() {
        return signerInfo;
    }

    public void setSignerInfo(String signerInfo) {
        this.signerInfo = signerInfo;
    }
// Constructors, getters, and setters
}
