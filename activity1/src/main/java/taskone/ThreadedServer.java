/**
  File: Server.java
  Author: Student in Fall 2020B
  Description: Server class in package tasktwo.
*/

package taskone;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class: Server
 * Description: Server tasks.
 */
class ThreadedServer {

    public static void main(String[] args) throws Exception {
        Socket sock = null;
        int port;
        int id = 0;
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
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server Started...");
            while (true) {
                sock = server.accept();
                System.out.println("Accepted a Request..."); //was "Accepting a request" before.
//                System.out.println("Threaded server connected to client-" + id);
                ThreadedPerformer threadedPerformer = new ThreadedPerformer(sock, strings, id);
                threadedPerformer.start();
                id ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sock != null) {
                System.out.println("close socket of client ");
                sock.close();
            }
        }
    }
}

