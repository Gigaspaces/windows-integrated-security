package com.gigaspaces.wis;

import com.sun.jna.platform.win32.Win32Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import waffle.util.Base64;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsSecurityContext;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

/**
 * Created by shadim on 9/10/2014.
 */
public class ClientHandler implements Runnable {

    private final static Log logger = LogFactory.getLog(ClientHandler.class);

    private final Socket client;
    private final JAXBContext context;
    private final PrintWriter out;
    private final BufferedReader in;
    private final String securityPackage;
    private final WindowsAuthProviderImpl provider = new WindowsAuthProviderImpl();
    private final Map<String, IWindowsIdentity> authenticatedUsers;

    public ClientHandler(Socket client, String securityPackage, Map<String, IWindowsIdentity> authenticatedUsers) throws IOException, JAXBException {
        this.authenticatedUsers = authenticatedUsers;
        this.securityPackage = securityPackage;
        this.client = client;
        this.context = JAXBContext.newInstance(Message.class);
        this.out = new PrintWriter(client.getOutputStream(), true);
        this.in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
    }

    @Override
    public void run() {

        String input;

        try {
            while ((input = in.readLine()) != null) {
                Message msg = null;
                try {
                    msg = deserializeMessage(input);
                } catch (JAXBException e) {
                    logger.error("can not read message: " + input, e);
                    sendMessage(new NotAuthorizedMessage());
                    break;
                }

                Object body = msg.getBody();

                if (!(body instanceof TokenMessage)) {
                    logger.info("message received: " + body);
                }

                if (body instanceof HelloMessage) {
                    sendMessage(new AuthorizationRequiredMessage());
                } else if (body instanceof TokenMessage) {
                    TokenMessage tokenMessage = (TokenMessage) body;

                    logTokenMessage(tokenMessage);

                    IWindowsSecurityContext serverContext;


                    try {
                        byte[] tkn = BitUtils.hexStringToByteArray(tokenMessage.getToken());

                        IWindowsCredentialsHandle credentials = WindowsCredentialsHandleImpl.getCurrent(securityPackage);

                        serverContext = provider.acceptSecurityToken("server-connection", tkn, securityPackage);

                        if (serverContext.isContinue()) {
                            byte[] token = serverContext.getToken();
                            TokenMessage t = new TokenMessage(token);
                            logTokenMessage(t);
                            sendMessage(t);
                        } else {
                            logger.info("user " + serverContext.getIdentity().getFqn() + " logged in");

                            String id = UUID.randomUUID().toString();
                            // save id and user identity to the authenticated users cache
                            authenticatedUsers.put(id, serverContext.getIdentity());

                            sendMessage(new AuthorizedMessage(id));
                            break;
                        }
                    } catch (Win32Exception e) {
                        logger.error("exception receiving token", e);
                        sendMessage(new NotAuthorizedMessage());
                        break;
                    }
                } else {
                    logger.info("received unknown message type " + body);
                    sendMessage(new NotAuthorizedMessage());
                    break;
                }
            }
        } catch (IOException e) {
            logger.info("received unknown message type");
            sendMessage(new NotAuthorizedMessage());
        }
    }

    private void logTokenMessage(TokenMessage tokenMessage) {
        logger.info("token received from client => ");
        logger.info("length: " + tokenMessage.getToken().length() / 2);
        logger.info("content: " + tokenMessage);
    }

    private void sendMessage(Object body) {

        String m = serializeMessage(new Message(body));

        out.write(m);

        out.flush();
    }

    private String serializeMessage(Message message) {

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

    private Message deserializeMessage(String input) throws JAXBException {

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
                logger.error("", e);
                e.printStackTrace();
            }
        }

        return (Message) result;
    }
}
