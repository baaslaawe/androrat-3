package com.core.payloads;

import android.app.Activity;
import com.corelib.IPayload;
import com.corelib.Packet;

/**
 * PayloadExample class
 * Created by hkff on 4/29/15.
 */
public class PayloadTemplate implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        FOO,
        // ....,
        // CMD,
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
            // FOO
            // Protocol : FOO
            case FOO:
                result.setMsg(this.foo());
                break;

            // CMD
            // Protocol : CMD
            /*case CMD:
                // handle CMD
                break;
            */
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

    public PayloadTemplate(){}

    /**
     * Constructor
     * @param app
     */
    public PayloadTemplate(Activity app){
        this.app = app;
    }

    /**
     * Foo
     */
    public String foo() {
        return "BAR";
    }
}
