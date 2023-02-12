package server;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import java.lang.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

class SockBaseServer extends Thread{
    static String logFilename = "logs.txt";

    ServerSocket serv = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    Game game;
    int tile1row = 0;
    int tile1col = 0;
    JSONArray player;
    JSONObject stats = new JSONObject();


    public SockBaseServer(Socket sock, Game game){
        this.clientSocket = sock;
        this.game = game;
//        try {
//            in = clientSocket.getInputStream();
//            out = clientSocket.getOutputStream();
//        } catch (Exception e){
//            System.out.println("Error in constructor: " + e);
//        }
    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer. 
    public void run() { //throws IOException
        boolean quit = false;
        String name = "";

        ReadJSONFile();
        System.out.println("Ready...");
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
            while (!quit) {
                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);
                String result = null;

                // if the operation is NAME (so the beginning then say there is a commention and greet the client)
                if (op.getOperationType() == Request.OperationType.NAME) {
                    // get name from proto object
                    name = op.getName();

                    // writing a connect message to the log with name and CONNENCT
                    writeToLog(name, Message.CONNECT);
                    System.out.println("Got a connection and a name: " + name);
                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.GREETING)
                            .setMessage("Hello " + name + " and welcome. Welcome to a simple game of Match. ")
                            .build();
                    response.writeDelimitedTo(out);

                    ReadJSONFile();
                    writeToLeader(name, false, true);
                }

                // Example how to start a new game and how to build a response with the board which you could then send to the server
                // LINES between ====== are just an example for Protobuf and how to work with the differnt types. They DO NOT
                // belong into this code as is!

                if (op.getOperationType() == Request.OperationType.NEW) {
                    // ========= Example start
                    game.newGame(); // starting a new game

                    // Example on how you could build a simple response for PLAY as answer to NEW
                    Response response2 = Response.newBuilder()
                            .setResponseType(Response.ResponseType.PLAY)
                            .setBoard(game.getBoard()) // gets the hidden board
                            .setEval(false)
                            .setSecond(false)
                            .build();

                    response2.writeDelimitedTo(out);

                    // On the client side you would receive a Response object which is the same as the one in line 73, so now you could read the fields
                    System.out.println("\n\nExample response:");
                    System.out.println("Type: " + response2.getResponseType());
                    System.out.println("Board:\n" + response2.getBoard());
                    System.out.println("Eval: " + response2.getEval());
                    System.out.println("Second: " + response2.getSecond());
                }

                try {
                    if (op.getOperationType() == Request.OperationType.TILE1) {
                        game.checkWin();
                        if(!game.getWon()) {
                            // this just temporarily unhides, the "hidden" image in game is still the same
                            String tile = op.getTile();

                            if (tile.matches("[a-zA-Z][1-9]")) {
                                System.out.println("One flipped tile");
                                tile1row = Character.getNumericValue(tile.charAt(0)) - 9;
                                tile1col = (Character.getNumericValue(tile.charAt(1)) * 2);
                                if (game.getHiddenTile(tile1row, tile1col) == '?') {
                                    String flippedBoard = game.tempFlipWrongTiles(tile1row, tile1col);
                                    System.out.println(flippedBoard);

                                    Response response = Response.newBuilder()
                                            .setResponseType(Response.ResponseType.PLAY)
                                            .setBoard(game.getBoard()) // gets the hidden board
                                            .setFlippedBoard(flippedBoard)
                                            .setEval(false)
                                            .setSecond(true)
                                            .build();

                                    response.writeDelimitedTo(out);
                                } else {
                                    System.out.println("Cannot flip the same tile twice, please select different tiles");
                                    Response response = Response.newBuilder()
                                            .setResponseType(Response.ResponseType.ERROR)
                                            .setMessage("Tile already flipped by you or another player. Choose different tiles.")
                                            .setBoard(game.getBoard())
                                            .build();

                                    response.writeDelimitedTo(out);
                                }
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            Response response = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.PLAY)
                                    .setBoard(game.getBoard()) // gets the hidden board
                                    .setFlippedBoard(game.getBoard())
                                    .setEval(false)
                                    .setSecond(true)
                                    .build();

                            response.writeDelimitedTo(out);
                        }
                    }

                    if (op.getOperationType() == Request.OperationType.TILE2) {
                        game.checkWin();
                        if (!game.getWon()) {
                            String tile = op.getTile();

                            if (tile.matches("[a-zA-Z][1-9]")) {
                                System.out.println("Two flipped tiles");
                                int tile2row = Character.getNumericValue(tile.charAt(0)) - 9;
                                int tile2col = (Character.getNumericValue(tile.charAt(1)) * 2);
                                if ((tile1row != tile2row || tile1col != tile2col) && game.getHiddenTile(tile2row, tile2col) == '?') {
                                    String flippedBoard = game.tempFlipWrongTiles(tile1row, tile1col, tile2row, tile2col);
                                    System.out.println(flippedBoard);

                                    boolean match = (game.getTile(tile1row, tile1col) == game.getTile(tile2row, tile2col));

                                    if (match) {
                                        System.out.println("Flip for found match, hidden in game will now be changed");
                                        // I flip two tiles here but it will NOT necessarily be a match, since I hard code the rows/cols here
                                        // and the board is randomized
                                        game.replaceOneCharacter(tile1row, tile1col);
                                        game.replaceOneCharacter(tile2row, tile2col);
                                        System.out.println(game.getBoard()); // shows the now not hidden tiles

                                    }

                                    game.checkWin();
                                    Response response;
                                    if (!game.getWon()) {
                                        response = Response.newBuilder()
                                                .setResponseType(Response.ResponseType.PLAY) // game.getWon() ? Response.ResponseType.WON : Response.ResponseType.PLAY
                                                .setBoard(game.getBoard()) // gets the hidden board
                                                .setFlippedBoard(flippedBoard)
                                                .setEval(match)
                                                .setSecond(false)
                                                .build();

                                    } else {
                                        response = Response.newBuilder()
                                                .setResponseType(Response.ResponseType.WON)
                                                .setBoard(game.getBoard())
                                                .build();

                                        ReadJSONFile();
                                        writeToLeader(name, true, false);
                                    }

                                    response.writeDelimitedTo(out);
                                } else {
                                    System.out.println("You must flip a valid tile, that is on the board, and you cannot flip the same tile twice, please select different tiles");

                                    Response response = Response.newBuilder()
                                            .setResponseType(Response.ResponseType.ERROR)
                                            .setMessage("Tile already flipped by you or another player. Choose different tiles.")
                                            .setBoard(game.getBoard())
                                            .build();

                                    response.writeDelimitedTo(out);
                                }
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            Response response = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.WON)
                                    .setBoard(game.getBoard())
                                    .build();

                            ReadJSONFile();
                            writeToLeader(name, true, false);
                            response.writeDelimitedTo(out);
                        }
                    }
                } catch (IllegalArgumentException e2){
                    System.out.println("Invalid input, please enter a character followed by a number between 1 and 9 (e.g. 'a1')");
                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.ERROR)
                            .setMessage("Invalid input, please enter a character followed by a number between 1 and 9 (e.g. 'a1')")
                            .setBoard(game.getBoard())
                            .build();

                    response.writeDelimitedTo(out);
                } catch (ArrayIndexOutOfBoundsException e1){
                    System.out.println("You must flip a valid tile, that is on the board, and you cannot flip the same tile twice, please select different tiles");
                    if (game.getHiddenTile(tile1row,tile1col) != '?') {
                        game.tempFlipWrongTiles(tile1row, tile1col);
                    }
                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.ERROR)
                            .setMessage("You must flip a valid tile, that is on the board")
                            .setBoard(game.getBoard())
                            .build();

