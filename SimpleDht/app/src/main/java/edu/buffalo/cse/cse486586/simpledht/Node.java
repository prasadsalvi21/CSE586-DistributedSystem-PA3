package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

/**
 * Created by prasad-pc on 4/7/17.
 */

public class Node{
    public String getMyPort2() {
        return myPort2;
    }

    public void setMyPort2(String myPort2) {
        this.myPort2 = myPort2;
    }

    String myPort2;
    String pred;
    String succ;

    public String getdName2() {
        return dName2;
    }

    public void setdName2(String dName2) {
        this.dName2 = dName2;
    }

    String dName2;

    public String getH_pred() {
        return h_pred;
    }

    public void setH_pred(String h_pred) {
        this.h_pred = h_pred;
    }

    public String getH_succ() {
        return h_succ;
    }

    public void setH_succ(String h_succ) {
        this.h_succ = h_succ;
    }

    String h_pred;
    String h_succ;



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
    @Override
    public String toString(){
        return "Device="+dName2+" MyPort2="+myPort2+" Pred="+pred+ " Succ="+succ+" Hash Pred="+h_pred+" Hash Succ="+h_succ;
    }
}
