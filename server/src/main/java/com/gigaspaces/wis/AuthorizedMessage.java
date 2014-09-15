package com.gigaspaces.wis;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class AuthorizedMessage implements Serializable {

    private String id;

    public AuthorizedMessage() {
    }

    public AuthorizedMessage(String id) {
        this.id = id;
    }

    @XmlElement(name = "Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
