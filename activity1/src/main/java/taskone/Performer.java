/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
class Performer {

    private StringList state;
    private Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.state = strings;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "add");
        state.add(str);
        json.put("data", state.toString());
        return json;
    }

    private JSONObject clear() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "clear");
        state.clear();
        json.put("data", state.toString());
        return json;
    }

    private JSONObject find(String str) {
        int pos = state.find(str);
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "add");
        json.put("data", pos);
        return json;
    }

    private JSONObject display() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "display");
        json.put("data", state.toString());
        return json;
    }

    private JSONObject sort() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "sort");
        state.sort();
        json.put("data", state.toString());
        return json;
    }

    private JSONObject prepend(String str) {
        //TODO: Fix index out of Bound Error
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "prepend");
        int pos = Integer.parseInt(str.substring(0, 1));
        str = str.substring(2);
        state.prepend(pos, str);
        json.put("data", state.toString());
        return json;
    }

    private JSONObject quit() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "quit");
        json.put("data", "");
        return json;
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }

    public void doPerform() {
        boolean quit = false;
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage = new JSONObject();
   
                int choice = message.getInt("selected");
                String inStr;
                    switch (choice) {
                        case (1):
                            inStr = (String) message.get("data");
                            returnMessage = add(inStr);
                            break;
                        case (2):
                            returnMessage = clear();
                            break;
                        case (3):
                            inStr = (String) message.get("data");
                            returnMessage = find(inStr);
                            break;
                        case (4):
                            returnMessage = display();
                            break;
                        case (5):
                            returnMessage = sort();
                            break;
                        case (6):
                            inStr = (String) message.get("data");
                            returnMessage = prepend(inStr);
                            break;
                        case (0):
                            returnMessage = quit();
                            conn.close();
                            break;
                        default:
                            returnMessage = error("Invalid selection: " + choice 
                                    + " is not an option");
                            break;
                    }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
