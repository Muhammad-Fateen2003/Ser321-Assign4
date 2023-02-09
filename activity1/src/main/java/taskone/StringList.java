package taskone;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class StringList {
    
    List<String> strings = new ArrayList<String>();

    public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }

    public boolean contains(String str) {
        return strings.indexOf(str) >= 0;
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }

    public void clear(){
        strings.clear();
    }

    public JSONArray toJSON() {
        JSONArray strArray = new JSONArray(strings);
        return strArray;
    }

    public int find(String str) {
        int pos = strings.indexOf(str);
        return pos;
    }

    public void sort() {
        Collections.sort(strings);
    }

    public void prepend(int pos, String str) {
        try {
            strings.set(pos, str + strings.get(pos));
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}