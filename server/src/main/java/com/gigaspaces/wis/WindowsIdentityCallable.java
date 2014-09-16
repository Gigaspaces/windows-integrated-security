package com.gigaspaces.wis;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by shadim on 9/15/2014.
 */
class WindowsIdentityCallable implements Callable<IdentityMessage> {

    private final String credentials;
    private final String hostname;
    private final int port;
    private JAXBContext context;

    public WindowsIdentityCallable(String hostname, int port, String credentials) throws JAXBException {
        this.credentials = credentials;
        this.hostname = hostname;
        this.port = port;
        this.context = JAXBContext.newInstance(Message.class);
    }

    @Override
    public IdentityMessage call() throws Exception {

        Socket wisSocket = new Socket(hostname, port);

        PrintWriter out = new PrintWriter(wisSocket.getOutputStream(), true);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(wisSocket.getInputStream()));


        String m = BitUtils.serializeMessage(new Message(new AuthorizedMessage(credentials)), context);
        out.write(m);
        out.flush();

        String line;

        while ((line = in.readLine()) != null) {

            Message message = BitUtils.deserializeMessage(line, context);

            Object body = message.getBody();

            if (body instanceof IdentityMessage) {

                return (IdentityMessage) body;

            } else {
                return null;
            }
        }

        return null;
    }
}
