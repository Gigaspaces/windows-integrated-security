package com.gigaspaces.wis;

import waffle.util.Base64;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by shadim on 9/10/2014.
 */
public class BitUtils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static Message deserializeMessage(String input, final JAXBContext context) throws JAXBException {

        byte[] buffer = Base64.decode(input);

        Object result = null;

        Unmarshaller unmarshaller = context.createUnmarshaller();

        ByteArrayInputStream bin = new ByteArrayInputStream(buffer);

        try {
            result = unmarshaller.unmarshal(bin);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                bin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return (Message) result;
    }

    public static String serializeMessage(Message message, final JAXBContext context) {
        String msgString = null;

        try {
            Marshaller marshaller = context.createMarshaller();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {

                marshaller.marshal(message, baos);

                msgString = Base64.encode(baos.toByteArray()) + "\n";
            } finally {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return msgString;
    }
}
