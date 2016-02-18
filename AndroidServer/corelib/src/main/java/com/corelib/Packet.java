package com.corelib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Packet
 * Created by hkff on 4/28/15.
 */
public class Packet implements Serializable {

    /**
     * Enum msg types
     */
    public enum Type {
        STRING,
        FILE,
        NONE,
        CMD,
    }

    // Attributes
    private  static  final  long serialVersionUID =  1350042861348723735L;
    private Type type = Type.NONE;
    private String msg = "";
    private byte[] content = "".getBytes();
    private ArrayList<String> extras = new ArrayList<>();

    // Getters and Setters
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ArrayList<String> getExtras() {
        return extras;
    }

    public void setExtras(ArrayList<String> extras) {
        this.extras = extras;
    }

    /**
     * Default constructor
     */
    public Packet() {}


    /**
     * Constructor
     * @param msg
     */
    public Packet(String msg) {
        this.msg = msg;
    }


    /**
     * Constructor
     * @param msg
     * @param content
     */
    public Packet(String msg, byte[] content) {
        this.msg = msg;
        this.content = content;
    }

    /**
     * Constructor
     * @param msg
     * @param content
     * @param type
     */
    public Packet(String msg, byte[] content, Type type) {
        this.msg = msg;
        this.content = content;
        this.type = type;
    }

    /**
     * Constructor
     * @param msg
     * @param type
     */
    public Packet(String msg, Type type) {
        this.msg = msg;
        this.type = type;
    }

    /**
     * Add extra
     * @param e
     */
    public void addExtra(String e) {
        this.extras.add(e);
    }

    /**
     * Build the byte packet
     * @return
     */
    public byte[] build() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Parse the packet to an object
     * @param packet
     */
    public Packet parse(byte[] packet) {
        ByteArrayInputStream bis = new ByteArrayInputStream(packet);
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(bis);
            Packet p = (Packet) in.readObject();
            this.type = p.type;
            this.msg = p.msg;
            this.content = p.content;
            this.extras = p.extras;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public String toString() {
        return  "- Type : " + this.type +
                " - Msg : " + this.msg +
                " - Content : " + new String(this.content) +
                " - Extras : " + this.extras;
    }

}
