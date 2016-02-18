package com.core.payloads;

import android.app.Activity;
import android.content.Context;
import com.corelib.IPayload;
import com.corelib.Packet;

/**
 * Vibrator class
 * Created by hkff on 4/22/15.
 */
public class Vibrator implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        VIBRATE,
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
        String[] tmp = new String(msg.getContent()).split(":");

        // We handle the command
        switch (cmd){
            // Vibrate
            // Protocol : VIBRATE:time
            case VIBRATE:
                // Get argument
                int n = (tmp.length > 0)? Integer.valueOf(tmp[0]) : 1;
                this.vibrate(n);
                result.setMsg("Vrrrrrr !");
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

    public Vibrator(){}

    /**
     * Constructor
     * @param app
     */
    public Vibrator(Activity app){
        this.app = app;
    }

    /**
     * Vibrate the phone
     * @param n : Time in ms
     */
    public void vibrate(long n) {
        android.os.Vibrator v = (android.os.Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(n);
    }
}
