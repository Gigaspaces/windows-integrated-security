package com.gigaspaces.wis;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class TokenMessage implements Serializable {
    private String token;

    public TokenMessage(byte[] token) {
        this.token = BitUtils.bytesToHex(token);
    }

    public TokenMessage() {
    }

    @XmlElement(name = "Token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + token;
    }
}
