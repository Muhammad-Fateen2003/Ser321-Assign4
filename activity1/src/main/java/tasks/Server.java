/**
  File: Server.java
  Author: Student in Fall 2020B
  Description: Server class in package taskone.
*/

package tasks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class: Server
 * Description: Server tasks.
 */
class Server {

    public static void main(String[] args) throws Exception {
        int port;
        StringList strings = new StringList();

        if (args.length != 1) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runServer -Pport=9099 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }
        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started...");
        Socket sock = null;
        try {
            while (true) {
                System.out.println("Accepting a Request...");
                try {
                    sock = server.accept();

                    Performer performer = new Performer(sock, strings);
                    performer.doPerform();
                } catch (NullPointerException e1) {
                    System.out.println("Client disconnect");
                } catch (Exception e2) {
                    System.out.println("Client disconnect");
                } finally {
                    if (sock != null) {
                        sock.close();
                        System.out.println("Socket closed");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        } finally {
            if (server != null) {
                try {
                    System.out.println("close socket of client ");
                    sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
