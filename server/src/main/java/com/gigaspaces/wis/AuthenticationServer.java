package com.gigaspaces.wis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import waffle.windows.auth.IWindowsIdentity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthenticationServer {

    private ExecutorService pool;
    private ServerSocket server;
    private final int port;
    private String securityPackage = "Negotiate";
    private Log log = LogFactory.getLog(AuthenticationServer.class);

    private Thread mainThread;

    private final Map<String, IWindowsIdentity> authenticatedUsers = new ConcurrentHashMap<String, IWindowsIdentity>();

    public AuthenticationServer(int port, String securityPackage) throws IOException {
        this.port = port;
        this.securityPackage = securityPackage;
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
            log.info("server start listening : "+port);

            mainThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    log.info("server started successfully");
                    log.info("server waiting for new connection...");
                    while (true) {

                        try {
                            Socket client = server.accept();

                            pool.submit(new ClientHandler(client, securityPackage, authenticatedUsers));
                        } catch (Throwable e) {
                            log.error("error occurs while accept new connection: ", e);
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            mainThread.start();

        } catch (Throwable e) {
            log.error("error while binding server port:", e);
            throw new RuntimeException(e);
        }


    }

    public void shutdown() throws IOException {

        log.debug("windows integrated security shutdown");
        try {
            server.close();
        } catch (IOException e) {
            log.error("error while binding server port:", e);
            throw e;
        }

        pool.shutdown();

//        if (mainThread == null)
//            mainThread.interrupt();
    }
}
