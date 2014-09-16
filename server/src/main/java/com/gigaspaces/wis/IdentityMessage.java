package com.gigaspaces.wis;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shadim on 9/15/2014.
 */
public class IdentityMessage implements Serializable {

    private String fqn;
    private List<String> groups = new ArrayList<String>();

    public IdentityMessage() {
    }

    public IdentityMessage(String fqdn, List<String> groups) {
        this.groups = groups;
        this.fqn = fqdn;
    }

    @XmlElement(name = "Group")
    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    @XmlElement(name = "Fqn")
    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }
}