                    response.writeDelimitedTo(out);
                }

                if (op.getOperationType() == Request.OperationType.LEADER) {
                    // Creating Entry and Leader response
                    Response.Builder res = Response.newBuilder()
                            .setResponseType(Response.ResponseType.LEADER);

                    ReadJSONFile();
                    for (int i = 0; i < player.length(); i++) {
                        Entry leader = Entry.newBuilder()
                                .setName(player.getJSONObject(i).getString("name"))
                                .setWins(player.getJSONObject(i).getInt("wins"))
                                .setLogins(player.getJSONObject(i).getInt("logins"))
                                .build();
                        res.addLeader(leader);
                    }

                    // building an Entry for the leaderboard
//                    Entry leader = Entry.newBuilder()
//                            .setName("name")
//                            .setWins(0)
//                            .setLogins(0)
//                            .build();
//
//                    // building another Entry for the leaderboard
//                    Entry leader2 = Entry.newBuilder()
//                            .setName("name2")
//                            .setWins(1)
//                            .setLogins(1)
//                            .build();
//
//                    // adding entries to the leaderboard
//                    res.addLeader(leader);
//                    res.addLeader(leader2);

                    // building the response
                    Response response3 = res.build();

                    // iterating through the current leaderboard and showing the entries

                    System.out.println("\n\nExample Leaderboard:");
                    for (Entry lead : response3.getLeaderList()) {
                        System.out.println(lead.getName() + ": " + lead.getWins());
                    }

                    response3.writeDelimitedTo(out);
                }

                if (op.getOperationType() == Request.OperationType.QUIT) {
                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.BYE)
                            .setMessage("Good Bye")
                            .build();

                    response.writeDelimitedTo(out);
                    quit = true;
                }

                // ========= Example end
            }
        } catch (NullPointerException e1) {
            System.out.println("Client abruptly Closed");
        } catch (Exception ex) {
            System.out.println("Client abruptly Closed");
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e){
                System.out.println("Closing sockets and streams, please wait...");
            }
        }
    }


    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();
            System.out.println(date);

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    public int returnSearch(JSONArray array, String searchValue){
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj= null;
            try {
                obj = array.getJSONObject(i);
                if(obj.getString("name").equals(searchValue))
                {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    void writeToLeader(String playerName, boolean won, boolean login) {
        //playername, points
        int index = returnSearch(player, playerName);
        if(index != -1 && won){
            int prevWins = player.getJSONObject(index).getInt("wins");
            player.getJSONObject(index).put("wins", prevWins + 1);
        } else if (index != -1 && login) {
//            player.getJSONObject(index).put("name", playerName);
//            player.getJSONObject(index).put("wins", player.getJSONObject(index).getInt("wins"));
            int prevLogins = player.getJSONObject(index).getInt("logins");
            player.getJSONObject(index).put("logins", prevLogins + 1);
        } else {
            stats.put("name", playerName);
            stats.put("wins", 0);
            stats.put("logins", 1);
            player.put(stats);
        }
        try {
            FileWriter lbWriter = new FileWriter("src/main/json/leaderboard.json");
            lbWriter.write(player.toString());
            lbWriter.flush();
            lbWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void ReadJSONFile(){
        try {
            FileReader lbReader = new FileReader("src/main/json/leaderboard.json");
            player = new JSONArray(new JSONTokener(lbReader));
        } catch (Exception e) {
            player = new JSONArray();
        }
    }

    public static void main (String args[]) throws Exception {
        Game game = new Game();

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        int sleepDelay = 10000; // default delay
        Socket clientSocket = null;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            serv = new ServerSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        try {
            while (true) {
                Socket sock = null;
                try {
                    clientSocket = serv.accept();
                    SockBaseServer server = new SockBaseServer(clientSocket, game);
                    server.start();
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
            if (serv != null) {
                try {
                    serv.close();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            }
        }
    }
}

