package edu.buffalo.cse.cse486586.simpledht;

/**
 * Created by prasad-pc on 4/6/17.
 */

import java.io.Serializable;
import java.util.TreeMap;

/** A simple chord message. */
public class Message {

    public String key;
    public String port;
    public String pred;
    public String succ;
    public String type;
    public TreeMap<String, Node> map = null;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPred() {
        return pred;
    }

    public void setPred(String pred) {
        this.pred = pred;
    }

    public String getSucc() {
        return succ;
    }

    public void setSucc(String succ) {
        this.succ = succ;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TreeMap<String, Node> getMap() {
        return map;
    }

    public void setMap(TreeMap<String, Node> map) {
        this.map = map;
    }

    @Override
    public String toString(){
        return "Key="+key+" Myport="+port+ " PRED="+pred+" Succ="+succ+" Type="+type;
    }

}
