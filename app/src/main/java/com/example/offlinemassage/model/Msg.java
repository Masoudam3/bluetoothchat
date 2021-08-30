package com.example.offlinemassage.model;

public class Msg {


    public String text;
    public boolean isMine;


    public Msg(String text, boolean isMine){
        this.text = text;
        this.isMine = isMine;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }


    public boolean isMine() {
        return isMine;
    }


    public void setMine(boolean mine) {
        isMine = mine;
    }
}
