package com.gigaspaces.wis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import waffle.windows.auth.IWindowsIdentity;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ExecutorService pool;
    private ServerSocket server;
    private final int port;
    private String securityPackage = "Negotiate";
    private Log log = LogFactory.getLog(Server.class);

    private Thread mainThread;

    private final Map<String, IWindowsIdentity> authenticatedUsers = new ConcurrentHashMap<String, IWindowsIdentity>();

    public Server(int port, String securityPackage) throws IOException {
        this.port = port;

        this.securityPackage = securityPackage;

        //this.mainThread.start();

    }

    public IWindowsIdentity authenticate(String userName, String token) {

        IWindowsIdentity identity = authenticatedUsers.get(token);

        if (identity == null)
            return null;

        if (!identity.getFqn().contains(userName))
            return null;

        return identity;
    }

    public void bootstrap() {

        this.pool = Executors.newCachedThreadPool();

        try {
            this.server = new ServerSocket(port);

        } catch (IOException e) {
            log.error("error while binding server port:", e);
            throw new RuntimeException(e);
        }

        mainThread = new Thread(new Runnable() {


            @Override
            public void run() {
                //log.debug("windows integrated security server started successfully at port " + this.port + " with security package " + this.securityPackage);

                while (true) {

                    try {
                        Socket client = server.accept();

                        pool.submit(new ClientHandler(client, securityPackage, authenticatedUsers));
                    } catch (IOException e) {
                        log.error("error occurs while accept new connection: ", e);
                        throw new RuntimeException(e);
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mainThread.start();

    }

    public void shutdown() {

        log.debug("windows integrated security shutdown");
        try {
            server.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        pool.shutdown();

    }
}
