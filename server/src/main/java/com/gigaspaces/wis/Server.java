package com.gigaspaces.wis;

import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * Created by shadim on 9/14/2014.
 */
public class Server {
    public static void main(String[] args) throws IOException {

        AuthenticationServer server = null;
        try {
            Options options = new Options();
            options.addOption("sp", true, "security package");
            options.addOption("p", true, "server port");
            options.addOption("h", false, "print this message");

            CommandLine cmd = new PosixParser().parse(options, args);

            if (cmd.hasOption("h") || !(cmd.hasOption("sp") && cmd.hasOption("p"))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("server", options);
                System.exit(0);
            }

            int port = Integer.parseInt(cmd.getOptionValue("p"));


            server = new AuthenticationServer(port, cmd.getOptionValue("sp"));

            server.bootstrap();

            System.in.read();

        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.shutdown();
            }
        }
    }
}
