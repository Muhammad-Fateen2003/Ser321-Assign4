package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.util.*;
import java.util.stream.Collectors;

class SockBaseClient {

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int port = 9099; // default port

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0].toString();
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server.");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // write to the server
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            if (strToSend.equals("exit")){
                Quit(op, out, in);
            }

            op.writeDelimitedTo(out);

            // read from the server
            response = Response.parseDelimitedFrom(in);

            // print the server response. 
            System.out.println(response.getMessage());

            int choice = 0;
            while (choice != 3) {
                System.out.println("* \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - quit the game");

                String strChoice = stdin.readLine();
                while (!strChoice.matches("\\d+") && !strChoice.equals("exit")) {
                    System.out.println("Invalid input, please enter a number (with any number of digits)");
                    strChoice = stdin.readLine();
                }
                if (strChoice.equals("exit")){
                    Quit(op, out, in);
                }
                choice = Integer.parseInt(strChoice);

                switch (choice) {
                    case 1:
                        // LEADER
                        op = Request.newBuilder()
                                .setOperationType(Request.OperationType.LEADER)
                                .build();

                        op.writeDelimitedTo(out);

                        // read from the server
                        response = Response.parseDelimitedFrom(in);

                        if (response.getResponseType() == Response.ResponseType.LEADER){
                            int maxLength = 20;

                            String nameHeader = String.format("%-" + maxLength + "s", "Name");
                            String winsHeader = String.format("%-" + maxLength + "s", "Wins");
                            String loginsHeader = String.format("%-" + maxLength + "s", "Logins");

                            System.out.println(nameHeader + "  " + winsHeader + "  " + loginsHeader);
                            System.out.println(new String(new char[maxLength * 3]).replace("\0", "-"));

                            for (Entry lead: response.getLeaderList()) {
                                String name = String.format("%-" + maxLength + "s", lead.getName());
                                String wins = String.format("%-" + maxLength + "s", lead.getWins());
                                String logins = String.format("%-" + maxLength + "s", lead.getLogins());

                                System.out.println(name + "  " + wins + "  " + logins);
                            }
                        }
                        break;
                    case 2:
                        // NEW
                        op = Request.newBuilder()
                                .setOperationType(Request.OperationType.NEW)
                                .build();

                        op.writeDelimitedTo(out);

                        // read from the server
                        response = Response.parseDelimitedFrom(in);

                        // print the server response.
                        System.out.println("\n\nHere is the Current Board:");
                        System.out.println("Type: " + response.getResponseType());
                        System.out.println("Board:\n" + response.getBoard());
                        System.out.println("Eval: " + response.getEval());
                        System.out.println("Second: " + response.getSecond());

                        boolean won = false;

                        while (!won) {
                            System.out.println("Choose the first tile to flip:");
                            String strTile1 = stdin.readLine();

                            while (!strTile1.matches("[a-zA-Z][1-9]")) {
                                if (strTile1.equals("exit")){
                                    Quit(op, out, in);
                                }
                                System.out.println("Invalid input, please enter a character followed by a number between 1 and 9 (e.g. 'a1')");
                                strTile1 = stdin.readLine();
                            }


                            op = Request.newBuilder()
                                    .setOperationType(Request.OperationType.TILE1)
                                    .setTile(strTile1)
                                    .build();

                            op.writeDelimitedTo(out);

                            response = Response.parseDelimitedFrom(in);

                            if(response.getResponseType() == Response.ResponseType.ERROR){
                                System.out.println("Board:\n" + response.getBoard());
                                System.out.println("Error: " + response.getMessage());
                            } else {
                                System.out.println("\n\nHere is the Current Board:");
                                System.out.println("Type: " + response.getResponseType());
                                System.out.println("Board:\n" + response.getFlippedBoard());
                                System.out.println("Eval: " + response.getEval());
                                System.out.println("Second: " + response.getSecond());

                                System.out.println("Choose the second tile to flip:");
                                String strTile2 = stdin.readLine();

                                while (!strTile2.matches("[a-zA-Z][1-9]")) {
                                    if (strTile2.equals("exit")){
                                        Quit(op, out, in);
                                    }
                                    System.out.println("Invalid input, please enter a character followed by a number between 1 and 9 (e.g. 'a1')");
                                    strTile2 = stdin.readLine();

                                }

                                op = Request.newBuilder()
                                        .setOperationType(Request.OperationType.TILE2)
                                        .setTile(strTile2)
                                        .build();

                                op.writeDelimitedTo(out);

                                response = Response.parseDelimitedFrom(in);

                                String anyThing;
                                if (response.getResponseType() == Response.ResponseType.ERROR){
                                    System.out.println("Board:\n" + response.getBoard());
                                    System.out.println("Error: " + response.getMessage());
                                } else {
                                    if (response.getResponseType() == Response.ResponseType.WON) {
                                        System.out.println("\n\nYou Win, here is the completed board:");
                                        System.out.println("Board:\n" + response.getBoard());
                                        won = true;
                                    } else if (response.getEval()) {
                                        System.out.println("\n\nHere is the Board:");
                                        System.out.println("Type: " + response.getResponseType());
                                        System.out.println("Board:\n" + response.getFlippedBoard());
                                        System.out.println("Eval: " + response.getEval());
                                        System.out.println("Second: " + response.getSecond());
                                        System.out.println("Enter Anything to get the Current Board: ");
                                        anyThing = stdin.readLine();
                                        if (anyThing.equals("exit")){
                                            Quit(op, out, in);
                                        }
                                        System.out.println("It's a Match! Here is the Current Board:");
                                        System.out.println("Board:\n" + response.getBoard());
                                    } else {
                                        System.out.println("\n\nHere is the Board:");
                                        System.out.println("Type: " + response.getResponseType());
                                        System.out.println("Board:\n" + response.getFlippedBoard());
                                        System.out.println("Eval: " + response.getEval());
                                        System.out.println("Second: " + response.getSecond());
                                        System.out.println("Enter Anything to get the Current Board: ");
                                        anyThing = stdin.readLine();
                                        if (anyThing.equals("exit")){
                                            Quit(op, out, in);
                                        }
                                        System.out.println("Not a Match! Here is the Current Board:");
                                        System.out.println("Board:\n" + response.getBoard());

                                    }
                                }
                            }
                        }
                        break;
                    case 3:
                        // QUIT
                        Quit(op, out, in);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
            System.out.println("done | crash");
            stdin.readLine();

        } catch (Exception e) {
            System.out.println("Disconnected");
        } finally {
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }

    private static void Quit(Request op, OutputStream out, InputStream in) throws Exception{

        System.out.println("Exiting...");
        op = Request.newBuilder()
                .setOperationType(Request.OperationType.QUIT)
                .build();

        op.writeDelimitedTo(out);

        // read from the server
        Response response = Response.parseDelimitedFrom(in);

        // print the server response.
        System.out.println(response.getMessage());
        System.exit(0);
    }
}


