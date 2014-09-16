package com.gigaspaces.wis;

import javax.xml.bind.JAXBException;

/**
 * Created by shadim on 9/16/2014.
 */
public class MessageException extends Exception {

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable e) {
        super(message
                , e);
    }
}
