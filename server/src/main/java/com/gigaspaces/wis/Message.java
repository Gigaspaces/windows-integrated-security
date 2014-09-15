package com.gigaspaces.wis;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by shadim on 9/11/2014.
 */
@XmlRootElement(name = "Message")
public class Message implements Serializable {

    private Object body;

    @XmlElements(value = {
            @XmlElement(type = HelloMessage.class, name = "HelloMessage"),
            @XmlElement(type = AuthorizationRequiredMessage.class, name = "AuthorizationRequiredMessage"),
            @XmlElement(type = AuthorizedMessage.class, name = "AuthorizedMessage"),
            @XmlElement(type = NotAuthorizedMessage.class, name = "NotAuthorizedMessage"),
            @XmlElement(type = TokenMessage.class, name = "TokenMessage")
    })
    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Message(){}
    public Message(Object body){
        this.body = body;
    }
}
